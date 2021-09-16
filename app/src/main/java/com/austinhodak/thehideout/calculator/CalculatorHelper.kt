package com.austinhodak.thehideout.calculator

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.calculator.models.CArmor
import com.austinhodak.thehideout.calculator.models.Character
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.math.max
import kotlin.math.min

object CalculatorHelper {

    fun getCharacters(context: Context): List<Character> {
        val groupListType: Type = object : TypeToken<ArrayList<Character?>?>() {}.type
        return Gson().fromJson(context.resources.openRawResource(R.raw.calc_characters).bufferedReader().use { it.readText() }, groupListType)
    }

    fun simulateHit(ammo: CAmmo, armor: CArmor): Double {
        var ammoDamage = ammo.damage
        var ammoPen = ammo.penetration
        var damageToArmor: Double

        var blocked = false

        if (armor.durability > 0.0) {
            val num = (armor.durability / armor.maxDurability) * 100.0
            val num3 = (121.0 - 5000.0 / (45.0 + num * 2.0)) * armor.resistance * 0.01

            if (simulateBlock(ammo, armor)) {
                damageToArmor = ammo.penetration * ammo.armorDamage * clamp(ammo.penetration / armor.resistance, 0.6, 1.1) * armor.destructibility
                ammoDamage *= armor.bluntThroughput * clamp(1.0 - 0.03 * (num3 - ammo.penetration),0.2, 1.0)
                blocked = true
            } else {
                damageToArmor = ammoPen * ammo.armorDamage * clamp(ammoPen / armor.resistance, 0.5, 0.9) * armor.destructibility
                val num4 = clamp(ammoPen / (num3 + 12.0), 0.6, 1.0)
                ammoDamage *= num4
                ammoPen *= num4
            }

            damageToArmor = max(1.0, damageToArmor)

            armor.durability -= damageToArmor
            if (armor.durability < 0.0) {
                armor.durability = 0.0
            }
        }

        if (blocked) {
            return ammoDamage
        }

        return ammoDamage
    }

    private fun simulateBlock(ammo: CAmmo, armor: CArmor): Boolean {
        if (armor.durability > 0.0) {
            val num = (armor.durability / armor.maxDurability) * 100.0

            val num3 = (121.0 - 5000.0 / (45.0 + num * 2.0)) * armor.resistance * 0.01

            val num4 = if (num3 >= ammo.penetration + 15.0) {
                0.0
            } else {
                if (num3 < ammo.penetration) {
                    (100.0 + ammo.penetration / (0.9 * num3 - ammo.penetration))
                } else {
                    (0.4 * (num3 - ammo.penetration - 15.0) * (num3 - ammo.penetration - 15.0))
                }
            }

            if (num4 - Math.random() * 100.0 < 0.0) {
                return true
            }
        }

        return false
    }

    fun penChance(ammo: CAmmo, armor: CArmor?): Double {
        var timesPenned = 0

        for (i in 1..10000) {
            if (!simulateBlock(ammo, armor ?: CArmor())) {
                timesPenned++
            }
        }

        return timesPenned.toDouble() / 100
    }

    fun clamp(num: Double, a: Double, b: Double): Double {
        return max(a, min(b, num))
    }

}