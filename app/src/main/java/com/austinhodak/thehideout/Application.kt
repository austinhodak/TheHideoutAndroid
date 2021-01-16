package com.austinhodak.thehideout

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        Firebase.auth.signInAnonymously().addOnSuccessListener {
            Log.d("FIREBASE_USER", "UID: ${it.user?.uid}")
        }
    }
}