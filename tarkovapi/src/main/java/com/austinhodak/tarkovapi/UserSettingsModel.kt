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

    val playerLevel by intPref(71, "playerLevel")

    val mapMarkerCategories by intSetPref(emptySet(), "mapMarkerCategories")
}

enum class Levels {
    `1`,
    `2`,
    `3`,
    `4`
}