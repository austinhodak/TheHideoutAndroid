package com.austinhodak.thehideout.hideout.models

import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.userRef

data class HideoutModule(
    var module: String,
    var level: Int,
    var function: String,
    var imgSource: String,
    var require: List<ModuleRequire>,
    var id: Int,
    var construction_time: Int,
    var bonuses: ArrayList<String>
) {
    data class ModuleRequire(
        var type: String,
        var name: String,
        var quantity: Int,
        var id: Int,
        var fleaPrice: Int = 0
    ) {
        override fun toString(): String {
            return when (type) {
                "currency" -> "$quantity $name"
                "module" -> "$name Level $quantity"
                "item" -> "x$quantity $name"
                else -> ""
            }
        }

        fun getSubtitle(fleaList: List<FleaItem>?): String {
            val item = fleaList?.find { it.name?.replace("#", "").equals(name, ignoreCase = true) }
            if (item != null) {
                if (type != "module" && type != "currency") {
                    fleaPrice = item.avg24hPrice!! * quantity
                    return (item.avg24hPrice * quantity).getPrice("₽")
                }
            }
            return ""
        }
    }

    fun getTotalBuildCost(): String {
        var total: Int = 0
        for (requirement in require) {
            total += requirement.fleaPrice
        }
        return total.getPrice("₽")
    }

    fun getConstructionTime(): String {
        return if (construction_time == 0) "Construction Time: Instant" else "Construction Time: $construction_time Hours"
    }

    fun getIcon(): Int {
        return when (module) {
            "Air Filtering Unit" -> R.drawable.air_filtering_unit_portrait
            "Bitcoin farm" -> R.drawable.bitcoin_farm_portrait
            "Booze generator" -> R.drawable.booze_generator_portrait
            "Generator" -> R.drawable.generator_portrait
            "Heating" -> R.drawable.heating_portrait
            "Illumination" -> R.drawable.illumination_portrait
            "Intelligence center" -> R.drawable.intelligence_center_portrait
            "Lavatory" -> R.drawable.lavatory_portrait
            "Library" -> R.drawable.library_portrait
            "Medstation" -> R.drawable.medstation_portrait
            "Nutrition unit" -> R.drawable.nutrition_unit_portrait
            "Rest space" -> R.drawable.rest_space_portrait
            "Scav case" -> R.drawable.scav_case_portrait
            "Security" -> R.drawable.security_portrait
            "Shooting range" -> R.drawable.shooting_range_portrait
            "Solar power" -> R.drawable.solar_power_portrait
            "Stash" -> R.drawable.stash_portrait
            "Vents" -> R.drawable.vents_portrait
            "Water collector" -> R.drawable.water_collector_portrait
            "Workbench" -> R.drawable.workbench_portrait
            else -> R.drawable.workbench_portrait
        }
    }

    fun buildModule() {
        userRef("/hideout/completed/\"$id\"").setValue(true)
    }

    fun downgradeModule() {
        userRef("/hideout/completed/\"$id\"").removeValue()
    }
}