package com.austinhodak.thehideout.data.enums

import java.util.Collections.singletonList

enum class ItemType(
    val rawValue: String,
) {
    ammo("ammo"),
    ammoBox("ammoBox"),
    any("any"),
    armor("armor"),
    backpack("backpack"),
    barter("barter"),
    container("container"),
    glasses("glasses"),
    grenade("grenade"),
    gun("gun"),
    headphones("headphones"),
    helmet("helmet"),
    injectors("injectors"),
    keys("keys"),
    markedOnly("markedOnly"),
    meds("meds"),
    mods("mods"),
    noFlea("noFlea"),
    pistolGrip("pistolGrip"),
    preset("preset"),
    provisions("provisions"),
    rig("rig"),
    suppressor("suppressor"),
    wearable("wearable"),
    /**
     * Auto generated constant for unknown enum values
     */
    UNKNOWN__("UNKNOWN__");

    companion object {
        fun safeValueOf(rawValue: String): ItemType = ItemType.values()
            .find { it.rawValue == rawValue }
            ?: ItemType.UNKNOWN__

        fun ItemType.asString(): String = rawValue
    }
}