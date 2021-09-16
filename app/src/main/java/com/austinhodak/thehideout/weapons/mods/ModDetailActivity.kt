package com.austinhodak.thehideout.weapons.mods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.austinhodak.thehideout.compose.theme.HideoutTheme

class ModDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HideoutTheme {

            }
        }
    }
}
