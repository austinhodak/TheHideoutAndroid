package com.austinhodak.thehideout

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.skydoves.only.Only
import timber.log.Timber
import timber.log.Timber.DebugTree

class Application : Application() {
    private val TAG = "APPLICATION: onCREATE"

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

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            Log.d(TAG, token)

        })

        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }
}