package com.austinhodak.thehideout.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.Body
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.calculator.models.CArmor
import com.austinhodak.thehideout.calculator.models.Part
import com.austinhodak.thehideout.views.HealthBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.max
import kotlin.math.roundToInt

class CalculatorMainActivity : AppCompatActivity() {
    private lateinit var headBar: HealthBar
    private lateinit var thoraxBar: HealthBar
    private lateinit var stomachBar: HealthBar
    private lateinit var leftArmBar: HealthBar
    private lateinit var rightArmBar: HealthBar
    private lateinit var leftLegBar: HealthBar
    private lateinit var rightLegBar: HealthBar
    private lateinit var totalHealthTV: TextView
    private lateinit var currentHealthTV: TextView
    private var body = Body()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator_main_new)
        setSupportActionBar(findViewById(R.id.toolbar))

        totalHealthTV = findViewById(R.id.healthTotal)
        currentHealthTV = findViewById(R.id.healthCurrentTV)

        val bs = BottomSheetBehavior.from(findViewById<ConstraintLayout>(R.id.bottomSheet))
        bs.skipCollapsed = true

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            if (bs.state == BottomSheetBehavior.STATE_HIDDEN || bs.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bs.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bs.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        headBar = findViewById(R.id.healthHead)
        thoraxBar = findViewById(R.id.healthThorax)
        stomachBar = findViewById(R.id.healthStomach)
        leftArmBar = findViewById(R.id.healthLArm)
        rightArmBar = findViewById(R.id.healthRArm)
        leftLegBar = findViewById(R.id.healthLLeg)
        rightLegBar = findViewById(R.id.healthRLeg)

        body.linkToHealthBar(Part.HEAD, headBar)
        body.linkToHealthBar(Part.THORAX, thoraxBar)
        body.linkToHealthBar(Part.STOMACH, stomachBar)
        body.linkToHealthBar(Part.LEFTARM, leftArmBar)
        body.linkToHealthBar(Part.RIGHTARM, rightArmBar)
        body.linkToHealthBar(Part.LEFTLEG, leftLegBar)
        body.linkToHealthBar(Part.RIGHTLEG, rightLegBar)

        val AP45 = CAmmo(
            1,
            50.0,
            30.0,
            0.37
        )

        val armor = CArmor(
            95.0,
            5,
            0.220,
            95.0,
            95.0,
            50.0,
            0.550
        )

        headBar.setOnClickListener {
            body.shoot(Part.HEAD, AP45)
        }

        thoraxBar.setOnClickListener {
            body.shoot(Part.THORAX, AP45, armor)
        }

        stomachBar.setOnClickListener {
            body.shoot(Part.STOMACH, AP45, armor)
        }

        leftArmBar.setOnClickListener {
            body.shoot(Part.LEFTARM, AP45)
        }

        rightArmBar.setOnClickListener {
            body.shoot(Part.RIGHTARM, AP45)
        }

        leftLegBar.setOnClickListener {
            body.shoot(Part.LEFTLEG, AP45)
        }

        rightLegBar.setOnClickListener {
            body.shoot(Part.RIGHTLEG, AP45)
        }

        body.bindTotalTextView(totalHealthTV)
        body.bindCurrentTextView(currentHealthTV)


        /*headButton = findViewById(R.id.button)
        thoraxButton = findViewById(R.id.button2)
        stomachButton = findViewById(R.id.button3)
        leftArmButton = findViewById(R.id.button4)
        rightArmButton = findViewById(R.id.button5)
        leftLegButton = findViewById(R.id.button6)
        rightLegButton = findViewById(R.id.button7)
        resetButton = findViewById(R.id.button8)



        Log.d("AMMO", CalculatorHelper.shotsToKill(AP45, armor, 70.0, 250, 1.5).toString())

        val body = Body()
        update(body)

        headButton.setOnClickListener {
            update(body.shoot(Part.HEAD, AP45))
        }
        thoraxButton.setOnClickListener {
            update(body.shoot(Part.THORAX, AP45, armor))
        }
        stomachButton.setOnClickListener {
            update(body.shoot(Part.STOMACH, AP45, armor))
        }
        leftArmButton.setOnClickListener {
            update(body.shoot(Part.LEFTARM, AP45))
        }
        rightArmButton.setOnClickListener {
            update(body.shoot(Part.RIGHTARM, AP45))
        }
        leftLegButton.setOnClickListener {
            update(body.shoot(Part.LEFTLEG, AP45))
        }
        rightLegButton.setOnClickListener {
            update(body.shoot(Part.RIGHTLEG, AP45))
        }
        resetButton.setOnClickListener {
            update(body.reset())
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.damage_calculator_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reset -> {
                body.reset()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*private fun update(body: Body) {
        findViewById<TextView>(R.id.textView3).text = "${body.getTotalHealth()} / ${body.getTotalInitialHealth()}"
        headButton.text = "Head ${body.head.health.roundToInt()} / ${body.head.initialHealth}"
        thoraxButton.text = "Thorax ${body.thorax.health.roundToInt()} / ${body.thorax.initialHealth}"
        stomachButton.text = "Stomach ${body.stomach.health.roundToInt()} / ${body.stomach.initialHealth}"
        leftArmButton.text = "L. Arm ${body.leftArm.health.roundToInt()} / ${body.leftArm.initialHealth}"
        rightArmButton.text = "R. Arm ${body.rightArm.health.roundToInt()} / ${body.rightArm.initialHealth}"
        leftLegButton.text = "L. Leg ${body.leftLeg.health.roundToInt()} / ${body.leftLeg.initialHealth}"
        rightLegButton.text = "R. Leg ${body.rightLeg.health.roundToInt()} / ${body.rightLeg.initialHealth}"


    }*/
}