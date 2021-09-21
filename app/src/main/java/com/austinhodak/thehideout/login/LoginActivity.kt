package com.austinhodak.thehideout.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme

class LoginActivity : GodActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {
                Scaffold {

                }
            }
        }
    }
}