package com.austinhodak.thehideout.settings

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.Levels
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.NavActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.billing.PremiumActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.utils.restartNavActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.michaelflisar.materialpreferences.preferencescreen.*
import com.michaelflisar.materialpreferences.preferencescreen.choice.singleChoice
import com.michaelflisar.materialpreferences.preferencescreen.classes.asIcon
import com.michaelflisar.materialpreferences.preferencescreen.input.input
import com.michaelflisar.text.asText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
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

                val gameInfo = JSONObject(FirebaseRemoteConfig.getInstance()["game_info"].asString())
                val wipeDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val date = LocalDate.parse(gameInfo.getString("wipe_date"), DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                    val now = LocalDate.now()

                    val between = ChronoUnit.DAYS.between(date, now)

                    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    "${date.format(formatter)} ($between Days)"
                } else {
                    gameInfo.getString("wipe_date")
                }

                val versionDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val date = LocalDate.parse(gameInfo.getString("version_date"), DateTimeFormatter.ofPattern("MM-dd-yyyy"))

                    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    "${date.format(formatter)}"
                } else {
                    gameInfo.getString("version_date")
                }

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
                                subScreen {
                                    title = "Player Profile".asText()
                                    icon = R.drawable.ic_baseline_person_24.asIcon()
                                    input(UserSettingsModel.playerIGN) {
                                        title = "In Game Name".asText()
                                        summary = "%s".asText()
                                        hint = "Name".asText()
                                    }
                                    input(UserSettingsModel.playerLevel) {
                                        title = "Player Level".asText()
                                        summary = "Level %s".asText()
                                        hint = "Level".asText()
                                    }
                                    subScreen {
                                        title = "Traders".asText()
                                        //icon = R.drawable.ic_baseline_person_24.asIcon()
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
                                        restartNavActivity()
                                    }
                                }
                                category {
                                    title = "Premium & Donations".asText()
                                }
                                button {
                                    title = "Upgrade or Donate".asText()
                                    icon = R.drawable.ic_baseline_upgrade_24.asIcon()
                                    onClick = {
                                        context.openActivity(PremiumActivity::class.java)
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
                                button {
                                    title = "Game Version".asText()
                                    summary = "${gameInfo.getString("version")} ($versionDate)".asText()
                                    icon = R.drawable.ic_baseline_info_24.asIcon()
                                    enabled = false
                                    onClick= {
                                        "https://escapefromtarkov.fandom.com/wiki/Changelog".openWithCustomTab(this@SettingsActivity)
                                    }
                                }
                                button {
                                    title = "Last Wipe".asText()
                                    summary = wipeDate.asText()
                                    icon = R.drawable.icons8_toilet_paper_24.asIcon()
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

