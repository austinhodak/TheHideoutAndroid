package com.austinhodak.tarkovapi.room.models

import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.StatusGreen
import com.austinhodak.tarkovapi.models.StatusRed
import com.austinhodak.tarkovapi.models.Stim
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.utils.getItemType
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import timber.log.Timber
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Entity(tableName = "items")
data class Item(
    @PrimaryKey var id: String,
    var itemType: ItemTypes? = ItemTypes.NONE,
    var parent: String? = null,
    val Name: String? = null,
    val ShortName: String? = null,
    val Description: String? = null,
    val Weight: Double? = null,
    val Width: Int? = null,
    val Height: Int? = null,
    val StackMaxSize: Int? = null,
    val Rarity: String? = null,
    val SpawnChance: Double? = null,
    val BackgroundColor: String? = null,
    val LootExperience: Int? = null,
    val ExamineExperience: Int? = null,
    val RepairCost: Int? = null,
    val Durability: Int? = null,
    val MaxDurability: Int? = null,
    val pricing: Pricing? = null,
    val DiscardLimit: Int? = null,
    //Armor
    val Slots: List<Weapon.Slot>? = null,
    val BlocksEarpiece: Boolean? = null,
    val BlocksEyewear: Boolean? = null,
    val BlocksHeadwear: Boolean? = null,
    val BlocksFaceCover: Boolean? = null,
    val BlocksArmorVest: Boolean? = null,
    val RigLayoutName: String? = null,
    val armorClass: String? = null,
    val speedPenaltyPercent: Double? = null,
    val mousePenalty: Double? = null,
    val weaponErgonomicPenalty: Double? = null,
    val armorZone: List<String>? = null,
    val Indestructibility: Double? = null,
    val headSegments: List<String>? = null,
    val FaceShieldComponent: Boolean? = null,
    val FaceShieldMask: String? = null,
    val HasHinge: Boolean? = null,
    val DeafStrength: String? = null,
    val BluntThroughput: Double? = null,
    val ArmorMaterial: String? = null,
    val BlindnessProtection: Double? = null,
    val Grids: List<Grid>? = null,
    //Mods
    val Accuracy: Double? = null,
    val Recoil: Double? = null,
    val Loudness: Double? = null,
    val EffectiveDistance: Double? = null,
    val Ergonomics: Double? = null,
    val Velocity: Double? = null,
    val RaidModdable: Boolean? = null,
    val ToolModdable: Boolean? = null,
    val SightingRange: Double? = null,
    val muzzleModType: String? = null,
    //Meds
    val medUseTime: Double? = null,
    val medEffectType: String? = null,
    val MaxHpResource: Double? = null,
    val hpResourceRate: Double? = null,
    val StimulatorBuffs: String? = null,
    @SerializedName("effects_health_null")
    var effects_health: JSONObject? = null,
    @SerializedName("effects_damage_null")
    var effects_damage: JSONObject? = null
) : Serializable {

    fun getFormattedWeight(): String {
        val weight = if (Weight?.rem(1.0) == 0.0 ) {
            Weight.toInt()
        } else {
            String.format("%.2f", Weight)
        }
        return "${weight}kg"
    }

    fun getGridIDs(): List<String?> {
        return Grids?.flatMap { it.getFilters() } ?: listOf()
    }

    fun getTotalInternalSize(): Int {
        return Grids?.sumOf { it.getInternalSlots() } ?: 0
    }

    fun getBuffs(effects: Stim?): List<Effect> {
        val list: MutableList<Effect> = emptyList<Effect>().toMutableList()
        effects_health?.let {
            if (it.has("Hydration")) {
                val value = it.optJSONObject("Hydration")?.optInt("value") ?: 0
                if (value > 0) {
                    list.add(
                        Effect(null, "Hydration", value.toString(), type = "buff", color = StatusGreen)
                    )
                } else {
                    list.add(
                        Effect(null, "Hydration", value.toString(), type = "debuff", color = StatusRed)
                    )
                }
            }
            if (it.has("Energy")) {
                val value = it.optJSONObject("Energy")?.optInt("value") ?: 0
                if (value > 0) {
                    list.add(
                        Effect(null, "Energy", value.toString(), type = "buff", color = StatusGreen)
                    )
                } else {
                    list.add(
                        Effect(null, "Energy", value.toString(), type = "debuff", color = StatusRed)
                    )
                }
            }
        }

        effects_damage?.let {
            if (it.has("Pain")) {
                val duration = it.optJSONObject("Pain")?.optInt("duration") ?: 0
                list.add(
                    Effect(R.drawable.pain_icon, "Removes Pain", "${duration}s", type = "buff")
                )
                list.add(
                    Effect(R.drawable.painkiller_icon, "On Painkillers", "${duration}s", type = "buff")
                )
            }
            if (it.has("Intoxication")) {
                list.add(
                    Effect(R.drawable.toxin_icon, "Removes Poisoning", "", type = "buff")
                )
            }
            if (it.has("Contusion")) {
                list.add(
                    Effect(R.drawable.contusion_icon, "Removes Contusion", "", type = "buff")
                )
            }

            if (it.has("LightBleeding")) {
                list.add(
                    Effect(R.drawable.light_bleeding_icon, "Removes Light Bleeding", "", type = "buff")
                )
                list.add(
                    Effect(R.drawable.fresh_wound_icon, "Adds Fresh Wound", "", type = "buff")
                )
            }

            if (it.has("HeavyBleeding")) {
                list.add(
                    Effect(R.drawable.heavy_bleeding_icon, "Removes Heavy Bleeding", "", type = "buff")
                )
                list.add(
                    Effect(R.drawable.fresh_wound_icon, "Adds Fresh Wound", "", type = "buff")
                )
            }

            if (it.has("Fracture")) {
                list.add(
                    Effect(R.drawable.fracture_icon, "Removes Fracture", "", type = "buff")
                )
                list.add(
                    Effect(R.drawable.fresh_wound_icon, "Adds Fresh Wound", "", type = "buff")
                )
            }
        }

        effects?.let {
            it.stats.find { it.BuffType == "EnergyRate" }?.let {
                list.add(
                    Effect(null, "Energy Recovery (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}/s", type = "buff", color = StatusGreen)
                )
            }
            it.stats.find { it.BuffType == "HydrationRate" }?.let {
                list.add(
                    Effect(null, "Hydration Recovery (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}/s", type = "buff", color = StatusGreen)
                )
            }

            it.stats.find { it.BuffType == "Contusion" }?.let {
                list.add(
                    Effect(R.drawable.contusion_icon, "Causes Contusion (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}/s", type = "debuff", color = StatusRed)
                )
            }

            it.stats.find { it.BuffType == "MaxStamina" }?.let {
                list.add(
                    Effect(null, "Increases Max Stamina (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}", type = "buff", color = StatusGreen)
                )
            }

            it.stats.find { it.BuffType == "StaminaRate" }?.let {
                list.add(
                    Effect(null, "Increases Stamina Recovery (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}/s", type = "buff", color = StatusGreen)
                )
            }

            it.stats.filter { it.BuffType == "SkillRate" }.forEach {
                list.add(
                    Effect(null, "Increases ${it.SkillName} (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}", type = "buff", color = StatusGreen)
                )
            }

            it.stats.find { it.BuffType == "HandsTremor" }?.let {
                list.add(
                    Effect(R.drawable.tremor_icon, "Causes Tremor (${it.Delay}s Delay/${it.Duration}s Duration)", "", type = "debuff")
                )
            }

            it.stats.find { it.BuffType == "QuantumTunnelling" }?.let {
                list.add(
                    Effect(R.drawable.tunnelvision, "Causes Tunnel Vision (${it.Delay}s Delay/${it.Duration}s Duration)", "", type = "debuff")
                )
            }

            it.stats.find { it.BuffType == "HealthRate" }?.let {
                list.add(
                    Effect(null, "${if (it.Value > 0) "increases" else "decreases"} Health Regen (${it.Delay}s Delay/${it.Duration}s Duration)", "${it.Value}/s", type = if (it.Value > 0) "buff" else "debuff")
                )
            }

            it.stats.find { it.BuffType == "RemovesAllBloodLosses" }?.let {
                list.add(
                    Effect(R.drawable.light_bleeding_icon, "Remove Light Bleeding (${it.Delay}s Delay/${it.Duration}s Duration)", "", type = "buff", color = StatusGreen)
                )
                list.add(
                    Effect(R.drawable.heavy_bleeding_icon, "Removes Heavy Bleeding (${it.Delay}s Delay/${it.Duration}s Duration)", "", type = "buff", color = StatusGreen)
                )
            }

            it.stats.find { it.BuffType == "DamageModifier" }?.let {
                list.add(
                    Effect(null, "${if (it.Value > 0) "increases" else "decreases"} Damage Taken (${it.Delay}s Delay/${it.Duration}s Duration)", "${(it.Value * 100)}%", type = if (it.Value > 0) "debuff" else "buff")
                )
            }

            it.stats.find { it.BuffType == "BodyTemperature" }?.let {
                list.add(
                    Effect(null, "Lowers Body Temp (${it.Delay}s Delay/${it.Duration}s Duration)", "-${it.Value.roundToInt()}°C", type = "buff", color = StatusGreen)
                )
            }
        }

        return list.distinctBy { it.title }
    }

    data class Effect(
        @DrawableRes
        val icon: Int?,
        val title: String,
        val value: String?,
        val color: Color? = null,
        val type: String
    )

    fun getEnergy(): Int? {
        return effects_health?.optJSONObject("Energy")?.optInt("value")
    }

    fun getHydration(): Int? {
        return effects_health?.optJSONObject("Hydration")?.optInt("value")
    }

    fun cArmorClass(): Int {
        return armorClass?.toIntOrNull() ?: 0
    }

    fun getInternalSlots(): Int? {
        if (Grids != null) {
            return Grids.sumOf { it._props?.cellsH?.times(it._props.cellsV ?: 0) ?: 0 }
        }
        return null
    }

    fun getTotalSlots(): Int {
        return Width?.times(Height ?: 1) ?: 1
    }

    fun getPrice(): Int {
        if (id == "59faff1d86f7746c51718c9c") {
            return pricing?.getHighestSellTrader()?.getPriceAsRoubles() ?: 0
        }
        return pricing?.getPrice() ?: 0
    }

    fun getPricePerSlot(price: Int = getPrice()): Int {
        return price / getTotalSlots()
    }

    fun getUpdatedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return "Updated ${
            DateUtils.getRelativeTimeSpanString(
                sdf.parse(pricing?.updated ?: "2021-07-01T08:36:35.194Z")?.time ?: 0,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }"
    }

    fun getAllMods(): List<String>? {
        if (Slots.isNullOrEmpty()) return null
        return Slots.flatMap {
            it._props?.filters?.first()?.Filter?.filterNotNull()!!
        }
    }

    fun destructibility(): Double {
        return when (ArmorMaterial) {
            "UHMWPE" -> 0.45
            "Titan" -> 0.55
            "Glass" -> 0.8
            "Combined" -> 0.5
            "Ceramic" -> 0.8
            "ArmoredSteel" -> 0.7
            "Aramid" -> 0.25
            "Aluminium" -> 0.6
            else -> 0.0
        }
    }

    fun materialName(): String {
        return when (ArmorMaterial) {
            "UHMWPE" -> ArmorMaterial
            "Titan" -> ArmorMaterial
            "Glass" -> ArmorMaterial
            "Combined" -> "Combined Materials"
            "Ceramic" -> ArmorMaterial
            "ArmoredSteel" -> "Steel"
            "Aramid" -> ArmorMaterial
            "Aluminium" -> ArmorMaterial
            else -> "$ArmorMaterial"
        }
    }

    fun isChildOf(item: Pricing?): Boolean? {
        return item?.containsItem?.any { it.item?.id == this.id }
    }

    fun discardLimit(): String {
        return when (DiscardLimit ?: -1) {
            -1, 0 -> "No Limit."
            else -> "$DiscardLimit"
        }
    }
}

fun JSONObject.toItem(): Item {
    val props = getJSONObject("_props")

    val itemType = getItemType()

    val builder = GsonBuilder()

    val item = builder.create().fromJson(props.toString(), Item::class.java)

    item.itemType = itemType
    item.parent = optString("_parent")
    item.id = getString("_id") ?: ""

    if (props.has("effects_damage") && props["effects_damage"] is JSONObject) {
        Timber.d(props.getJSONObject("effects_damage").toString())
    }

    item.effects_health = if (props.has("effects_health") && props["effects_health"] is JSONObject) {
        props.getJSONObject("effects_health")
    } else {
        null
    }

    item.effects_damage = if (props.has("effects_damage") && props["effects_damage"] is JSONObject) {
        props.getJSONObject("effects_damage")
    } else {
        null
    }

    return item
}