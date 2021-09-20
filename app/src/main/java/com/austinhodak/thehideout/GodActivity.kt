package com.austinhodak.thehideout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.austinhodak.thehideout.settings.UserSettingsModel
import com.austinhodak.thehideout.utils.keepScreenOn

open class GodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserSettingsModel.keepScreenOn.observe(lifecycleScope) { keepOn ->
            keepScreenOn(keepOn)
        }
    }

}