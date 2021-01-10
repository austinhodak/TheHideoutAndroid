package com.austinhodak.thehideout.quests.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class QuestOverviewItem(
    @DrawableRes var icon: Int,
    @ColorRes var color: Int,
    var title: String,
    var subtitle: String,
    var progress: Int = 0,
    var maxProgress: Int = 100,
    val count: String = "$progress/$maxProgress",
)