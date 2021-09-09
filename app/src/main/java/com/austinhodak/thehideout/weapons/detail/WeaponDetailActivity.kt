package com.austinhodak.thehideout.weapons.detail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.weapons.viewmodel.WeaponDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeaponDetailActivity : AppCompatActivity() {

    private val weaponViewModel: WeaponDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weaponID = intent.getStringExtra("weaponID") ?: "5bb2475ed4351e00853264e3"
        weaponViewModel.getWeapon(weaponID)

        setContent {
            HideoutTheme {
                val weapon by weaponViewModel.weaponDetails.observeAsState()
                val scaffoldState = rememberScaffoldState()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            AmmoDetailToolbar(
                                title = weapon?.pricing?.name ?: "Error Loading...",
                                onBackPressed = { finish() }
                            )
                            if (weapon == null) {
                                LinearProgressIndicator(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                    backgroundColor = Color.Transparent
                                )
                            }
                        }
                    }
                ) {
                    Image(
                        //rememberImagePainter("https://cdn.tarkov-market.com/loadouts/images/${weapon?.ShortName?.replace(" ", "_")?.lowercase()}/default.jpg"),
                        rememberImagePainter(weapon?.pricing?.gridImageLink),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}