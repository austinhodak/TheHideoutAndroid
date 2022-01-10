package com.austinhodak.thehideout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.adapty.Adapty
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.utils.*
import com.austinhodak.thehideout.utils.Prefs
import com.austinhodak.thehideout.utils.isWorkRunning
import com.austinhodak.thehideout.utils.isWorkScheduled
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.workmanager.PriceUpdateFactory
import com.austinhodak.thehideout.workmanager.PriceUpdateWorker
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.skydoves.only.Only
import dagger.hilt.android.HiltAndroidApp
import io.gleap.Gleap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidApp
class Application : android.app.Application(), Configuration.Provider {


    @Inject
    lateinit var myWorkerFactory: PriceUpdateFactory

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        var questPrefs: Prefs? = null
        var maps: Maps? = null
        var rigs: Rigs? = null
        var skills: Skills? = null
        var presets: WeaponPresets? = null
        var hideout: Hideout? = null
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

    override fun onCreate() {
        super.onCreate()

        createServerStatusChannel()


        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        instance = this
        questPrefs = Prefs(applicationContext)
        maps = Maps(applicationContext)
        rigs = Rigs(applicationContext)
        skills = Skills(applicationContext)
        presets = WeaponPresets(applicationContext)
        hideout = Hideout(applicationContext)

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
            Firebase.auth.signInAnonymously()
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

        Adapty.restorePurchases { purchaserInfo, googleValidationResultList, error ->
            if (error == null) {
                // successful restore
            }
        }

        UserSettingsModel.dataSyncFrequency.observe(MainScope()) {
            val workManager = WorkManager.getInstance(this)
            val oldFrequency = UserSettingsModel.dataSyncFrequencyPrevious.value

            val isScheduled = isWorkScheduled(this, "price_update")
            val isRunning = isWorkRunning(this, "price_update")

            Timber.d("Scheduled $isScheduled || Running $isRunning")

            if (oldFrequency == it && isScheduled) {
                //Do nothing, already set.
            } else {
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

                val priceUpdateRequest = PeriodicWorkRequestBuilder<PriceUpdateWorker>(it.toString().toLong(), TimeUnit.MINUTES).setConstraints(constraints).build()

                workManager.enqueueUniquePeriodicWork("price_update", ExistingPeriodicWorkPolicy.REPLACE, priceUpdateRequest)

                MainScope().launch {
                    UserSettingsModel.dataSyncFrequencyPrevious.update(it)
                }
            }
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

        //Fetch and active.
        Firebase.remoteConfig.fetchAndActivate()
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .setWorkerFactory(myWorkerFactory)
        .build()
}

val questPrefs: Prefs by lazy {
    Application.questPrefs!!
}

val mapsList: Maps = Application.maps!!

val rigsList: Rigs = Application.rigs!!

val skillsList: Skills = Application.skills!!

val presetList: WeaponPresets = Application.presets!!

val hideoutList: Hideout by lazy {
    Application.hideout!!
}