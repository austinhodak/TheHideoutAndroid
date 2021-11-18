package com.austinhodak.thehideout.quests.inraid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.navigation.compose.rememberNavController
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuestInRaidActivity : GodActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {

                val scaffoldState = rememberScaffoldState()
                val navController = rememberNavController()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        AmmoDetailToolbar(
                            title = "In Raid at Customs",
                            onBackPressed = {
                                finish()
                            }
                        )
                    }
                ) {

                }

            }
        }
    }
}