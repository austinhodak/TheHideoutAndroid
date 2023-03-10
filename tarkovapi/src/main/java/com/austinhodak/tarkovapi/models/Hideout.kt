package com.austinhodak.tarkovapi.models

import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.utils.Hideout
import java.io.Serializable

data class Hideout(
    var modules: List<Module?>? = null,
    var stations: List<Station?>? = null
) : Serializable {
    data class Module(
        var id: Int? = null,
        var level: Int? = null,
        var module: String? = null,
        var require: List<Require?>? = null,
        var stationId: Int? = null
    ) : Serializable {
        data class Require(
            var id: Int? = null,
            var name: Any? = null,
            var quantity: Int? = null,
            var type: String? = null
        ) : Serializable {
            fun getNumberString(): String {
                return if (quantity ?: 0 <= 1) "" else "${quantity}x "
            }

            override fun toString(): String {
                return when (type) {
                    "module" -> "MODULES NEEDED"
                    "item" -> "ITEMS NEEDED"
                    "trader" -> "TRADER LOYALTY NEEDED"
                    "skill" -> "SKILL LEVEL NEEDED"
                    else -> ""
                }
            }
        }

        fun getModuleRequirements(modules: List<Module?>?): List<Int> {
            val ids: MutableList<Int> = emptyList<Int>().toMutableList()

            if (module == "Stash") return emptyList()

            require?.filter { it?.type == "module" }?.forEach {
                val name = it?.name
                val level = it?.quantity

                val module = modules?.find { module ->
                    module?.module == name && module?.level == level
                }

                module?.id?.let {
                    ids.add(it)
                }
            }

            return ids
        }

        fun getIcon(name: String? = module): Int {
            return when (name) {
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

        fun getStation(h: Hideout): Station? {
            return h.hideout?.stations?.find { it?.id == stationId }
        }

        override fun toString(): String {
            return "$module Level $level"
        }


    }

    data class Station(
        var disabled: Boolean? = null,
        var function: String? = null,
        var id: Int? = null,
        var imgSource: String? = null,
        var locales: Locales? = null
    ) : Serializable {
        data class Locales(
            var en: String? = null
        ) : Serializable

        fun getName() = locales?.en

        fun getIcon(name: String? = locales?.en): Int {
            return when (name) {
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
    }
}