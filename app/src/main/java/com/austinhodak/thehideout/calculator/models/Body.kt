package com.austinhodak.thehideout.calculator.models

import android.util.Log
import android.widget.TextView
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.views.HealthBar
import kotlin.math.roundToInt

data class Body (
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

    fun reset(): Body {
        head.health = 35.0
        thorax.health = 85.0
        stomach.health = 70.0
        leftArm.health = 60.0
        rightArm.health = 60.0
        leftLeg.health = 65.0
        rightLeg.health = 65.0

        shotsFired = 0
        shotsFiredAfterDead = 0

        updateHealthBars()
        return this
    }

    fun getTotalHealth(): Int {
        return (head.health + thorax.health + stomach.health + leftArm.health + rightArm.health + leftLeg.health + rightLeg.health).coerceAtLeast(0.0).roundToInt()
    }
    
    fun getTotalInitialHealth(): Int {
        return (head.initialHealth + thorax.initialHealth + stomach.initialHealth + leftArm.initialHealth + rightArm.initialHealth + leftLeg.initialHealth + rightLeg.initialHealth).coerceAtLeast(0.0).roundToInt()
    }

    fun shoot (part: Part, ammo: CAmmo, armor: CArmor? = CArmor()): Body {
        shotsFired ++
        when (part) {
            Part.HEAD -> {
                head.health -= sim.simulateHit(ammo, armor!!)
            }
            Part.THORAX -> {
                thorax.health -= sim.simulateHit(ammo, armor!!)
            }
            Part.STOMACH -> {
                if (stomach.blacked()) doBlowthrough(stomach)
                stomach.health -= sim.simulateHit(ammo, armor!!)
            }
            Part.LEFTARM -> {
                if (leftArm.blacked()) doBlowthrough(leftArm)
                leftArm.health -= sim.simulateHit(ammo, armor!!)
            }
            Part.RIGHTARM -> {
                if (rightArm.blacked()) doBlowthrough(rightArm)
                rightArm.health -= sim.simulateHit(ammo, armor!!)
            }
            Part.LEFTLEG -> {
                if (leftLeg.blacked()) doBlowthrough(leftLeg)
                leftLeg.health -= sim.simulateHit(ammo, armor!!)
            }
            Part.RIGHTLEG -> {
                if (rightLeg.blacked()) doBlowthrough(rightLeg)
                rightLeg.health -= sim.simulateHit(ammo, armor!!)
            }
        }

        coerceAll()

        if (head.health <= 0.0 || thorax.health <= 0.0) {
            shotsFiredAfterDead ++
            dead()
        }

        updateHealthBars()

        return this
    }

    private fun doBlowthrough(bodyPart: BodyPart) {
        head.health -= (head.initialHealth * bodyPart.blowthrough * (1/6.0))
        thorax.health -= (thorax.initialHealth * bodyPart.blowthrough * (1/6.0))
        stomach.health -= (stomach.initialHealth * bodyPart.blowthrough * (1/6.0))
        leftArm.health -= (leftArm.initialHealth * bodyPart.blowthrough * (1/6.0))
        rightArm.health -= (rightArm.initialHealth * bodyPart.blowthrough * (1/6.0))
        leftLeg.health -= (leftLeg.initialHealth * bodyPart.blowthrough * (1/6.0))
        rightLeg.health -= (rightLeg.initialHealth * bodyPart.blowthrough * (1/6.0))
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
        leftLeg.health = leftLeg.health .coerceAtLeast(0.0)
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
        currentHealthTV.text = "${getTotalHealth()}"
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

data class BodyPart (
    var health: Double,
    var blowthrough: Double,
    var initialHealth: Double = health,
    var healthBar: HealthBar? = null
) {
    fun blacked(): Boolean {
        return health <= 0.0
    }

    fun toHB(): HealthBar.Health {
        return HealthBar.Health(health, initialHealth)
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