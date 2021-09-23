package com.austinhodak.thehideout.firebase

import java.io.Serializable

data class WeaponBuildFirestore(
        val ammo: String? = null,
        val weapon: _Weapon? = null,
        val uid: String? = null,
        val name: String? = null,
        var id: String? = null,
        val stats: _Stats? = null,
        val mods: HashMap<String, String>? = null
) : Serializable {
    data class _Weapon (
            val id: String? = null,
            val name: String? = null,
            val shortName: String? = null
    ) : Serializable

    data class _Stats (
            val ergonomics: Int? = null,
            val verticalRecoil: Int? = null,
            val horizontalRecoil: Int? = null,
            val velocity: Int? = null,
            val weight: Double? = null,
            val costRoubles: Int? = null,
    ) : Serializable
}
