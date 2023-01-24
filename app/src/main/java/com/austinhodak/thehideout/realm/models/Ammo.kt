package com.austinhodak.thehideout.realm.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Ammo : RealmObject {
    @PrimaryKey
    var id: String? = null
    var item: Item? = null
    var weight: Double = 0.0
    var caliber: String? = null
    var stackMaxSize: Int = 0
    var tracer: Boolean = false
    var tracerColor: String? = null
    var ammoType: String? = null
    var projectileCount: Int? = null
    var damage: Int = 0
    var armorDamage: Int = 0
    var fragmentationChance: Double = 0.0
    var ricochetChance: Double = 0.0
    var penetrationChance: Double = 0.0
    var penetrationPower: Int = 0
    var accuracyModifier: Double? = null
    var recoilModifier: Double? = null
    var initialSpeed: Double? = null
    var lightBleedModifier: Double = 0.0
    var heavyBleedModifier: Double = 0.0
}