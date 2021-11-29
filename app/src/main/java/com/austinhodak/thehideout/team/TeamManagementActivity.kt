package com.austinhodak.thehideout.team

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamManagementActivity : GodActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {
                Scaffold(
                    topBar = {
                        TopBar()
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = {
                                   Text(text = "JOIN TEAM", color = Color.Black)
                            },
                            onClick = {

                            },
                            icon = {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_link_24), contentDescription = "", tint = Color.Black)
                            }
                        )
                    }
                ) {

                }
            }
        }
    }

    @Composable
    private fun TopBar() {
        TopAppBar(
            title = { Text("Team Management") },
            navigationIcon = {
                IconButton(onClick = {
                    onBackPressed()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
            actions = {
                IconButton(onClick = {

                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_add_circle_24), contentDescription = "", tint = Color.White)
                }
            }
        )
    }
}