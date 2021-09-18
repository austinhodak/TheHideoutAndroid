package com.austinhodak.thehideout.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.InputType
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
import androidx.preference.Preference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.datastore.DataStoreStorage
import com.michaelflisar.materialpreferences.preferencescreen.*
import com.michaelflisar.materialpreferences.preferencescreen.choice.asChoiceListString
import com.michaelflisar.materialpreferences.preferencescreen.choice.singleChoice
import com.michaelflisar.materialpreferences.preferencescreen.classes.asBatch
import com.michaelflisar.materialpreferences.preferencescreen.classes.asIcon
import com.michaelflisar.materialpreferences.preferencescreen.input.input
import com.michaelflisar.text.asText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    lateinit var screen: PreferenceScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {

                var toolbarTitle by remember { mutableStateOf("Settings") }



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
                                    singleChoice(UserSettingsModel.fenceLevel, Levels.values(), { "Level $it" }) {
                                        title = "Fence".asText()
                                        enabled = false
                                    }
                                    singleChoice(UserSettingsModel.skierLevel, Levels.values(), { "Level $it" }) {
                                        title = "Skier".asText()
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
                                subScreen {
                                    title = "Flea Market".asText()
                                    icon = R.drawable.ic_baseline_storefront_24.asIcon()
                                    switch(UserSettingsModel.fleaScreenOn) {
                                        title = "Keep Screen On".asText()
                                        summary = "Screen won't turn off when on Flea Market screens.".asText()
                                        icon = R.drawable.ic_baseline_screen_lock_portrait_24.asIcon()
                                    }
                                }
                                subScreen {
                                    title = "Hideout".asText()
                                    //icon = R.drawable.icons8_tent_96.asIcon()
                                }
                                subScreen {
                                    title = "Quests".asText()
                                    //icon = R.drawable.ic_baseline_assignment_24.asIcon()
                                }
                                button {
                                    title = "Log out".asText()
                                    icon = R.drawable.ic_baseline_logout_24.asIcon()
                                    enabled = false
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

    enum class Levels {
        `1`,
        `2`,
        `3`,
        `4`
    }

    override fun onBackPressed() {
        screen.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        screen.onSaveInstanceState(outState)

    }
}

object UserSettingsModel : SettingsModel(DataStoreStorage(name = "user")) {

    val test by boolPref(true, "test")

    val fleaScreenOn by boolPref(false, "fleaScreenOn")

    val praporLevel by enumPref(SettingsActivity.Levels.`4`, "praporLevel")
    val therapistLevel by enumPref(SettingsActivity.Levels.`4`, "therapistLevel")
    val fenceLevel by enumPref(SettingsActivity.Levels.`1`, "fenceLevel")
    val skierLevel by enumPref(SettingsActivity.Levels.`4`, "skierLevel")
    val peacekeeperLevel by enumPref(SettingsActivity.Levels.`4`, "peacekeeperLevel")
    val mechanicLevel by enumPref(SettingsActivity.Levels.`4`, "mechanicLevel")
    val ragmanLevel by enumPref(SettingsActivity.Levels.`4`, "ragmanLevel")
    val jaegerLevel by enumPref(SettingsActivity.Levels.`4`, "jaegerLevel")

    val playerLevel by intPref(71, "playerLevel")

    val mapMarkerCategories by intSetPref(emptySet(), "mapMarkerCategories")

}