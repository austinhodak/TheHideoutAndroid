package com.austinhodak.thehideout.weapons.mods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import javax.inject.Inject

class ModDetailActivity : GodActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modID = intent.getStringExtra("id") ?: ""

        setContent {
            HideoutTheme {

            }
        }
    }
}
