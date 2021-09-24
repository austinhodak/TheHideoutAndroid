package com.austinhodak.thehideout.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    data class _Weapon(
        val id: String? = null,
        val name: String? = null,
        val shortName: String? = null
    ) : Serializable

    data class _Stats(
        val ergonomics: Int? = null,
        val verticalRecoil: Int? = null,
        val horizontalRecoil: Int? = null,
        val velocity: Int? = null,
        val weight: Double? = null,
        val costRoubles: Int? = null,
    ) : Serializable

    fun delete() {
        id?.let {
            Firebase.firestore.collection("loadouts").document(it).delete()
        }
    }

    fun updateName(name: String) {
        id?.let {
            Firebase.firestore.collection("loadouts").document(it).update(
                mapOf(
                    "name" to name
                )
            )
        }
    }
}
