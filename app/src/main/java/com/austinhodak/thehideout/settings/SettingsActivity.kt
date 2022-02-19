package com.austinhodak.thehideout.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.InputType
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import coil.annotation.ExperimentalCoilApi
import com.adapty.Adapty
import com.afollestad.materialdialogs.MaterialDialog
import com.austinhodak.tarkovapi.*
import com.austinhodak.thehideout.*
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.profile.UserProfileActivity
import com.austinhodak.thehideout.team.TeamManagementActivity
import com.austinhodak.thehideout.utils.*
import com.austinhodak.thehideout.workmanager.PriceUpdateFactory
import com.austinhodak.thehideout.workmanager.PriceUpdateWorker
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.michaelflisar.materialpreferences.preferencescreen.*
import com.michaelflisar.materialpreferences.preferencescreen.choice.singleChoice
import com.michaelflisar.materialpreferences.preferencescreen.classes.asBatch
import com.michaelflisar.materialpreferences.preferencescreen.classes.asIcon
import com.michaelflisar.materialpreferences.preferencescreen.dependencies.Dependency
import com.michaelflisar.materialpreferences.preferencescreen.dependencies.asDependency
import com.michaelflisar.materialpreferences.preferencescreen.input.input
import com.michaelflisar.text.asText
import dagger.hilt.android.AndroidEntryPoint
import io.gleap.Gleap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import javax.inject.Inject


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

    @Inject
    lateinit var myWorkerFactory: PriceUpdateFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("tarkov", MODE_PRIVATE)

        UserSettingsModel.openingScreen.observe(lifecycleScope) {
            Timber.d(it.toString())
            when (it) {
                OpeningScreen.AMMO -> {
                    extras.setOpeningItem(101, "ammunition/{caliber}")
                }
                OpeningScreen.KEYS -> {
                    extras.setOpeningItem(104, "keys")
                }
                OpeningScreen.FLEA -> {
                    extras.setOpeningItem(107, "flea")
                }
                OpeningScreen.HIDEOUT -> {
                    extras.setOpeningItem(108, "hideout")
                }
                OpeningScreen.QUESTS -> {
                    extras.setOpeningItem(109, "quests")
                }
                OpeningScreen.LOADOUTS -> {
                    extras.setOpeningItem(115, "weaponloadouts")
                }
                OpeningScreen.MODS -> {
                    extras.setOpeningItem(114, "weaponmods")
                }
                OpeningScreen.WEAPONS -> {
                    extras.setOpeningItem(301, "weapons/{classID}")
                }
                OpeningScreen.NEEDED_ITEMS -> {
                    extras.setOpeningItem(116, "neededGrid")
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
                } catch (e: Exception) {
                    Firebase.crashlytics.recordException(e)
                    null
                }

                val roomUpdatesTime = DateUtils.getRelativeTimeSpanString(
                    preferences.getLong("lastPriceUpdate", 0),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )

                val scaffoldState = rememberScaffoldState()

                Scaffold(
                    scaffoldState = scaffoldState,
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
                                    title = getString(R.string.player_profile).asText()
                                    icon = R.drawable.ic_baseline_person_24.asIcon()
                                    input(UserSettingsModel.playerIGN) {
                                        title = getString(R.string.in_game_name).asText()
                                        summary = "%s".asText()
                                        hint = "Name".asText()
                                    }
                                    input(UserSettingsModel.discordName) {
                                        title = getString(R.string.discord_name).asText()
                                        summary = "%s".asText()
                                        hint = "Username#0000".asText()
                                    }
                                    input(UserSettingsModel.playerLevel) {
                                        title = getString(R.string.player_level).asText()
                                        summary = "Level %s".asText()
                                        hint = "Level".asText()
                                    }
                                    button {
                                        title = getString(R.string.traders).asText()
                                        onClick = {
                                            openActivity(UserProfileActivity::class.java)
                                        }
                                    }
                                    /*subScreen {
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
                                    }*/
                                    singleChoice(UserSettingsModel.userGameEdition, GameEdition.values(), {
                                        when (it) {
                                            GameEdition.STANDARD -> "Standard Edition"
                                            GameEdition.LEFT_BEHIND -> "Left Behind Edition"
                                            GameEdition.PREPARE_FOR_ESCAPE -> "Prepare for Escape Edition"
                                            GameEdition.EDGE_OF_DARKNESS -> "Edge of Darkness Limited Edition"
                                        }
                                    }) {
                                        title = getString(R.string.game_edition).asText()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                    category {
                                        title = getString(R.string.mouse_settings).asText()
                                    }
                                    input(UserSettingsModel.dpi) {
                                        title = getString(R.string.mouse_dpi).asText()
                                        summary = "%s".asText()
                                        hint = "DPI".asText()
                                    }
                                    input(UserSettingsModel.hipfireSens) {
                                        title = getString(R.string.in_game_mouse_sens).asText()
                                        summary = "%s".asText()
                                        hint = "Mouse Sens".asText()
                                        textInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                    }
                                    input(UserSettingsModel.aimSens) {
                                        title = getString(R.string.in_game_aim_sens).asText()
                                        summary = "%s".asText()
                                        hint = "Aim Sens".asText()
                                        textInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                    }
                                    category {
                                        title = getString(R.string.other).asText()
                                    }
                                    button {
                                        title = getString(R.string.log_out).asText()
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
                                        title = getString(R.string.reset).asText()
                                        icon = R.drawable.ic_baseline_settings_backup_restore_24.asIcon()
                                        button {
                                            title = getString(R.string.reset_hideout_progress).asText()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Reset Hideout Progress?")
                                                    message(text = "Are you sure?")
                                                    positiveButton(text = "RESET") {
                                                        userFirestore()?.update("progress.hideoutModules", FieldValue.delete())
                                                        userFirestore()?.update("progress.hideoutObjectives", FieldValue.delete())
                                                        userRefTracker("hideoutModules").removeValue()
                                                        userRefTracker("hideoutObjectives").removeValue()
                                                    }
                                                    negativeButton(text = "NEVERMIND")
                                                }
                                            }
                                        }
                                        button {
                                            title = getString(R.string.reset_quest_progress).asText()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Reset Quest Progress?")
                                                    message(text = "Are you sure?")
                                                    positiveButton(text = "RESET") {
                                                        userFirestore()?.update("progress.quests", FieldValue.delete())
                                                        userFirestore()?.update("progress.questObjectives", FieldValue.delete())
                                                        userRefTracker("questObjectives").removeValue()
                                                        userRefTracker("quests").removeValue()
                                                    }
                                                    negativeButton(text = "NEVERMIND")
                                                }
                                            }
                                        }
                                        button {
                                            title = getString(R.string.reset_all_progress).asText()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Reset All Progress?")
                                                    message(text = "This is typically used after a wipe or reset.\n\nTeam settings will not be affected.")
                                                    positiveButton(text = "RESET") {
                                                        userFirestore()?.update("progress.quests", FieldValue.delete())
                                                        userFirestore()?.update("progress.questObjectives", FieldValue.delete())
                                                        userFirestore()?.update("progress.hideoutModules", FieldValue.delete())
                                                        userFirestore()?.update("progress.hideoutObjectives", FieldValue.delete())
                                                        userFirestore()?.update("keys", FieldValue.delete())

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
                                button {
                                    title = getString(R.string.team_management).asText()
                                    //summary = "Join, leave, and manage your teams.".asText()
                                    icon = R.drawable.ic_baseline_group_24.asIcon()
                                    onClick = {
                                        openActivity(TeamManagementActivity::class.java)
                                    }
                                }
                                category {
                                    title = getString(R.string.settings).asText()
                                }
                                subScreen {
                                    title = getString(R.string.data_sync).asText()
                                    icon = R.drawable.ic_baseline_update_24.asIcon()
                                    val updateTime = if (isWorkRunning(this@SettingsActivity, "price_update")) {
                                        "Updating now..."
                                    } else roomUpdatesTime.toString()
                                    button {
                                        title = "Last Price Sync".asText()
                                        summary = updateTime.asText()
                                        icon = R.drawable.ic_baseline_access_time_24.asIcon()
                                        enabled = false
                                    }
                                    singleChoice(UserSettingsModel.dataSyncFrequency, DataSyncFrequency.values(), {
                                        when (it) {
                                            DataSyncFrequency.`60` -> "1 Hour"
                                            DataSyncFrequency.`120` -> "2 Hour"
                                            DataSyncFrequency.`360` -> "6 Hour"
                                            DataSyncFrequency.`720` -> "12 Hour"
                                            DataSyncFrequency.`1440` -> "1 Day"
                                            else -> ""
                                        }
                                    }) {
                                        title = getString(R.string.sync_frequency).asText()
                                        icon = R.drawable.ic_baseline_update_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                    button {
                                        title = getString(R.string.sync_now).asText()
                                        summary = "".asText()
                                        icon = R.drawable.ic_baseline_sync_24.asIcon()
                                        //badge = "PRO".asBatch()
                                        onClick = {
                                            Toast.makeText(this@SettingsActivity, "Updating...", Toast.LENGTH_SHORT).show()
                                            val priceUpdateRequestTest = OneTimeWorkRequest.Builder(
                                                PriceUpdateWorker::class.java
                                            ).build()

                                            WorkManager.getInstance(this@SettingsActivity).enqueue(priceUpdateRequestTest)
                                        }
                                    }
                                    category {
                                        title = getString(R.string.data_provided_by).asText()
                                    }
                                    button {
                                        title = "Tarkov Tools".asText()
                                        summary = "Created by kokarn".asText()
                                        icon = R.drawable.ic_icons8_github.asIcon()
                                        onClick = {
                                            "https://tarkov-tools.com/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Tarkov Data".asText()
                                        summary = "Maintained by Community Devs".asText()
                                        icon = R.drawable.ic_icons8_github.asIcon()
                                        onClick = {
                                            "https://github.com/TarkovTracker/tarkovdata/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                }
                                subScreen {
                                    title = getString(R.string.display).asText()
                                    icon = R.drawable.ic_baseline_wb_sunny_24.asIcon()
                                    switch(UserSettingsModel.keepScreenOn) {
                                        title = getString(R.string.keep_screen_on).asText()
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
                                            OpeningScreen.NEEDED_ITEMS -> "Needed Items Grid"
                                            else -> ""
                                        }
                                    }) {
                                        title = getString(R.string.opening_screen).asText()
                                        icon = R.drawable.ic_baseline_open_in_browser_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                    category {
                                        title = getString(R.string.other).asText()
                                    }
                                    if (!isPremium() || isDebug())
                                        switch(UserSettingsModel.hidePremiumBanner) {
                                            title = getString(R.string.hide_premium_banner).asText()
                                            icon = R.drawable.ic_baseline_visibility_off_24.asIcon()
                                        }
                                    singleChoice(UserSettingsModel.languageSetting, LanguageSetting.values(), {
                                        when (it) {
                                            LanguageSetting.ENGLISH -> "English"
                                            LanguageSetting.FRENCH -> "French"
                                            LanguageSetting.GERMAN -> "German"
                                            LanguageSetting.POLISH -> "Polish"
                                            LanguageSetting.RUSSIAN -> "Russian"
                                            LanguageSetting.TURKISH -> "Turkish"
                                            else -> ""
                                        }
                                    }) {
                                        title = getString(R.string.language).asText()
                                        icon = R.drawable.ic_baseline_language_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                }
                                subScreen {
                                    title = getString(R.string.flea_market).asText()
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
                                        title = getString(R.string.list_price).asText()
                                        icon = R.drawable.ic_baseline_money_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                    singleChoice(UserSettingsModel.fleaIconDisplay, IconSelection.values(), {
                                        when (it) {
                                            IconSelection.ORIGINAL -> "Original"
                                            IconSelection.TRANSPARENT -> "Transparent"
                                            IconSelection.GAME -> "Same as in game"
                                        }
                                    }) {
                                        title = getString(R.string.icon_display).asText()
                                        icon = R.drawable.ic_baseline_image_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                    singleChoice(UserSettingsModel.fleaHideTime, FleaHideTime.values(), {
                                        when (it) {
                                            FleaHideTime.NONE -> "Don't Hide"
                                            FleaHideTime.HOUR24 -> "Within last 24 Hours"
                                            FleaHideTime.DAY7 -> "Within last 7 Days"
                                            FleaHideTime.DAY14 -> "Within last 14 Days"
                                            FleaHideTime.DAY30 -> "Within last 30 Days"
                                        }
                                    }) {
                                        title = getString(R.string.only_show_items_scanned).asText()
                                        icon = R.drawable.ic_baseline_access_time_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                    switch(UserSettingsModel.fleaHideNonFlea) {
                                        title = getString(R.string.hide_banned_from_flea).asText()
                                        icon = R.drawable.ic_baseline_block_24.asIcon()
                                    }
                                    /*subScreen {
                                        title = "Colors".asText()
                                        icon = R.drawable.ic_baseline_color_lens_24.asIcon()
                                        category {
                                            title = "Colors".asText()
                                        }
                                        color(UserSettingsModel.itemColorBlue) {
                                            title = "Blue".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with blue background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorGrey) {
                                            title = "Grey".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with grey background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorRed) {
                                            title = "Red".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with red background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorOrange) {
                                            title = "Orange".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with orange background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorDefault) {
                                            title = "Default".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with default background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorViolet) {
                                            title = "Violet".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with violet background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorYellow) {
                                            title = "Yellow".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with yellow background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorGreen) {
                                            title = "Green".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with green background.".asText()
                                            supportsAlpha = false
                                        }
                                        color(UserSettingsModel.itemColorBlack) {
                                            title = "Black".asText()
                                            icon = R.drawable.ic_blank.asIcon()
                                            summary = "Items with black background.".asText()
                                            supportsAlpha = false
                                        }
                                    }*/
                                }
                                subScreen {
                                    title = getString(R.string.maps).asText()
                                    icon = R.drawable.ic_baseline_map_24.asIcon()
                                    singleChoice(UserSettingsModel.defaultMap, MapEnums.values(), {
                                        it.id
                                    }) {
                                        title = getString(R.string.default_map).asText()
                                        icon = R.drawable.ic_baseline_map_24.asIcon()
                                        showCheckBoxes = true
                                        bottomSheet = true
                                    }
                                }
                                subScreen {
                                    title = getString(R.string.notifications).asText()
                                    icon = R.drawable.ic_baseline_notifications_active_24.asIcon()
                                    category {
                                        title = getString(R.string.server_status).asText()
                                    }
                                    switch(UserSettingsModel.serverStatusNotifications) {
                                        title = "Show Notifications".asText()
                                    }
                                    switch(UserSettingsModel.serverStatusUpdates) {
                                        title = "Service Status Updates".asText()
                                        summary = "Will notify when a new status update is posted.".asText()
                                        dependsOn = UserSettingsModel.serverStatusNotifications.asDependency()
                                    }
                                    switch(UserSettingsModel.serverStatusMessages) {
                                        title = "New Status Messages".asText()
                                        summary = "Will notify when a new status message is received.".asText()
                                        dependsOn = UserSettingsModel.serverStatusNotifications.asDependency()
                                    }
                                    if (Build.VERSION.SDK_INT >= 26)
                                        button {
                                            title = "Notification Settings".asText()
                                            onClick = {
                                                openNotificationSettings("SERVER_STATUS")
                                            }
                                        }
                                    category {
                                        title = "Trader Restock".asText()
                                    }
                                    switch(UserSettingsModel.globalRestockAlert) {
                                        title = "Show Notifications".asText()
                                    }
                                    if (Build.VERSION.SDK_INT >= 26)
                                        button {
                                            title = "Notification Settings".asText()
                                            onClick = {
                                                openNotificationSettings("TRADER_RESTOCK")
                                            }
                                        }
                                    category {
                                        title = "Price Alerts".asText()
                                    }
                                    switch(UserSettingsModel.priceAlertsGlobalNotifications) {
                                        title = "Show Notifications".asText()
                                    }
                                    if (Build.VERSION.SDK_INT >= 26)
                                        button {
                                            title = "Notification Settings".asText()
                                            onClick = {
                                                openNotificationSettings("PRICE_ALERTS")
                                            }
                                        }
                                }
                                category {
                                    title = getString(R.string.integrations_beta).asText()
                                }
                                subScreen {
                                    title = "Tarkov Tracker".asText()
                                    icon = R.drawable.ic_baseline_link_24.asIcon()
                                    //summary = "Coming soon.".asText()
                                    badge = "PRO".asBatch()
                                    if (isPremium() || isDebug()) {
                                        button {
                                            title = "How to Setup".asText()
                                            icon = R.drawable.ic_baseline_info_24.asIcon()
                                            onClick = {
                                                MaterialDialog(this@SettingsActivity).show {
                                                    title(text = "Instructions")
                                                    message(text = "1. Go to TarkovTracker.io and login or create an account. \n\n2. Go to Settings. \n\n3. In the TarkovTracker API section, enter a name for the token, make sure all permissions are checked and create the token. \n\n4. Next to the token click the QR code, then click Scan QR Code below.")
                                                    positiveButton(text = "GO") {
                                                        "https://tarkovtracker.io/settings/".openWithCustomTab(this@SettingsActivity)
                                                    }
                                                    negativeButton(text = "CANCEL")
                                                    neutralButton(text = "SCAN QR") {
                                                        val scanOptions = ScanOptions()
                                                        scanOptions.setPrompt("Scan QR code from Tarkov Tracker API settings.")
                                                        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                                        scanOptions.setBeepEnabled(false)
                                                        scanOptions.setBarcodeImageEnabled(false)
                                                        scanOptions.setOrientationLocked(false)
                                                        scanOptions.addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
                                                        barcodeLauncher.launch(scanOptions)
                                                    }
                                                }
                                            }
                                        }
                                        input(UserSettingsModel.ttAPIKey) {
                                            title = "API Token".asText()
                                            hint = "Enter API key here.".asText()
                                            icon = R.drawable.ic_baseline_vpn_key_24.asIcon()
                                            summary = "".asText()
                                            textInputType = InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                                        }
                                        button {
                                            title = "Scan QR Code".asText()
                                            icon = R.drawable.ic_baseline_qr_code_scanner_24.asIcon()
                                            onClick = {
                                                val scanOptions = ScanOptions()
                                                scanOptions.setPrompt("Scan QR code from Tarkov Tracker API settings.")
                                                scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                                scanOptions.setBeepEnabled(false)
                                                scanOptions.setBarcodeImageEnabled(false)
                                                scanOptions.setOrientationLocked(false)
                                                scanOptions.addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
                                                barcodeLauncher.launch(scanOptions)
                                            }
                                        }
                                        category {
                                            title = "Sync Settings".asText()
                                        }
                                        switch(UserSettingsModel.ttSync) {
                                            title = "Sync Your Progress".asText()
                                            summary = "Automatic sync temporarily disabled, please sync manually below.".asText()
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
                                            dependsOn = object : Dependency<String> {
                                                override val setting = UserSettingsModel.ttAPIKey
                                                override suspend fun isEnabled(): Boolean {
                                                    val value = setting.flow.first()
                                                    return value.isNotEmpty()
                                                }
                                            }
                                        }
                                        switch(UserSettingsModel.ttSyncHideout) {
                                            title = "Sync Hideout Progress".asText()
                                            dependsOn = object : Dependency<String> {
                                                override val setting = UserSettingsModel.ttAPIKey
                                                override suspend fun isEnabled(): Boolean {
                                                    val value = setting.flow.first()
                                                    return value.isNotEmpty()
                                                }
                                            }
                                        }
                                        button {
                                            title = "Sync Now".asText()
                                            summary = "".asText()
                                            icon = R.drawable.ic_baseline_sync_24.asIcon()
                                            dependsOn = object : Dependency<String> {
                                                override val setting = UserSettingsModel.ttAPIKey
                                                override suspend fun isEnabled(): Boolean {
                                                    val value = setting.flow.first()
                                                    return value.isNotEmpty()
                                                }
                                            }
                                            onClick = {
                                                lifecycleScope.launch {
                                                    if (UserSettingsModel.ttSync.value && UserSettingsModel.ttAPIKey.value.isNotEmpty()) {
                                                        scaffoldState.snackbarHostState.showSnackbar("Sync starting! This may take a while.")
                                                        syncTT(lifecycleScope, ttRepository)
                                                    } else {
                                                        scaffoldState.snackbarHostState.showSnackbar("Sync turned off, please turn on first.")
                                                    }
                                                }
                                            }
                                        }
                                        category {
                                            title = "Overwrite".asText()
                                        }
                                        button {
                                            title = "Push".asText()
                                            summary = "Will overwrite any data on Tarkov Tracker.".asText()
                                            icon = R.drawable.ic_baseline_backup_24.asIcon()
                                            dependsOn = object : Dependency<String> {
                                                override val setting = UserSettingsModel.ttAPIKey
                                                override suspend fun isEnabled(): Boolean {
                                                    val value = setting.flow.first()
                                                    return value.isNotEmpty()
                                                }
                                            }
                                            onClick = {
                                                try {
                                                    lifecycleScope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar("Pushing to TarkovTracker! This may take a while.")
                                                        fsUser.value?.let {
                                                            it.pushToTT(lifecycleScope, ttRepository)
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Firebase.crashlytics.recordException(e)
                                                }
                                            }
                                        }
                                        button {
                                            title = "Pull".asText()
                                            summary = "Will overwrite any data on app.".asText()
                                            icon = R.drawable.ic_baseline_cloud_download_24.asIcon()
                                            dependsOn = object : Dependency<String> {
                                                override val setting = UserSettingsModel.ttAPIKey
                                                override suspend fun isEnabled(): Boolean {
                                                    val value = setting.flow.first()
                                                    return value.isNotEmpty()
                                                }
                                            }
                                            onClick = {
                                                try {
                                                    lifecycleScope.launch {
                                                        val test = ttRepository.getUserProgress()

                                                        if (test.isSuccessful) {
                                                            test.body()?.pushToDB()
                                                            UserSettingsModel.playerLevel.update(test.body()?.level ?: return@launch)

                                                            scaffoldState.snackbarHostState.showSnackbar("Sync completed!")
                                                        } else {
                                                            scaffoldState.snackbarHostState.showSnackbar("Error, please check API key and try again.")
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Firebase.crashlytics.recordException(e)
                                                }
                                            }
                                        }
                                    } else {
                                        button {
                                            title = "Not Premium".asText()
                                            icon = R.drawable.ic_baseline_stars_24.asIcon()
                                            summary = "Tarkov Tracker integration is a premium feature, please upgrade to enable.".asText()
                                            onClick = {
                                                launchPremiumPusher()
                                            }
                                        }
                                    }
                                }
                                category {
                                    title = getString(R.string.about).asText()
                                }
                                if (Firebase.remoteConfig.getBoolean("gleap_enabled")) {
                                    button {
                                        title = "Send Feedback".asText()
                                        summary = "Take a screenshot, try it!".asText()
                                        icon = R.drawable.ic_baseline_feedback_24.asIcon()
                                        onClick = {
                                            Gleap.getInstance().startFeedbackFlow()
                                        }
                                    }
                                }
                                subScreen {
                                    title = getString(R.string.socials).asText()
                                    icon = R.drawable.ic_icons8_discord_svg.asIcon()
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
                                        title = "Battlestate Games".asText()
                                    }
                                    button {
                                        title = "Twitch".asText()
                                        icon = R.drawable.ic_icons8_twitch.asIcon()
                                        onClick = {
                                            "https://www.twitch.tv/battlestategames".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Twitter".asText()
                                        icon = R.drawable.ic_icons8_twitter.asIcon()
                                        onClick = {
                                            "https://twitter.com/bstategames".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    category {
                                        title = "Community Tools".asText()
                                    }
                                    button {
                                        title = "Tarkov Tracker".asText()
                                        //icon = R.drawable.ic_round_language_24.asIcon()
                                        onClick = {
                                            "https://tarkovtracker.io/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Tarkov Tools".asText()
                                        //icon = R.drawable.ic_round_language_24.asIcon()
                                        onClick = {
                                            "https://tarkov-tools.com/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Rat Scanner".asText()
                                        //icon = R.drawable.ic_round_language_24.asIcon()
                                        //summary = "Rat Scanner allows you to scan items and provides you with realtime data about them.".asText()
                                        onClick = {
                                            "https://ratscanner.com/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "EFT Dev Tracker".asText()
                                        //icon = R.drawable.ic_round_language_24.asIcon()
                                        //summary = "Rat Scanner allows you to scan items and provides you with realtime data about them.".asText()
                                        onClick = {
                                            "https://developertracker.com/escape-from-tarkov/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Tarkov Guru".asText()
                                        //icon = R.drawable.ic_round_language_24.asIcon()
                                        //summary = "Rat Scanner allows you to scan items and provides you with realtime data about them.".asText()
                                        onClick = {
                                            "https://tarkov.guru".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                }
                                subScreen {
                                    title = getString(R.string.more_about).asText()
                                    icon = R.drawable.ic_baseline_info_24.asIcon()
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
                                        onClick = {
                                            "https://escapefromtarkov.fandom.com/wiki/Changelog".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Last Wipe".asText()
                                        summary = (wipeDate ?: "").asText()
                                        icon = R.drawable.icons8_toilet_paper_24.asIcon()
                                        enabled = false
                                    }
                                    button {
                                        title = "Data Pulled".asText()
                                        summary = "Jan 19, 2022".asText()
                                        icon = R.drawable.ic_baseline_access_time_24.asIcon()
                                        enabled = false
                                    }
                                    button {
                                        title = "Translations".asText()
                                        summary = "Click to contribute!".asText()
                                        icon = R.drawable.ic_baseline_translate_24.asIcon()
                                        enabled = true
                                        onClick = {
                                            "https://localazy.com/p/the-hideout".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Open Source Licenses".asText()
                                        icon = R.drawable.ic_baseline_source_24.asIcon()
                                        enabled = true
                                        onClick = {
                                            startActivity(Intent(this@SettingsActivity, OssLicensesMenuActivity::class.java))
                                        }
                                    }
                                    button {
                                        title = "Icons from Icons8".asText()
                                        icon = R.drawable.ic_icons8_icons8.asIcon()
                                        onClick = {
                                            "https://icons8.com/".openWithCustomTab(this@SettingsActivity)
                                        }
                                    }
                                    button {
                                        title = "Restore Purchases".asText()
                                        icon = R.drawable.ic_baseline_settings_backup_restore_24.asIcon()
                                        onClick = {
                                            Adapty.restorePurchases { purchaserInfo, googleValidationResultList, error ->
                                                if (error == null) {
                                                    // successful restore
                                                    Toast.makeText(this@SettingsActivity, "Purchases restored.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
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

    private val barcodeLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {

        } else {
            lifecycleScope.launch {
                UserSettingsModel.ttAPIKey.update(result.contents)
                Toast.makeText(this@SettingsActivity, "API Token saved, please reload page.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

