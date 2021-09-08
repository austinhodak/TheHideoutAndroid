package com.austinhodak.thehideout.weapons.mods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.austinhodak.thehideout.weapons.mods.ui.theme.TheHideoutTheme

class ModDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheHideoutTheme {

            }
        }
    }
}
