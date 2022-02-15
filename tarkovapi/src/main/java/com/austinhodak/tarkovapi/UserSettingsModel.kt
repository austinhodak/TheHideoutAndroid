package com.austinhodak.tarkovapi

import android.graphics.Color
import com.austinhodak.tarkovapi.utils.Maps
import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.datastore.DataStoreStorage
import java.util.*


object UserSettingsModel : SettingsModel(DataStoreStorage(name = "user")) {

    val ttAPIKey by stringPref("", "ttAPIKey")
    val ttSync by boolPref(false, "ttSync")
    val ttSyncQuest by boolPref(false, "ttSyncQuest")
    val ttSyncHideout by boolPref(false, "ttSyncHideout")

    val test by boolPref(true, "test")

    val keepScreenOn by boolPref(false, "keepScreenOn")
    val hidePremiumBanner by boolPref(false, "hidePremiumBanner")

    val praporLevel by enumPref(Levels.`4`, "praporLevel")
    val therapistLevel by enumPref(Levels.`4`, "therapistLevel")
    val fenceLevel by enumPref(Levels.`1`, "fenceLevel")
    val skierLevel by enumPref(Levels.`4`, "skierLevel")
    val peacekeeperLevel by enumPref(Levels.`4`, "peacekeeperLevel")
    val mechanicLevel by enumPref(Levels.`4`, "mechanicLevel")
    val ragmanLevel by enumPref(Levels.`4`, "ragmanLevel")
    val jaegerLevel by enumPref(Levels.`4`, "jaegerLevel")

    val openingScreen by enumPref(OpeningScreen.FLEA, "openingScreen")

    val defaultMap by enumPref(MapEnums.CUSTOMS, "defaultMap")

    val fleaHideTime by enumPref(FleaHideTime.NONE, "fleaHideTime")
    val fleaIconDisplay by enumPref(IconSelection.ORIGINAL, "fleaIconDisplay")
    val fleaHideNonFlea by boolPref(false, "fleaHideNonFlea")

    val playerLevel by intPref(71, "playerLevel")

    val mapMarkerCategories by intSetPref(emptySet(), "mapMarkerCategories")

    val mapQuestSelection by stringSetPref(setOf("Active", "Locked", "Completed"), "mapQuestSelection")

    val playerIGN by stringPref(key = "playerIGN")
    val discordName by stringPref(key = "discordName")

    val fleaVisiblePrice by enumPref(FleaVisiblePrice.DEFAULT, "fleaVisiblePrice")

    // Sorting and filtering
    val fleaSort by intPref(2, "fleaSort")
    val ammoSort by intPref(0, "ammoSort")
    val gearSort by intPref(0, "gearSort")
    val keySort by intPref(0, "keySort")
    val craftSort by intPref(0, "craftSort")

    val dpi by intPref(400, "userDPI")
    val hipfireSens by stringPref("0.50", "hipfireSens")
    val aimSens by stringPref("0.50", "aimSens")

    val dataSyncFrequency by enumPref(DataSyncFrequency.`60`, "dataSyncFrequency")
    val dataSyncFrequencyPrevious by enumPref(DataSyncFrequency.`60`, "dataSyncFrequencyPrevious")

    val showStatusOnHomeScreen by boolPref(true, "showStatusOnHomeScreen")

    val serverStatusNotifications by boolPref(false, "serverStatusNotifications")
    val serverStatusUpdates by boolPref(true, "serverStatusUpdates")
    val serverStatusMessages by boolPref(true, "serverStatusMessages")

    val praporRestockAlert by boolPref(false, "praporRestockAlert")
    val therapistRestockAlert by boolPref(false, "therapistRestockAlert")
    val skierRestockAlert by boolPref(false, "skierRestockAlert")
    val peacekeeperRestockAlert by boolPref(false, "peacekeeperRestockAlert")
    val mechanicRestockAlert by boolPref(false, "mechanicRestockAlert")
    val ragmanRestockAlert by boolPref(false, "ragmanRestockAlert")
    val jaegerRestockAlert by boolPref(false, "jaegerRestockAlert")
    val globalRestockAlert by boolPref(false, "globalRestockAlert")
    val globalRestockAlertAppOpen by boolPref(true, "globalRestockAlertAppOpen")

    val modPickerShowAvailable by boolPref(false, "modPickerShowAvailable")

    val priceAlertsGlobalNotifications by boolPref(true, "priceAlertsGlobalNotifications")

    val isPremiumUser by boolPref(false, "isPremiumUser")

    val userGameEdition by enumPref(GameEdition.STANDARD, "userGameEdition")

    val itemColorBlue by intPref(Color.parseColor("#222A2F"))
    val itemColorGrey by intPref(Color.parseColor("#1B1C1C"))
    val itemColorRed by intPref(Color.parseColor("#311B18"))
    val itemColorOrange by intPref(Color.parseColor("#201813"))
    val itemColorDefault by intPref(Color.parseColor("#383A3A"))
    val itemColorViolet by intPref(Color.parseColor("#261F29"))
    val itemColorYellow by intPref(Color.parseColor("#313122"))
    val itemColorGreen by intPref(Color.parseColor("#181F11"))
    val itemColorBlack by intPref(Color.parseColor("#181919"))

    val traderRestockTime by enumPref(TraderRestockTime.`1`, "traderRestockTime")

    val languageSetting by enumPref(LanguageSetting.ENGLISH, "language")
}

enum class LanguageSetting (val locale: Locale) {
    ENGLISH (Locale.ENGLISH),
    FRENCH (Locale.FRENCH),
    GERMAN (Locale.GERMAN),
    POLISH (Locale.forLanguageTag("PL")),
    RUSSIAN (Locale.forLanguageTag("RU")),
    TURKISH  (Locale.forLanguageTag("TR")),
}

enum class TraderRestockTime {
    `1`,
    `2`,
    `5`,
    `10`,
    `15`,
    `30`
}

enum class DataSyncFrequency {
    `60`,
    `120`,
    `360`,
    `720`,
    `1440`
}

enum class OpeningScreen {
    AMMO,
    FLEA,
    KEYS,
    HIDEOUT,
    QUESTS,
    WEAPONS,
    LOADOUTS,
    MODS,
    NEEDED_ITEMS
}

enum class FleaVisiblePrice {
    DEFAULT,
    LAST,
    LOW,
    AVG,
    HIGH
}

enum class FleaHideTime {
    NONE,
    HOUR24,
    DAY7,
    DAY14,
    DAY30
}

enum class Levels {
    `1`,
    `2`,
    `3`,
    `4`
}

enum class IconSelection {
    ORIGINAL,
    TRANSPARENT,
    GAME
}

enum class GameEdition {
    STANDARD,
    LEFT_BEHIND,
    PREPARE_FOR_ESCAPE,
    EDGE_OF_DARKNESS
}

enum class MapEnums(var id: String, var int: Int, var icon: Int) {
    FACTORY("Factory", 0, R.drawable.icons8_factory_breakdown_96),
    CUSTOMS("Customs", 1, R.drawable.icons8_structural_96),
    WOODS("Woods", 2, R.drawable.icons8_forest_96),
    SHORELINE("Shoreline", 3, R.drawable.icons8_bay_96),
    INTERCHANGE("Interchange", 4, R.drawable.icons8_shopping_mall_96),
    RESERVE("Reserve", 6, R.drawable.icons8_knight_96),
    THELAB("Labs", 5, R.drawable.icons8_laboratory_96),
    LIGHTHOUSE("Lighthouse", 7, R.drawable.icons8_lighthouse_96),
}