package com.austinhodak.tarkovapi

import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.datastore.DataStoreStorage

object UserSettingsModel : SettingsModel(DataStoreStorage(name = "user")) {

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

    val playerLevel by intPref(71, "playerLevel")

    val mapMarkerCategories by intSetPref(emptySet(), "mapMarkerCategories")

    val playerIGN by stringPref(key = "playerIGN")

    val fleaVisiblePrice by enumPref(FleaVisiblePrice.AVG, "fleaVisiblePrice")

    // Sorting and filtering
    val fleaSort by intPref(2, "fleaSort")
    val ammoSort by intPref(0, "ammoSort")
    val gearSort by intPref(0, "gearSort")
    val keySort by intPref(0, "keySort")
    val craftSort by intPref(0, "craftSort")
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