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
import com.austinhodak.tarkovapi.tarkovtracker.TTRepository
import com.austinhodak.tarkovapi.utils.*
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.utils.*
import com.austinhodak.thehideout.workmanager.PriceUpdateFactory
import com.austinhodak.thehideout.workmanager.PriceUpdateWorker
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
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
import com.skydoves.only.Only
import dagger.hilt.android.HiltAndroidApp
import io.gleap.Gleap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidApp
class Application : android.app.Application(), Configuration.Provider {

    @Inject
    lateinit var myWorkerFactory: PriceUpdateFactory

    @Inject
    lateinit var ttRepository: TTRepository

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

    override fun onCreate() {
        super.onCreate()

        createServerStatusChannel()
        createRestockNotificationChannel()
        createPriceAlertsNotificationChannel()

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        instance = this
        extras = Extras(applicationContext)
        maps = Maps(applicationContext)
        traders = Traders(applicationContext)
        rigs = Rigs(applicationContext)
        skills = Skills(applicationContext)
        presets = WeaponPresets(applicationContext)
        hideout = Hideout(applicationContext)
        ballistics = AmmoBallistics(applicationContext)

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
                        userRefTracker("update").setValue(true)
                    }
                    Timber.d("$user")
                    user?.let {
                        fsUser.postValue(it)
                    }
                }

            }
        }

        Only.init(applicationContext)

        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
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

            if (oldFrequency == it && isScheduled) {
                //Do nothing, already set.
            } else {
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresDeviceIdle(true).build()

                val priceUpdateRequest = PeriodicWorkRequestBuilder<PriceUpdateWorker>(it.toString().toLong(), TimeUnit.MINUTES).setConstraints(constraints).build()

                workManager.enqueueUniquePeriodicWork("price_update", ExistingPeriodicWorkPolicy.REPLACE, priceUpdateRequest)

                MainScope().launch {
                    UserSettingsModel.dataSyncFrequencyPrevious.update(it)
                }
            }
        }

        if (Localazy.isEnabled()) {
            Timber.d("Localazy is enabled.")
            Localazy.forceLocale(Locale.forLanguageTag("RU"), true)
        }
    }
    /**
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
                "game_info" to "{\"version\":\"0.12.12.10.16440\",\"version_date\":\"01-09-2022\",\"wipe_date\":\"12-12-2021\"}"
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

val hideoutList: Hideout by lazy {
    Application.hideout!!
}