package com.austinhodak.thehideout.realm.converters

import com.austinhodak.thehideout.AmmoQuery
import com.austinhodak.thehideout.realm.models.Ammo
import io.realm.kotlin.MutableRealm

fun AmmoQuery.Data.Ammo.toRealmAmmo(realm: MutableRealm): Ammo {
    val fragment = this
    val ammo = Ammo()
    ammo.apply {
        id = fragment.item.id
        item = findObjectById(realm, fragment.item.id)
        weight = fragment.weight
        caliber = fragment.caliber
        stackMaxSize = fragment.stackMaxSize
        tracer = fragment.tracer
        tracerColor = fragment.tracerColor
        ammoType = fragment.ammoType
        projectileCount = fragment.projectileCount
        damage = fragment.damage
        armorDamage = fragment.armorDamage
        fragmentationChance = fragment.fragmentationChance
        ricochetChance = fragment.ricochetChance
        penetrationChance = fragment.penetrationChance
        penetrationPower = fragment.penetrationPower
        accuracyModifier = fragment.accuracyModifier
        recoilModifier = fragment.recoilModifier
        initialSpeed = fragment.initialSpeed
        lightBleedModifier = fragment.lightBleedModifier
        heavyBleedModifier = fragment.heavyBleedModifier
    }
    return ammo
}