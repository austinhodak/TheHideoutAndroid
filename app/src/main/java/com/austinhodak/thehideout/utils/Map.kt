package com.austinhodak.thehideout.utils

import androidx.annotation.IntegerRes
import com.austinhodak.thehideout.R

enum class Map(var mapName: String, var duration: String, var players: String, @IntegerRes var icon: Int) {
    CUSTOMS("Customs", "45 Minutes", "8-12", R.drawable.icons8_structural_96),
    FACTORY("Factory", "20 Minutes", "4-5", R.drawable.icons8_factory_breakdown_96),
    INTERCHANGE("Interchange", "45 Minutes", "10-14", R.drawable.icons8_shopping_mall_96),
    LIGHTHOUSE("Lighthouse", "50 Minutes", "8-14", R.drawable.icons8_lighthouse_96),
    RESERVE("Reserve", "50 Minutes", "9-12", R.drawable.icons8_knight_96),
    SHORELINE("Shoreline", "50 Minutes", "10-13", R.drawable.icons8_bay_96),
    THELAB("The Lab", "40 Minutes", "6-10", R.drawable.icons8_laboratory_96),
    WOODS("Woods", "50 Minutes", "8-14", R.drawable.icons8_forest_96),
}