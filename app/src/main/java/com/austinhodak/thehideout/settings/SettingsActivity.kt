package com.austinhodak.thehideout.settings

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.annotation.ExperimentalCoilApi
import com.afollestad.materialdialogs.MaterialDialog
import com.austinhodak.tarkovapi.FleaVisiblePrice
import com.austinhodak.tarkovapi.Levels
import com.austinhodak.tarkovapi.OpeningScreen
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.*
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.billing.PremiumActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.team.TeamManagementActivity
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.utils.restartNavActivity
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.michaelflisar.materialpreferences.preferencescreen.*
import com.michaelflisar.materialpreferences.preferencescreen.choice.singleChoice
import com.michaelflisar.materialpreferences.preferencescreen.classes.asIcon
import com.michaelflisar.materialpreferences.preferencescreen.dependencies.Dependency
import com.michaelflisar.materialpreferences.preferencescreen.dependencies.asDependency
import com.michaelflisar.materialpreferences.preferencescreen.input.input
import com.michaelflisar.text.asText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
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

        UserSettingsModel.openingScreen.observe(lifecycleScope) {
            Timber.d(it.toString())
            when (it) {
                OpeningScreen.AMMO -> {
                    questPrefs.setOpeningItem(101, "ammunition/{caliber}")
                }
                OpeningScreen.KEYS -> {
                    questPrefs.setOpeningItem(104, "keys")
                }
                OpeningScreen.FLEA -> {
                    questPrefs.setOpeningItem(107, "flea")
                }
                OpeningScreen.HIDEOUT -> {
                    questPrefs.setOpeningItem(108, "hideout")
                }
                OpeningScreen.QUESTS -> {
                    questPrefs.setOpeningItem(109, "quests")
                }
                OpeningScreen.LOADOUTS -> {
                    questPrefs.setOpeningItem(115, "weaponloadouts")
                }
                OpeningScreen.MODS -> {
                    questPrefs.setOpeningItem(114, "weaponmods")
                }
                OpeningScreen.WEAPONS -> {
                    questPrefs.setOpeningItem(301, "assaultRifle")
                }
            }
        }
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
                
                val gameInfo = try {
                    JSONObject(FirebaseRemoteConfig.getInstance()["game_info"].asString())
                } catch (e: Exception) {
                    Firebase.crashlytics.recordException(e)
                    null
                }

                val wipeDate = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val date = LocalDate.parse(gameInfo?.getString("wipe_date"), DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                        val now = LocalDate.now()

                        val between = ChronoUnit.DAYS.between(date, now)

                        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        "${date.format(formatter)} ($between Days Ago)"
                    } else {
                        gameInfo?.getString("wipe_date")
                    }
                } catch (e: Exception) {
                    Firebase.crashlytics.recordException(e)
                    null
                }

                val versionDate = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val date = LocalDate.parse(gameInfo?.getString("version_date"), DateTimeFormatter.ofPattern("MM-dd-yyyy"))

                        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        date.format(formatter)
                    } else {
                        gameInfo?.getString("version_date")
                    }
                }  catch (e: Exception) {
                    Firebase.crashlytics.recordException(e)
                    null
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
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            actions = {
                                IconButton(onClick = {
                                    openActivity(PremiumActivity::class.java)
                                }) {
                                    Image(painter = painterResource(id = R.drawable.icons8_buy_upgrade_96), contentDescription = "")
                                    //Icon(painter = painterResource(id = R.drawable.icons8_buy_upgrade_96), contentDescription = "", tint = Color.Transparent)
                                }
                            }
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
                                    input(UserSettingsModel.discordName) {
                                        title = "Discord Name".asText()
                                        summary = "%s".asText()
                                        hint = "Username#0000".asText()
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
                                    category {
                                        title = "Mouse Settings".asText()
                                    }
                                    input(UserSettingsModel.dpi) {
                                        title = "Mouse DPI".asText()
                                        summary = "%s".asText()
                                        hint = "DPI".asText()
                                    }
                                    input(UserSettingsModel.hipfireSens) {
                                        title = "In Game Mouse Sensitivity".asText()
                                        summary = "%s".replace("[^0-9.]".toRegex(), "").asText()
                                        hint = "Mouse Sens".asText()
                                    }
                                    input(UserSettingsModel.aimSens) {
                                        title = "In Game Aim Sensitivity".asText()
                                        summary = "%s".replace("[^0-9.]".toRegex(), "").asText()
                                        hint = "Aim Sens".asText()
                                    }
                                    category {
                                        title = "Other".asText()
                                    }
                                    button {
                                        title = "Log out".asText()
                                        icon = R.drawable.ic_baseline_logout_24.asIcon()
                                        enabled = isSignedIn
                                        onClick = {
                                            MaterialDialog(this@SettingsActivity).show {
                                                title(text = "Log Out?")
                                                message(text = "Are you sure? Progress will be saved.")
                                                positiveButton(text = "LOGOUT") {
                                                    Firebase.auth.signOut()
                                                    Toast.makeText(this@SettingsActivity, "Logged out!", Toast.LENGTH_SHORT).show()
                                                    restartNavActivity()
                                                }
                                                negativeButton(text = "NEVERMIND")
                                            }
                                        }
                                    }
                                    subScreen {
                                        title = "Reset".asText()
                                        icon = R.drawable.ic_baseline_settings_backup_restore_24.asIcon()
                                        button {
                                            title = "Reset Hideout Progress".asText()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Reset Hideout Progress?")
                                                    message(text = "Are you sure?")
                                                    positiveButton(text = "RESET") {
                                                        userRefTracker("hideoutModules").removeValue()
                                                        userRefTracker("hideoutObjectives").removeValue()
                                                    }
                                                    negativeButton(text = "NEVERMIND")
                                                }
                                            }
                                        }
                                        button {
                                            title = "Reset Quest Progress".asText()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Reset Quest Progress?")
                                                    message(text = "Are you sure?")
                                                    positiveButton(text = "RESET") {
                                                        userRefTracker("questObjectives").removeValue()
                                                        userRefTracker("quests").removeValue()
                                                    }
                                                    negativeButton(text = "NEVERMIND")
                                                }
                                            }
                                        }
                                        button {
                                            title = "Reset All Progress".asText()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Reset All Progress?")
                                                    message(text = "This is typically used after a wipe or reset.\n\nTeam settings will not be affected.")
                                                    positiveButton(text = "RESET") {
                                                        userRefTracker("hideoutModules").removeValue()
                                                        userRefTracker("hideoutObjectives").removeValue()
                                                        userRefTracker("items").removeValue()
                                                        userRefTracker("keysHave").removeValue()
                                                        userRefTracker("questObjectives").removeValue()
                                                        userRefTracker("quests").removeValue()
                                                    }
                                                    negativeButton(text = "NEVERMIND")
                                                }
                                            }
                                        }
                                    }
                                }
                                subScreen {
                                    title = "Flea Market".asText()
                                    icon = R.drawable.ic_baseline_storefront_24.asIcon()
                                    singleChoice(UserSettingsModel.fleaVisiblePrice, FleaVisiblePrice.values(), {
                                        when (it) {
                                            FleaVisiblePrice.DEFAULT -> "Default (Original Method)"
                                            FleaVisiblePrice.AVG -> "Average 24 Hour Price"
                                            FleaVisiblePrice.HIGH -> "Highest 24 Hour Price"
                                            FleaVisiblePrice.LOW -> "Lowest 24 Hour Price"
                                            FleaVisiblePrice.LAST -> "Last Scanned Price"
                                            else -> ""
                                        }
                                    }) {
                                        title = "List Price".asText()
                                        icon = R.drawable.ic_blank.asIcon()
                                    }
                                }
                                /*subScreen {
                                    title = "Data".asText()
                                    icon = R.drawable.ic_baseline_cloud_download_24.asIcon()
                                }*/
                                subScreen {
                                    title = "Display".asText()
                                    icon = R.drawable.ic_baseline_wb_sunny_24.asIcon()
                                    switch(UserSettingsModel.keepScreenOn) {
                                        title = "Keep Screen On".asText()
                                        icon = R.drawable.ic_baseline_screen_lock_portrait_24.asIcon()
                                    }
                                    singleChoice(UserSettingsModel.openingScreen, OpeningScreen.values(), {
                                        when (it) {
                                            OpeningScreen.AMMO -> "Ammunition"
                                            OpeningScreen.KEYS -> "Keys"
                                            OpeningScreen.LOADOUTS -> "Weapon Loadouts"
                                            OpeningScreen.FLEA -> "Flea Market"
                                            OpeningScreen.HIDEOUT -> "Hideout"
                                            OpeningScreen.QUESTS -> "Quests"
                                            OpeningScreen.MODS -> "Weapon Mods"
                                            OpeningScreen.WEAPONS -> "Weapons"
                                            else -> ""
                                        }
                                    }) {
                                        title = "Opening Screen".asText()
                                        icon = R.drawable.ic_baseline_open_in_browser_24.asIcon()
                                    }
                                }
                                button {
                                    title = "Team Management".asText()
                                    //summary = "Join, leave, and manage your teams.".asText()
                                    icon = R.drawable.ic_baseline_group_24.asIcon()
                                    onClick = {
                                        openActivity(TeamManagementActivity::class.java)
                                    }
                                }
                                /*category {
                                    title = "Integrations".asText()
                                }
                                subScreen {
                                    title = "Tarkov Tracker".asText()
                                    icon = R.drawable.ic_baseline_link_24.asIcon()
                                    button {
                                        title = "How to Setup".asText()
                                        icon = R.drawable.ic_baseline_info_24.asIcon()
                                        onClick = {
                                            MaterialDialog(this@SettingsActivity).show {
                                                title(text = "Instructions")
                                                message(text = "Instructions here.")
                                                positiveButton(text = "GO") {
                                                    "https://tarkovtracker.io/settings/".openWithCustomTab(this@SettingsActivity)
                                                }
                                                negativeButton(text = "CANCEL")
                                            }
                                        }
                                    }
                                    input(UserSettingsModel.ttAPIKey) {
                                        title = "API Key".asText()
                                        hint = "Enter API key here.".asText()
                                        icon = R.drawable.ic_baseline_vpn_key_24.asIcon()
                                        summary = "".asText()
                                        textInputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                                    }
                                    category {
                                        title = "Sync Settings".asText()
                                    }
                                    switch(UserSettingsModel.ttSync) {
                                        title = "Sync Your Progress".asText()
                                        summary = "Syncs on app open and close due to API limitation.".asText()
                                        icon = R.drawable.ic_baseline_cloud_sync_24.asIcon()
                                        dependsOn = object : Dependency<String> {
                                            override val setting = UserSettingsModel.ttAPIKey
                                            override suspend fun isEnabled(): Boolean {
                                                val value = setting.flow.first()
                                                return value.isNotEmpty()
                                            }
                                        }
                                    }
                                    switch(UserSettingsModel.ttSyncQuest) {
                                        title = "Sync Quest Progress".asText()
                                        dependsOn = UserSettingsModel.ttSync.asDependency()
                                    }
                                    switch(UserSettingsModel.ttSyncHideout) {
                                        title = "Sync Hideout Progress".asText()
                                        dependsOn = UserSettingsModel.ttSync.asDependency()
                                    }
                                    button {
                                        title = "Sync Now".asText()
                                        summary = "".asText()
                                        icon = R.drawable.ic_baseline_sync_24.asIcon()
                                        dependsOn = UserSettingsModel.ttSync.asDependency()
                                        onClick = {
                                            Toast.makeText(this@SettingsActivity, "Syncing...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    category {
                                        title = "Overwrite".asText()
                                    }
                                    button {
                                        title = "Push".asText()
                                        summary = "Will overwrite any data on Tarkov Tracker.".asText()
                                        icon = R.drawable.ic_baseline_backup_24.asIcon()
                                        dependsOn = UserSettingsModel.ttSync.asDependency()
                                        onClick = {

                                        }
                                    }
                                    button {
                                        title = "Pull".asText()
                                        summary = "Will overwrite any data on app.".asText()
                                        icon = R.drawable.ic_baseline_cloud_download_24.asIcon()
                                        dependsOn = UserSettingsModel.ttSync.asDependency()
                                        onClick = {

                                        }
                                    }
                                }*/
                                /*subScreen {
                                    title = "Hideout".asText()
                                    //icon = R.drawable.icons8_tent_96.asIcon()
                                }*/
                                /*subScreen {
                                    title = "Quests".asText()
                                    //icon = R.drawable.ic_baseline_assignment_24.asIcon()
                                }*/
                                /*category {
                                    title = "Premium & Donations".asText()
                                }
                                button {
                                    title = "Upgrade or Donate".asText()
                                    icon = R.drawable.ic_baseline_attach_money_24.asIcon()
                                    onClick = {

                                    }
                                }*/
                                category {
                                    title = "Join Us On".asText()
                                }
                                button {
                                    title = "Discord".asText()
                                    icon = R.drawable.ic_icons8_discord_svg.asIcon()
                                    onClick = {
                                        "https://discord.gg/YQW36z29z6".openWithCustomTab(this@SettingsActivity)
                                    }
                                }
                                button {
                                    title = "Twitch".asText()
                                    icon = R.drawable.ic_icons8_twitch.asIcon()
                                    onClick = {
                                        "https://www.twitch.tv/theeeelegend".openWithCustomTab(this@SettingsActivity)
                                    }
                                }
                                button {
                                    title = "Twitter".asText()
                                    icon = R.drawable.ic_icons8_twitter.asIcon()
                                    onClick = {
                                        "https://twitter.com/austin6561".openWithCustomTab(this@SettingsActivity)
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
                                    summary = "${gameInfo?.getString("version")} ($versionDate)".asText()
                                    icon = R.drawable.ic_baseline_info_24.asIcon()
                                    enabled = false
                                    onClick= {
                                        "https://escapefromtarkov.fandom.com/wiki/Changelog".openWithCustomTab(this@SettingsActivity)
                                    }
                                }
                                button {
                                    title = "Last Wipe".asText()
                                    summary = (wipeDate ?: "").asText()
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

