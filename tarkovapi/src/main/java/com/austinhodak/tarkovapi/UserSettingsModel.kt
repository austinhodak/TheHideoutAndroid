package com.austinhodak.tarkovapi

import com.austinhodak.tarkovapi.utils.Maps
import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.datastore.DataStoreStorage


object UserSettingsModel : SettingsModel(DataStoreStorage(name = "user")) {

    val ttAPIKey by stringPref("", "ttAPIKey")
    val ttSync by boolPref(true, "ttSync")
    val ttSyncQuest by boolPref(true, "ttSyncQuest")
    val ttSyncHideout by boolPref(true, "ttSyncHideout")

    val test by boolPref(true, "test")

    val keepScreenOn by boolPref(false, "keepScreenOn")

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
    val dataSyncFrequencyPrevious by enumPref(DataSyncFrequency.`120`, "dataSyncFrequencyPrevious")

    val showStatusOnHomeScreen by boolPref(true, "showStatusOnHomeScreen")

    val serverStatusNotifications by boolPref(false, "serverStatusNotifications")
    val serverStatusUpdates by boolPref(true, "serverStatusUpdates")
    val serverStatusMessages by boolPref(true, "serverStatusMessages")
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
    MODS
}

enum class FleaVisiblePrice {
    DEFAULT,
    LAST,
    LOW,
    AVG,
    HIGH
}

enum class Levels {
    `1`,
    `2`,
    `3`,
    `4`
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