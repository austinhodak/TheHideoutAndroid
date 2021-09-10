package com.austinhodak.thehideout

import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Weapon

data class WeaponBuild (
    var id: String? = null,
    var parentWeapon: Weapon? = null,
    var name: String? = null,
    var creator: String? = null,
    var mods: MutableMap<String, BuildMod>? = HashMap()
) {
    data class BuildMod (
        var id: String? = null,
        var mods: Map<String, BuildMod>? = null,
        var item: Item? = null
    )
}
