package com.austinhodak.thehideout.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.tarkovapi.Levels
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.datastore.DataStoreStorage
import com.michaelflisar.materialpreferences.preferencescreen.*
import com.michaelflisar.materialpreferences.preferencescreen.choice.singleChoice
import com.michaelflisar.materialpreferences.preferencescreen.classes.asIcon
import com.michaelflisar.materialpreferences.preferencescreen.input.input
import com.michaelflisar.text.asText
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsActivity : GodActivity() {

    lateinit var screen: PreferenceScreen

    var currentScreen = "Settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {

                var toolbarTitle by remember { mutableStateOf("Settings") }

                var isSignedIn by remember {
                    mutableStateOf(false)
                }

                FirebaseAuth.AuthStateListener {
                    isSignedIn = it.currentUser != null && it.currentUser?.isAnonymous == false
                }

                isSignedIn = FirebaseAuth.getInstance().currentUser != null && FirebaseAuth.getInstance().currentUser?.isAnonymous == false

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(toolbarTitle) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressed()
                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
                        )
                    }
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            val recyclerView = RecyclerView(context)
                            recyclerView.layoutManager = LinearLayoutManager(context)

                            PreferenceScreenConfig.apply {
                                alignIconsWithBackArrow = true

                            }

                            screen = screen {
                                onScreenChanged = { subScreenStack, stateRestored ->
                                    val breadcrumbs = subScreenStack.joinToString(" > ") { it.title.get(this@SettingsActivity) }
                                    toolbarTitle = if (breadcrumbs.isBlank()) "Settings" else breadcrumbs
                                    currentScreen = if (breadcrumbs.isBlank()) "Settings" else breadcrumbs
                                }
                                state = savedInstanceState
                                input(UserSettingsModel.playerLevel) {
                                    title = "Player Level".asText()
                                    summary = "Level %s".asText()
                                    hint = "Level".asText()
                                }
                                subScreen {
                                    title = "Traders".asText()
                                    icon = R.drawable.ic_baseline_person_24.asIcon()
                                    category {
                                        title = "Trader Levels".asText()
                                    }
                                    singleChoice(UserSettingsModel.praporLevel, Levels.values(), { "Level $it" }) {
                                        title = "Prapor".asText()
                                    }
                                    singleChoice(UserSettingsModel.therapistLevel, Levels.values(), { "Level $it" }) {
                                        title = "Therapist".asText()
                                    }
                                    singleChoice(UserSettingsModel.fenceLevel, Levels.values(), { "Level $it" }) {
                                        title = "Fence".asText()
                                        enabled = false
                                    }
                                    singleChoice(UserSettingsModel.skierLevel, Levels.values(), { "Level $it" }) {
                                        title = "Skier".asText()
                                    }
                                    singleChoice(UserSettingsModel.peacekeeperLevel, Levels.values(), { "Level $it" }) {
                                        title = "Peacekeeper".asText()
                                    }
                                    singleChoice(UserSettingsModel.mechanicLevel, Levels.values(), { "Level $it" }) {
                                        title = "Mechanic".asText()
                                    }
                                    singleChoice(UserSettingsModel.ragmanLevel, Levels.values(), { "Level $it" }) {
                                        title = "Ragman".asText()
                                    }
                                    singleChoice(UserSettingsModel.jaegerLevel, Levels.values(), { "Level $it" }) {
                                        title = "Jaeger".asText()
                                    }
                                }
                                /*subScreen {
                                    title = "Flea Market".asText()
                                    icon = R.drawable.ic_baseline_storefront_24.asIcon()
                                }*/
                                subScreen {
                                    title = "Display".asText()
                                    icon = R.drawable.ic_baseline_wb_sunny_24.asIcon()
                                    switch(UserSettingsModel.keepScreenOn) {
                                        title = "Keep Screen On".asText()
                                        icon = R.drawable.ic_baseline_screen_lock_portrait_24.asIcon()
                                    }
                                }
                                /*subScreen {
                                    title = "Hideout".asText()
                                    //icon = R.drawable.icons8_tent_96.asIcon()
                                }*/
                                /*subScreen {
                                    title = "Quests".asText()
                                    //icon = R.drawable.ic_baseline_assignment_24.asIcon()
                                }*/
                                button {
                                    title = "Log out".asText()
                                    icon = R.drawable.ic_baseline_logout_24.asIcon()
                                    enabled = isSignedIn
                                    onClick = {
                                        Firebase.auth.signOut()
                                        Toast.makeText(this@SettingsActivity, "Logged out!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                category {
                                    title = "About".asText()
                                }
                                button {
                                    title = "The Hideout".asText()
                                    summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})".asText()
                                    icon = R.drawable.ic_baseline_code_24.asIcon()
                                    enabled = false
                                }
                            }

                            screen.bind(recyclerView)
                            recyclerView
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (currentScreen == "Settings") {
            super.onBackPressed()
        } else {
            screen.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        screen.onSaveInstanceState(outState)

    }
}

