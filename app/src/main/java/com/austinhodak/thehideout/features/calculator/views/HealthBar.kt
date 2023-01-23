package com.austinhodak.thehideout.features.calculator.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.features.calculator.models.Body
import com.austinhodak.thehideout.features.calculator.models.BodyPart
import kotlin.math.roundToInt


@SuppressLint("ViewConstructor", "SetTextI18n")
class HealthBar @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0,
    bodyPart: BodyPart,
    body: Body
) : ConstraintLayout(context, attr, defStyleAttr) {

    private var currentHealth = Health()
    private var pg: ProgressBar
    private var healthTV: TextView
    private var blackedTV: TextView
    private var shotCountTV: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_health_bar, this, true)
        pg = findViewById(R.id.healthBarPG)
        val title = findViewById<TextView>(R.id.healthBarTV)
        healthTV = findViewById(R.id.healthBarHPTV)
        blackedTV = findViewById(R.id.healthBarX)
        shotCountTV = findViewById(R.id.shotCountTV)
        body.linkToHealthBar(bodyPart.name, this)

        bodyPart.let {
            title.text = it.name.name
            pg.max = it.initialHealth.roundToInt()
            pg.progress = it.health.roundToInt()
            healthTV.text = "${pg.progress}/${pg.max}"
            currentHealth.current = pg.progress.toDouble()
            currentHealth.max = pg.max.toDouble()
            updateBackground(currentHealth)
        }

        /*context.withStyledAttributes(attr, R.styleable.HealthBar) {
            title.text = getString(R.styleable.HealthBar_name)
            pg.max = getInt(R.styleable.HealthBar_maxHealth, 0)
            pg.progress = getInt(R.styleable.HealthBar_health, 0)
            healthTV.text = "${pg.progress}/${pg.max}"
            currentHealth.current = pg.progress.toDouble()
            currentHealth.max = pg.max.toDouble()
            updateBackground(currentHealth)
        }*/
    }

    fun updateHealth(newHealth: Health) {
        pg.max = newHealth.max.toInt()
        updateBackground(currentHealth, newHealth)
        currentHealth = newHealth
    }

    fun updateShotCount(count: Int) {
        shotCountTV.visibility = if (count == 0) View.GONE else View.VISIBLE
        shotCountTV.text = count.toString()
    }

    private fun updateBackground(health: Health, newHealth: Health? = null) {
        val layerDrawable: LayerDrawable = getDrawable(context, R.drawable.progress_bar) as LayerDrawable
        val strokeDrawable = layerDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable

        if (newHealth != null && newHealth.current <= 0.0) {
            healthTV.setTextColor(context.getColor(R.color.md_red_500))
            strokeDrawable.setStroke(3, context.getColor(R.color.md_red_500))
            blackedTV.visibility = View.VISIBLE
        } else if (health.current <= 0.0 && newHealth == null) {
            healthTV.setTextColor(context.getColor(R.color.md_red_500))
            strokeDrawable.setStroke(3, context.getColor(R.color.md_red_500))
            blackedTV.visibility = View.VISIBLE
        } else {
            blackedTV.visibility = View.GONE
            healthTV.setTextColor(context.getColor(R.color.white))
            strokeDrawable.setStroke(3, context.getColor(R.color.md_grey_800))
        }

        val progressPercentage = health.current / health.max

        if (newHealth != null) {
            val valueAnimator = ValueAnimator.ofFloat(health.current.toFloat(), newHealth.current.toFloat())
            valueAnimator.duration = 300
            valueAnimator.addUpdateListener {
                val progressPercentageNew = if (health.max != newHealth.max) {
                    it.animatedValue as Float / newHealth.max
                } else {
                    it.animatedValue as Float / health.max
                }
                healthTV.text = "${(it.animatedValue as Float).roundToInt()}/${newHealth.max.roundToInt()}"
                pg.progressTintList = ColorStateList.valueOf(
                    Color.rgb(
                        (255 - (255 * progressPercentageNew)).roundToInt().coerceIn(0, 255),
                        (185 * progressPercentageNew).roundToInt().coerceIn(0, 255),
                        0
                    )
                )
                pg.progress = (it.animatedValue as Float).roundToInt()
            }
            valueAnimator.start()
        } else {
            if (health.max != 0.0) {
                pg.progressTintList = ColorStateList.valueOf(
                    Color.rgb(
                        (255 - (255 * progressPercentage)).roundToInt(),
                        (185 * progressPercentage).roundToInt(),
                        0
                    )
                )
            }
        }

        pg.progressDrawable = layerDrawable
    }

    data class Health(
        var current: Double = 0.0,
        var max: Double = 0.0
    )

}