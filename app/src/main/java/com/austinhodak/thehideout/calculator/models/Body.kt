package com.austinhodak.thehideout.calculator.models

import android.widget.TextView
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.calculator.views.HealthBar
import kotlin.math.roundToInt

data class Body(
    var head: BodyPart = BodyPart(35.0, 0.0),
    var thorax: BodyPart = BodyPart(85.0, 0.0),
    var stomach: BodyPart = BodyPart(70.0, 1.5),
    var leftArm: BodyPart = BodyPart(60.0, 0.7),
    var rightArm: BodyPart = BodyPart(60.0, 0.7),
    var leftLeg: BodyPart = BodyPart(65.0, 1.000000000000001),
    var rightLeg: BodyPart = BodyPart(65.0, 1.000000000000001),
) {
    private val sim = CalculatorHelper
    private lateinit var totalHealthTV: TextView
    private lateinit var currentHealthTV: TextView
    private var shotsFired = 0
    private var shotsFiredAfterDead = 0
    var onShootListener: (() -> Unit?)? = null

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

    private fun getTotalHealth(): Int {
        return (head.health + thorax.health + stomach.health + leftArm.health + rightArm.health + leftLeg.health + rightLeg.health).coerceAtLeast(0.0)
            .roundToInt()
    }

    private fun getTotalInitialHealth(): Int {
        return (head.initialHealth + thorax.initialHealth + stomach.initialHealth + leftArm.initialHealth + rightArm.initialHealth + leftLeg.initialHealth + rightLeg.initialHealth).coerceAtLeast(
            0.0
        ).roundToInt()
    }

    fun shoot(part: Part, ammo: CAmmo, cArmor: CArmor? = CArmor()): Body {
        if (getTotalHealth() <= 0) return this

        var armor = cArmor ?: CArmor()

        if (armor.zones.find { it.replace(" ", "").equals(part.name, ignoreCase = true) }.isNullOrEmpty()) {
            //Armor does not cover body part, remove from calculation.
            if (part == Part.HEAD && armor.zones.contains("Top")) {

            } else {
                armor = CArmor()
            }
        }

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
        onShootListener?.invoke()

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

    private fun round() {
        head.health.roundToInt()
        thorax.health.roundToInt()
        stomach.health.roundToInt()
        leftArm.health.roundToInt()
        rightArm.health.roundToInt()
        leftLeg.health.roundToInt()
        rightLeg.health.roundToInt()
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

        if (this::currentHealthTV.isInitialized)
        currentHealthTV.text = "${getTotalHealth()}"

        if (this::totalHealthTV.isInitialized)
        totalHealthTV.text = "/${getTotalInitialHealth()}"

        /*if (shotsFiredAfterDead >= 5) {
            totalHealthTV.text = "He's dead Jim!"
        }*/
    }

    fun bindTotalTextView(view: TextView) {
        totalHealthTV = view
    }

    fun bindCurrentTextView(view: TextView) {
        currentHealthTV = view
    }
}

data class BodyPart(
    var health: Double,
    var blowthrough: Double,
    var initialHealth: Double = health,
    var healthBar: HealthBar? = null,
    var shotCount: Int = 0
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