package com.austinhodak.thehideout

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skydoves.only.Only
import timber.log.Timber
import timber.log.Timber.DebugTree

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        Firebase.database.setPersistenceEnabled(true)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        Firebase.auth.signInAnonymously().addOnSuccessListener {
            Timber.d("UID: ${it.user?.uid}")
        }

        Only.init(applicationContext)
    }
}