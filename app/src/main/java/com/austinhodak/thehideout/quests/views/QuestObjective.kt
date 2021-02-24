package com.austinhodak.thehideout.quests.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Quest

class QuestObjective @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    private var icon: ImageView
    private var text: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_quest_objective, this, true)
        icon = findViewById(R.id.questTaskIcon)
        text = findViewById(R.id.questTaskText)
    }

    private fun completed(complete: Boolean) {
        if (complete) {
            text.setTextColor(resources.getColor(R.color.md_green_500))
            icon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
        } else {
            text.setTextColor(resources.getColor(R.color.primaryText87))
            icon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText87))
        }
    }

    private fun setText(string: String) {
        text.text = string
    }

    private fun setIcon(@DrawableRes drawable: Int) {
        icon.setImageResource(drawable)
    }

    fun setObjective(objective: Quest.QuestObjectives, objectivesList: UserFB.UserFBQuestObjectives?) {
        setText(objective.toString())
        setIcon(objective.getIcon())
        completed(objective.isCompleted(objectivesList))
    }

}