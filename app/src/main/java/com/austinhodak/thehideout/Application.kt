package com.austinhodak.thehideout

import android.app.Application
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.skydoves.only.Only
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONObject
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        //Device is either Firebase Test Lab or Google Play Pre-launch test device, disable analytics.
        if ("true" == Settings.System.getString(contentResolver, "firebase.test.lab")) {
            Firebase.analytics.setAnalyticsCollectionEnabled(false)
        }

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.d("Firebase UID: %s", uid())
        }

        Firebase.database.setPersistenceEnabled(true)

        Firebase.auth.signInAnonymously()

        Only.init(applicationContext)

        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }

        setupRemoteConfig()
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

        //Set default values
        Firebase.remoteConfig.setDefaultsAsync(
            mapOf(
                "fleaMarketCacheTimeout" to 900000,
                "cacheTimeout" to 14400000,
                "ammoCalibers" to JSONObject("{\"calibers\":[{\"name\":\".300\",\"longName\":\".300 Blackout\",\"key\":\"300blackout\"},{\"name\":\".338\",\"longName\":\".338 Lapua\",\"key\":\"338lapua\"},{\"name\":\"5.7x28\",\"longName\":\"5.7x28mm FN\",\"key\":\"5f4a52549f319f4528ac3623\"},{\"name\":\".45\",\"longName\":\".45 ACP\",\"key\":\"5f4a52549f319f4528ac3632\"},{\"name\":\"12.7x55\",\"longName\":\"12.7x55mm STs-130\",\"key\":\"5f4a52549f319f4528ac363d\"},{\"name\":\"40x46\",\"longName\":\"40x46mm\",\"key\":\"5f4a52549f319f4528ac3644\"},{\"name\":\".366\",\"longName\":\".366 TKM\",\"key\":\"5f4a52549f319f4528ac3650\"},{\"name\":\"23x75\",\"longName\":\"23x75mm\",\"key\":\"23x75\"},{\"name\":\"12G\",\"longName\":\"12x70mm\",\"key\":\"5f4a52549f319f4528ac365a\"},{\"name\":\"20G\",\"longName\":\"20x70mm\",\"key\":\"5f4a52549f319f4528ac367e\"},{\"name\":\"4.6x30\",\"longName\":\"4.6x30mm HK\",\"key\":\"5f4a52549f319f4528ac3690\"},{\"name\":\"5.45x39\",\"longName\":\"5.45x39mm\",\"key\":\"5f4a52549f319f4528ac369a\"},{\"name\":\"5.56x45\",\"longName\":\"5.56x45mm NATO\",\"key\":\"5f4a52549f319f4528ac36b6\"},{\"name\":\"7.62x25\",\"longName\":\"7.62x25mm Tokarev\",\"key\":\"5f4a52549f319f4528ac36cb\"},{\"name\":\"7.62x39\",\"longName\":\"7.62x39mm\",\"key\":\"5f4a52549f319f4528ac36da\"},{\"name\":\"7.62x51\",\"longName\":\"7.62x51mm NATO\",\"key\":\"5f4a52549f319f4528ac36e5\"},{\"name\":\"7.62x54R\",\"longName\":\"7.62x54mmR\",\"key\":\"5f4a52549f319f4528ac36f3\"},{\"name\":\"9x18\",\"longName\":\"9x18mm Makarov\",\"key\":\"5f4a52549f319f4528ac3700\"},{\"name\":\"9x19\",\"longName\":\"9x19mm Parabellum\",\"key\":\"5f4a52549f319f4528ac371d\"},{\"name\":\"9x21\",\"longName\":\"9x21mm Gyurza\",\"key\":\"5f4a52549f319f4528ac3732\"},{\"name\":\"9x39\",\"longName\":\"9x39mm\",\"key\":\"5f4a52549f319f4528ac373b\"}]}")
            )
        )

        //Fetch and active.
        Firebase.remoteConfig.fetchAndActivate()
    }
}