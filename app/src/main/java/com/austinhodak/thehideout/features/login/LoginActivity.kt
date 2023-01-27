package com.austinhodak.thehideout.features.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class LoginActivity : GodActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            HideoutTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setStatusBarColor(
                    Color.Transparent,
                    darkIcons = false
                )

                Scaffold {
                    Column {
                        Image(painter = painterResource(id = R.drawable.bulb), contentDescription = "")
                        //Image(painter = painterResource(id = R.drawable.email_button), contentDescription = "", modifier = Modifier.width(200.dp))
                    }
                }
            }
        }
    }
}