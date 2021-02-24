package com.austinhodak.thehideout.calculator.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.austinhodak.thehideout.R
import kotlin.math.roundToInt


class HealthBar @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    private var name = ""
    private var currentHealth = Health()
    private var pg: ProgressBar
    private var healthTV: TextView
    private var blackedTV: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_health_bar, this, true)
        pg = findViewById(R.id.healthBarPG)
        val title = findViewById<TextView>(R.id.healthBarTV)
        healthTV = findViewById(R.id.healthBarHPTV)
        blackedTV = findViewById(R.id.healthBarX)

        context.withStyledAttributes(attr, R.styleable.HealthBar) {
            title.text = getString(R.styleable.HealthBar_name)
            pg.max = getInt(R.styleable.HealthBar_maxHealth, 0)
            pg.progress = getInt(R.styleable.HealthBar_health, 0)
            healthTV.text = "${pg.progress}/${pg.max}"
            currentHealth.current = pg.progress.toDouble()
            currentHealth.max = pg.max.toDouble()
            updateBackground(currentHealth)
        }
    }

    fun updateHealth(newHealth: Health) {
        updateBackground(currentHealth, newHealth)
        currentHealth = newHealth
    }

    private fun updateBackground(health: Health, newHealth: Health? = null) {
        val layerDrawable: LayerDrawable = getDrawable(context, R.drawable.progress_bar) as LayerDrawable
        val strokeDrawable = layerDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable
        Log.d("HEALTH", newHealth.toString())
        if (newHealth != null && newHealth.current <= 0.0) {
            healthTV.setTextColor(resources.getColor(R.color.md_red_500))
            strokeDrawable.setStroke(3, context.getColor(R.color.md_red_500))
            blackedTV.visibility = View.VISIBLE
        } else if (health.current <= 0.0 && newHealth == null) {
            healthTV.setTextColor(resources.getColor(R.color.md_red_500))
            strokeDrawable.setStroke(3, context.getColor(R.color.md_red_500))
            blackedTV.visibility = View.VISIBLE
        } else {
            blackedTV.visibility = View.GONE
            healthTV.setTextColor(resources.getColor(R.color.white))
            strokeDrawable.setStroke(3, context.getColor(R.color.md_grey_800))
        }

        val progressPercentage = health.current / health.max

        if (newHealth != null) {
            val valueAnimator = ValueAnimator.ofFloat(health.current.toFloat(), newHealth.current.toFloat())
            valueAnimator.duration = 300
            valueAnimator.addUpdateListener {
                val progressPercentage = it.animatedValue as Float / health.max
                healthTV.text = "${(it.animatedValue as Float).roundToInt()}/${newHealth.max.roundToInt()}"
                pg.progressTintList = ColorStateList.valueOf(
                    Color.rgb(
                        (255 - (255 * progressPercentage)).roundToInt(),
                        (185 * progressPercentage).roundToInt(),
                        0
                    )
                )
                pg.progress = (it.animatedValue as Float).roundToInt()
            }
            valueAnimator.start()
        } else {
            pg.progressTintList = ColorStateList.valueOf(
                Color.rgb(
                    (255 - (255 * progressPercentage)).roundToInt(),
                    (185 * progressPercentage).roundToInt(),
                    0
                )
            )
        }

        pg.progressDrawable = layerDrawable
    }

    data class Health(
        var current: Double = 0.0,
        var max: Double = 0.0
    )

}