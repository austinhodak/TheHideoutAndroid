package com.austinhodak.thehideout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.adapty.Adapty
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.tarkovtracker.TTRepository
import com.austinhodak.tarkovapi.utils.*
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.utils.Extras
import com.austinhodak.thehideout.utils.isWorkRunning
import com.austinhodak.thehideout.utils.isWorkScheduled
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.workmanager.PriceUpdateFactory
import com.austinhodak.thehideout.workmanager.PriceUpdateWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.localazy.android.Localazy
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionConfig
import com.qonversion.android.sdk.dto.QLaunchMode
import com.skydoves.only.Only
import dagger.hilt.android.HiltAndroidApp
import io.gleap.Gleap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal const val SyncWorkName = "SyncWorkName"

@HiltAndroidApp
class Application : android.app.Application(), Configuration.Provider {


    @Inject
    lateinit var myWorkerFactory: PriceUpdateFactory

    @Inject
    lateinit var ttRepository: TTRepository

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        var fsUser: MutableLiveData<FSUser?> = MutableLiveData()
        var extras: Extras? = null
        var maps: Maps? = null
        var traders: Traders? = null
        var rigs: Rigs? = null
        var skills: Skills? = null
        var presets: WeaponPresets? = null
        var hideout: Hideout? = null
        var ballistics: AmmoBallistics? = null
        var stims: Stims? = null
        lateinit var instance: Application
            private set
    }

    private fun createServerStatusChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Server Status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SERVER_STATUS", name, importance)
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createRestockNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Trader Restocks"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("TRADER_RESTOCK", name, importance)
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPriceAlertsNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Price Alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("PRICE_ALERTS", name, importance)
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    lateinit var listenerRegistration: ListenerRegistration

    private fun setupQonversion() {
        Qonversion.initialize(QonversionConfig.Builder(this, "QGPONvV6rgfVxSM74fv5aID13TuunIu3", QLaunchMode.SubscriptionManagement).build())
    }

    override fun onCreate() {
        super.onCreate()

        createServerStatusChannel()
        createRestockNotificationChannel()
        createPriceAlertsNotificationChannel()

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        setupQonversion()

        instance = this
        extras = Extras(applicationContext)
        maps = Maps(applicationContext)
        traders = Traders(applicationContext)
        rigs = Rigs(applicationContext)
        skills = Skills(applicationContext)
        presets = WeaponPresets(applicationContext)
        hideout = Hideout(applicationContext)
        ballistics = AmmoBallistics(applicationContext)
        stims = Stims(applicationContext)

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        //Device is either Firebase Test Lab or Google Play Pre-launch test device, disable analytics.
        if ("true" == Settings.System.getString(contentResolver, "firebase.test.lab")) {
            Firebase.analytics.setAnalyticsCollectionEnabled(false)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.d("Firebase UID: %s", uid())
        }

        Firebase.database.setPersistenceEnabled(true)

        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously().addOnSuccessListener {
                Timber.d("Result ${uid()}")
                it.user?.uid?.let {
                    listenerRegistration = Firebase.firestore.collection("users").document(it).addSnapshotListener { value, error ->
                        val user = value?.toObject<FSUser>()
                        if (value?.exists() == false) {
                            Firebase.firestore.collection("users").document(it).set(
                                hashMapOf(
                                    "playerLevel" to UserSettingsModel.playerLevel.value
                                ), SetOptions.merge())
                        }
                        Timber.d("User $user")
                        user?.let {
                            fsUser.postValue(it)
                        }
                    }
                }
            }
        } else {
            Timber.d("${uid()}")
            uid()?.let {
                listenerRegistration = Firebase.firestore.collection("users").document(it).addSnapshotListener { value, error ->
                    val user = value?.toObject<FSUser>()
                    if (value?.exists() == false) {
                        Firebase.firestore.collection("users").document(it).set(
                            hashMapOf(
                                "playerLevel" to UserSettingsModel.playerLevel.value
                            ), SetOptions.merge())
                    }
                    Timber.d("$user")
                    user?.let {
                        fsUser.postValue(it)
                    }
                }

            }
        }

        Only.init(applicationContext)

        try {
            Firebase.firestore.firestoreSettings = firestoreSettings {
                isPersistenceEnabled = true
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
        }

        Firebase.messaging.token.addOnSuccessListener {
            Timber.d("Firebase Messaging Token: %s", it)
        }

        setupRemoteConfig()

        Adapty.activate(applicationContext, "public_live_SqlSm08V.OIqFvCDBsqP81tCyaNEU", customerUserId = uid())

        UserSettingsModel.dataSyncFrequency.observe(MainScope()) {
            val workManager = WorkManager.getInstance(this)
            val oldFrequency = UserSettingsModel.dataSyncFrequencyPrevious.value

            val isScheduled = isWorkScheduled(this, "price_update")
            val isRunning = isWorkRunning(this, "price_update")

            Timber.d("Scheduled $isScheduled || Running $isRunning")

            if (oldFrequency == it && isScheduled && !isRunning) {
                //Do nothing, already set.
            } else {
                //Possible fix for prices not updating properly
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresDeviceIdle(false).build()

                val priceUpdateRequest = PeriodicWorkRequestBuilder<PriceUpdateWorker>(it.toString().toLong(), TimeUnit.MINUTES).setConstraints(constraints).build()

                workManager.enqueueUniquePeriodicWork("price_update", ExistingPeriodicWorkPolicy.REPLACE, priceUpdateRequest)

                MainScope().launch {
                    UserSettingsModel.dataSyncFrequencyPrevious.update(it)
                }
            }

            MainScope().launch(Dispatchers.IO) {
                if (tarkovRepo.isPriceDaoEmpty()) {
                    workManager.enqueue(OneTimeWorkRequest.Builder(PriceUpdateWorker::class.java).build())
                }
            }
        }

        UserSettingsModel.languageSetting.observe(MainScope()) {
            if (Localazy.isEnabled()) {
                val currentLocale = Localazy.getCurrentLocale()
                val currentSelectedLocale = it

                Localazy.forceLocale(currentSelectedLocale.locale, true)
            }
        }
    }

    /*
     * Sets up the Firebase remote config.
     */

    private fun setupRemoteConfig() {
        //Set remote config settings if debug.
        if (BuildConfig.DEBUG) {
            Firebase.remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 30
            })
        }

        Firebase.remoteConfig.setDefaultsAsync(
            mapOf(
                "game_info" to "{\"version\":\"0.12.12.30.19047\",\"version_date\":\"07-15-2022\",\"wipe_date\":\"06-30-2022\"}"
            )
        )

        //Fetch and active.
        Firebase.remoteConfig.fetchAndActivate().addOnSuccessListener {
            if (Firebase.remoteConfig.getBoolean("gleap_enabled")) {
                Gleap.initialize("RHpheXAdEP7q0gz4utGMWYVobhULPsjz", this)
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .setWorkerFactory(myWorkerFactory)
        .build()
}



val extras: Extras by lazy {
    Application.extras!!
}

val fsUser: LiveData<FSUser?> = Application.fsUser

val mapsList: Maps = Application.maps!!

val tradersList: Traders = Application.traders!!

val rigsList: Rigs = Application.rigs!!

val skillsList: Skills = Application.skills!!

val presetList: WeaponPresets = Application.presets!!

val ballistics: AmmoBallistics = Application.ballistics!!

val stims: Stims = Application.stims!!

val hideoutList: Hideout by lazy {
    Application.hideout!!
}