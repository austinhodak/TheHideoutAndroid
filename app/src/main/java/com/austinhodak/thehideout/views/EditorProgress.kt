package com.austinhodak.thehideout.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import com.austinhodak.thehideout.R


class EditorProgress @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    var pg: ProgressBar
    var nameTV: TextView
    var valueTV: TextView
    var icon: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_loadout_progressbar, this, true)
        pg = findViewById(R.id.progressBar)
        nameTV = findViewById(R.id.name)
        valueTV = findViewById(R.id.value)
        icon = findViewById(R.id.icon)



        context.withStyledAttributes(attr, R.styleable.LoadoutProgressBar) {
            nameTV.text = getString(R.styleable.LoadoutProgressBar_lname)
            valueTV.text = getString(R.styleable.LoadoutProgressBar_lendText)
            icon.setImageDrawable(getDrawable(R.styleable.LoadoutProgressBar_licon))
            pg.max = getInt(R.styleable.LoadoutProgressBar_lmax, 0)
            pg.progress = getInt(R.styleable.LoadoutProgressBar_lprogress, 0)
            pg.secondaryProgress = getInt(R.styleable.LoadoutProgressBar_lsecondaryProgress, 0)
            pg.progressDrawable = getDrawable(R.styleable.LoadoutProgressBar_lprogressDrawable)

           //recycle()
        }
    }
}