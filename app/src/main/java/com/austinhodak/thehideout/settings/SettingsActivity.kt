package com.austinhodak.thehideout.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.settings.ui.settingstraderlevel.SettingsTraderLevelFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TheHideout)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsTraderLevelFragment.newInstance())
                .commitNow()
        }
    }
}