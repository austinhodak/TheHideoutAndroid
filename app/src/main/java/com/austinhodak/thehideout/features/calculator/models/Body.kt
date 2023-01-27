package com.austinhodak.thehideout.features.calculator.models

import com.austinhodak.thehideout.features.calculator.CalculatorHelper
import com.austinhodak.thehideout.features.calculator.views.HealthBar
import timber.log.Timber
import kotlin.math.roundToInt

data class Body(
    var head: BodyPart = BodyPart(35.0, 0.0, name = Part.HEAD),
    var thorax: BodyPart = BodyPart(85.0, 0.0, name = Part.THORAX),
    var stomach: BodyPart = BodyPart(70.0, 1.5, name = Part.STOMACH),
    var leftArm: BodyPart = BodyPart(60.0, 0.7, name = Part.LEFTARM),
    var rightArm: BodyPart = BodyPart(60.0, 0.7, name = Part.RIGHTARM),
    var leftLeg: BodyPart = BodyPart(65.0, 1.000000000000001, name = Part.LEFTLEG),
    var rightLeg: BodyPart = BodyPart(65.0, 1.000000000000001, name = Part.RIGHTLEG),
) {
    private val sim = CalculatorHelper
    private var shotsFired = 0
    private var shotsFiredAfterDead = 0

    fun reset(selectedCharacter: Character): Body {
        val characterHealth = selectedCharacter.health
        head.health = characterHealth.head.toDouble()
        thorax.health = characterHealth.thorax.toDouble()
        stomach.health = characterHealth.stomach.toDouble()
        leftArm.health = characterHealth.arms.toDouble()
        rightArm.health = characterHealth.arms.toDouble()
        leftLeg.health = characterHealth.legs.toDouble()
        rightLeg.health = characterHealth.legs.toDouble()

        head.reset()
        thorax.reset()
        stomach.reset()
        leftArm.reset()
        rightArm.reset()
        leftLeg.reset()
        rightLeg.reset()

        shotsFired = 0
        shotsFiredAfterDead = 0

        updateHealthBars()
        return this
    }

    fun getTotalHealth(): Int {
        return (head.health + thorax.health + stomach.health + leftArm.health + rightArm.health + leftLeg.health + rightLeg.health).coerceAtLeast(0.0)
            .roundToInt()
    }

    fun getTotalInitialHealth(): Int {
        return (head.initialHealth + thorax.initialHealth + stomach.initialHealth + leftArm.initialHealth + rightArm.initialHealth + leftLeg.initialHealth + rightLeg.initialHealth).coerceAtLeast(
            0.0
        ).roundToInt()
    }

    fun shoot(part: Part, ammo: CAmmo?, cArmor: CArmor? = CArmor()): Body {
        if (ammo == null) return this
        if (getTotalHealth() <= 0) return this

        var armor = cArmor ?: CArmor()

        if (armor.zones.find { it.replace(" ", "").replace("Chest", "Thorax").equals(part.name, ignoreCase = true) }.isNullOrEmpty()) {
            //Armor does not cover body part, remove from calculation.
            /*if (part == Part.HEAD && armor.zones.contains("Top")) {

            } else {
                armor = CArmor()
            }*/
            armor = CArmor()
        }

        Timber.d(armor.toString())

        shotsFired++
        when (part) {
            Part.HEAD -> {
                head.health -= sim.simulateHit(ammo, armor)
                head.shot()
            }
            Part.THORAX -> {
                thorax.health -= sim.simulateHit(ammo, armor)
                thorax.shot()
            }
            Part.STOMACH -> {
                stomach.health -= sim.simulateHit(ammo, armor)
                if (stomach.blacked()) doBlowthrough(stomach, ammo)
                stomach.shot()
            }
            Part.LEFTARM -> {
                leftArm.health -= sim.simulateHit(ammo, armor)
                if (leftArm.blacked()) doBlowthrough(leftArm, ammo)
                leftArm.shot()
            }
            Part.RIGHTARM -> {
                rightArm.health -= sim.simulateHit(ammo, armor)
                if (rightArm.blacked()) doBlowthrough(rightArm, ammo)
                rightArm.shot()
            }
            Part.LEFTLEG -> {
                leftLeg.health -= sim.simulateHit(ammo, armor)
                if (leftLeg.blacked()) doBlowthrough(leftLeg, ammo)
                leftLeg.shot()
            }
            Part.RIGHTLEG -> {
                rightLeg.health -= sim.simulateHit(ammo, armor)
                if (rightLeg.blacked()) doBlowthrough(rightLeg, ammo)
                rightLeg.shot()
            }
        }

        coerceAll()

        if (head.health <= 0.0 || thorax.health <= 0.0) {
            shotsFiredAfterDead++
            dead()
        }

        updateHealthBars()

        return this
    }

    private fun doBlowthrough(bodyPart: BodyPart, ammo: CAmmo) {
        head.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
        thorax.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
        stomach.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
        leftArm.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
        rightArm.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
        leftLeg.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
        rightLeg.health -= (ammo.damage * bodyPart.blowthrough * (1 / 6.0))
    }

    private fun dead() {
        head.health = 0.0
        thorax.health = 0.0
        stomach.health = 0.0
        leftArm.health = 0.0
        rightArm.health = 0.0
        leftLeg.health = 0.0
        rightLeg.health = 0.0
    }

    private fun coerceAll() {
        head.health = head.health.coerceAtLeast(0.0)
        thorax.health = thorax.health.coerceAtLeast(0.0)
        stomach.health = stomach.health.coerceAtLeast(0.0)
        leftArm.health = leftArm.health.coerceAtLeast(0.0)
        rightArm.health = rightArm.health.coerceAtLeast(0.0)
        leftLeg.health = leftLeg.health.coerceAtLeast(0.0)
        rightLeg.health = rightLeg.health.coerceAtLeast(0.0)
    }

    fun linkToHealthBar(part: Part, healthBar: HealthBar) {
        when (part) {
            Part.HEAD -> head.healthBar = healthBar
            Part.THORAX -> thorax.healthBar = healthBar
            Part.STOMACH -> stomach.healthBar = healthBar
            Part.RIGHTARM -> rightArm.healthBar = healthBar
            Part.LEFTARM -> leftArm.healthBar = healthBar
            Part.RIGHTLEG -> rightLeg.healthBar = healthBar
            Part.LEFTLEG -> leftLeg.healthBar = healthBar
        }
    }

    private fun updateHealthBars() {
        head.healthBar?.updateHealth(head.toHB())
        thorax.healthBar?.updateHealth(thorax.toHB())
        stomach.healthBar?.updateHealth(stomach.toHB())
        leftArm.healthBar?.updateHealth(leftArm.toHB())
        rightArm.healthBar?.updateHealth(rightArm.toHB())
        leftLeg.healthBar?.updateHealth(leftLeg.toHB())
        rightLeg.healthBar?.updateHealth(rightLeg.toHB())
    }
}

data class BodyPart(
    var health: Double,
    var blowthrough: Double,
    var initialHealth: Double = health,
    var healthBar: HealthBar? = null,
    var shotCount: Int = 0,
    var name: Part
) {
    fun blacked(): Boolean {
        return health <= 0.0
    }

    fun toHB(): HealthBar.Health {
        return HealthBar.Health(health, initialHealth)
    }

    fun reset() {
        initialHealth = health
        shotCount = 0
        healthBar?.updateShotCount(shotCount)
    }

    fun shot() {
        shotCount ++
        healthBar?.updateShotCount(shotCount)
    }
}

enum class Part {
    HEAD,
    THORAX,
    STOMACH,
    RIGHTARM,
    LEFTARM,
    RIGHTLEG,
    LEFTLEG
}