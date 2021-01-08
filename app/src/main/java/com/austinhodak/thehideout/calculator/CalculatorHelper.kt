package com.austinhodak.thehideout.calculator

import android.util.Log
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.calculator.models.CArmor
import kotlin.math.max

object CalculatorHelper {

    fun simulateHit(ammo: CAmmo, armor: CArmor): Double {
        var ammoDamage = ammo.damage
        var ammoPen = ammo.penetration
        var damageToArmor = 0.0
        //var armorDurability = armor.durability

        var blocked = false

        Log.d("DURABILITY", armor.durability.toString())

        if (armor.durability > 0.0) {
            val num = (armor.durability / armor.maxDurability) * 100.0
            val num3 = (121.0 - 5000.0 / (45.0 + num * 2.0)) * armor.resistance * 0.01

            if (simulateBlock(ammo, armor)) {
                val num00 = (ammo.penetration / armor.resistance).coerceIn(0.6, 1.1)
                damageToArmor = ammo.penetration * ammo.armorDamage * num00 * armor.destructibility
                ammoDamage *= armor.bluntThroughput * (1.0 - 0.03 * (num3 - ammo.penetration).coerceIn(0.2, 1.0))
                blocked = true
            } else {
                damageToArmor = ammoPen * ammo.armorDamage * (ammoPen / armor.resistance).coerceIn(0.5, 0.9) * armor.destructibility
                val num4 = (ammoPen / (num3 + 12.0).coerceIn(0.6, 1.0))
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
            var num = (armor.durability / armor.maxDurability) * 100.0

            var num3 = (121.0 - 5000.0 / (45.0 + num * 2.0)) * armor.resistance * 0.01

            var num4 = if (num >= ammo.penetration + 15) {
                0.0
            } else {
                if (!(num3 >= ammo.penetration)) {
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

    private fun _shotsToKill(bullet: CAmmo, armor: CArmor, health: Double, blowthrough: Double): Double {
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
    }

     fun shotsToKill(bullet: CAmmo, armor: CArmor, health: Double, simulations: Int, blowthrough: Double): Double {
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
    }
}