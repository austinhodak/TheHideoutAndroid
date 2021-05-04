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

object CalculatorHelper {

    fun getCharacters(context: Context): List<Character> {
        val groupListType: Type = object : TypeToken<ArrayList<Character?>?>() {}.type
        return Gson().fromJson(context.resources.openRawResource(R.raw.calc_characters).bufferedReader().use { it.readText() }, groupListType)
    }

    fun simulateHit(ammo: CAmmo, armor: CArmor): Double {
        var ammoDamage = ammo.damage
        var ammoPen = ammo.penetration
        var damageToArmor = 0.0

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

        //Log.d("AMMO", ammoDamage.toString())

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
                if (!(num3 >= ammo.penetration)) {
                    (100.0 + ammo.penetration / (0.9 * num3 - ammo.penetration))
                } else {
                    (0.4 * (num3 - ammo.penetration - 15.0) * (num3 - ammo.penetration - 15.0))
                }
            }

            if (num4 - Math.random() * 100.0 < 0.0) {
                //Timber.d("Blocked!")
                return true
            }
        }
        //Timber.d("Not Blocked!")
        return false
    }

    fun penChance(ammo: CAmmo, armor: CArmor): Double {
        var timesPenned = 0

        for (i in 1..10000) {
            if (!simulateBlock(ammo, armor)) {
                timesPenned++
            }
        }

        return timesPenned.toDouble() / 100
    }

    /*private fun _shotsToKill(bullet: CAmmo, armor: CArmor, health: Double, blowthrough: Double): Double {
        var h = health
        var headHealth = 35.0
        var shotCount = 0.0

        for (i in 1..100) {
            shotCount++

            for (b in 1..bullet.bullets) {
                val bulletDamage = simulateHit(bullet, armor)
                //Log.d("AMMOTEST", "BULLET DAMAGE: $bulletDamage")
                h -= bulletDamage
            }

            if (h <= 0.0) {
                if (blowthrough == 0.0) {
                    return shotCount
                } else {
                    val num = (35 * blowthrough * (1.0/6.0))
                    headHealth -= num
                    h = 0.0
                    if (headHealth <= 0) {
                        return shotCount
                    }
                }
            }
        }

        return Double.POSITIVE_INFINITY
    }*/

    /*fun shotsToKill(bullet: CAmmo, armor: CArmor, health: Double, simulations: Int, blowthrough: Double): Double {
        var avg = 0.0

        for (i in 1..simulations) {

            val armorC = CArmor(
                95.0,
                2,
                0.30,
                50.0,
                50.0,
                20.0,
                0.250
            )

            val ct = _shotsToKill(bullet, armorC, 70.0, 1.5)
            //Log.d("AMMOTEST", "$ct")
            avg += ct
        }

        return avg/simulations
    }*/

    fun clamp(num: Double, a: Double, b: Double): Double {
        return Math.max(a, Math.min(b, num))
    }

}