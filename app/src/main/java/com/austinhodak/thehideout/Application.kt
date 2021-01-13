package com.austinhodak.thehideout

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)


    }
}