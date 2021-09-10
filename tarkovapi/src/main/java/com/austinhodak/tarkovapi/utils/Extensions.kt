package com.austinhodak.tarkovapi.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.austinhodak.tarkovapi.BartersQuery
import com.austinhodak.tarkovapi.CraftsQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.QuestsQuery
import com.austinhodak.tarkovapi.fragment.ItemFragment
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

fun AmmoCalibers(): List<String> = arrayListOf(
    "Caliber762x35",
    "Caliber86x70",
    "Caliber366TKM",
    "Caliber1143x23ACP",
    "Caliber127x55",
    "Caliber12g",
    "Caliber20g",
    "Caliber23x75",
    "Caliber46x30",
    "Caliber40x46",
    "Caliber545x39",
    "Caliber556x45NATO",
    "Caliber57x28",
    "Caliber762x25TT",
    "Caliber762x39",
    "Caliber762x51",
    "Caliber762x54R",
    "Caliber9x18PM",
    "Caliber9x19PARA",
    "Caliber9x21",
    "Caliber9x39",
)

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun getCaliberName(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> ".300 Blackout"
        "Caliber86x70" -> ".338 Lapua"
        "Caliber366TKM" -> ".366 TKM"
        "Caliber1143x23ACP" -> ".45 ACP"
        "Caliber127x55" -> "12.7x55mm"
        "Caliber12g" -> "12 Gauge"
        "Caliber20g" -> "20 Gauge"
        "Caliber23x75" -> "23x75mm"
        "Caliber46x30" -> "4.6x30mm"
        "Caliber40x46" -> "40x46mm"
        "Caliber545x39" -> "5.45x39mm"
        "Caliber556x45NATO" -> "5.56x45mm NATO"
        "Caliber57x28" -> "5.7x28mm FN"
        "Caliber762x25TT" -> "7.62x25mm Tokarev"
        "Caliber762x39" -> "7.62x39mm"
        "Caliber762x51" -> "7.62x51mm NATO"
        "Caliber762x54R" -> "7.62x54mmR"
        "Caliber9x18PM" -> "9x18mm Makarov"
        "Caliber9x19PARA" -> "9x19mm Parabellum"
        "Caliber9x21" -> "9x21mm Gyurza"
        "Caliber9x39" -> "9x39mm"
        else -> "Unknown Ammo Type"
    }
}

fun getCaliberShortName(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> ".300"
        "Caliber86x70" -> ".338"
        "Caliber366TKM" -> ".366 TKM"
        "Caliber1143x23ACP" -> ".45 ACP"
        "Caliber127x55" -> "12.7x55mm"
        "Caliber12g" -> "12G"
        "Caliber20g" -> "20G"
        "Caliber23x75" -> "23x75mm"
        "Caliber46x30" -> "4.6x30mm"
        "Caliber40x46" -> "40x46mm"
        "Caliber545x39" -> "5.45x39mm"
        "Caliber556x45NATO" -> "5.56x45mm"
        "Caliber57x28" -> "5.7x28mm"
        "Caliber762x25TT" -> "7.62x25mm"
        "Caliber762x39" -> "7.62x39mm"
        "Caliber762x51" -> "7.62x51mm"
        "Caliber762x54R" -> "7.62x54mmR"
        "Caliber9x18PM" -> "9x18mm"
        "Caliber9x19PARA" -> "9x19mm"
        "Caliber9x21" -> "9x21mm"
        "Caliber9x39" -> "9x39mm"
        else -> "Unknown Ammo Type"
    }
}

fun JSONObject.itemType(): ItemTypes {
    val props = getJSONObject("_props")
    if (
        getString("_name").equals("Ammo")
        || getString("_parent").isNullOrBlank()
        || getString("_parent") == "54009119af1c881c07000029"
        || getString("_parent") == "5661632d4bdc2d903d8b456b"
    ) {
        return ItemTypes.NULL
    }

    return when {
        props.has("weapFireType") -> ItemTypes.WEAPON
        props.has("Caliber") && !props.getString("Name").contains("Shrapnel", true) -> ItemTypes.AMMO
        else -> {
            ItemTypes.NONE
        }
    }
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun QuestsQuery.Quest.toQuest(): Quest {
    val quest = this.fragments.questFragment
    return Quest(
        quest.id,
        quest.title,
        quest.wikiLink,
        quest.exp,
        quest.giver.fragments.traderFragment,
        quest.turnin.fragments.traderFragment,
        quest.unlocks,
        Quest.QuestRequirement(
            level = quest.requirements?.level,
            quests = quest.requirements?.quests
        ),
        quest.objectives.map {
            val obj = it?.fragments?.objectiveFragment
            Quest.QuestObjective(
                obj?.id,
                obj?.type,
                obj?.target,
                obj?.number,
                obj?.location,
                obj?.targetItem?.fragments?.itemFragment?.toClass()
            )
        }
        //quest.reputation?.map { it.fragments.repFragment },
    )
}

fun ItemFragment.toClass(): Pricing {
    val item = this
    return Pricing(
        item.id,
        item.name,
        item.shortName,
        item.iconLink,
        item.imageLink,
        item.gridImageLink,
        item.avg24hPrice,
        item.basePrice,
        item.lastLowPrice,
        item.changeLast48h,
        item.low24hPrice,
        item.high24hPrice,
        item.updated,
        types = emptyList(),
        item.width,
        item.height,
        sellFor = item.sellFor?.map { s1 ->
            val s = s1.fragments.itemPrice
            Pricing.BuySellPrice(
                s.source?.rawValue,
                s.price,
                s.requirements.map { requirement ->
                    Pricing.BuySellPrice.Requirement(requirement?.type?.rawValue!!, requirement.value!!)
                }
            )
        },
        buyFor = item.buyFor?.map { s1 ->
            val s = s1.fragments.itemPrice
            Pricing.BuySellPrice(
                s.source?.rawValue,
                s.price,
                s.requirements.map { requirement ->
                    Pricing.BuySellPrice.Requirement(requirement?.type?.rawValue!!, requirement.value!!)
                }
            )
        }
    )
}

fun CraftsQuery.Craft.toCraft(): Craft {
    return Craft(
        duration = duration,
        requiredItems = requiredItems.map {
            Craft.CraftItem(it?.fragments?.taskItem?.count?.roundToInt(), it?.fragments?.taskItem?.item?.fragments?.itemFragment?.toClass())
        },
        rewardItems = rewardItems.map {
            Craft.CraftItem(it?.fragments?.taskItem?.count?.roundToInt(), it?.fragments?.taskItem?.item?.fragments?.itemFragment?.toClass())
        },
        source = source
    )
}

fun BartersQuery.Barter.toBarter(): Barter {
    return Barter(
        requiredItems = requiredItems.map {
            Craft.CraftItem(it?.fragments?.taskItem?.count?.roundToInt(), it?.fragments?.taskItem?.item?.fragments?.itemFragment?.toClass())
        },
        rewardItems = rewardItems.map {
            Craft.CraftItem(it?.fragments?.taskItem?.count?.roundToInt(), it?.fragments?.taskItem?.item?.fragments?.itemFragment?.toClass())
        },
        source = source
    )
}

fun ItemsByTypeQuery.ItemsByType.toPricing(): Pricing {
    val item = this.fragments.itemFragment
    return item.toClass()
}

/*
fun Double.getColor(reverse: Boolean = false, surfaceColor: Color): Color {
    return if (this == 0.0) {
        surfaceColor
    } else if (this > 0) {
        if (reverse) Red500 else Green500
    } else {
        if (reverse) Green500 else Red500
    }
}
*/

fun Double.plusMinus(): String {
    val value = this.roundToInt()
    return if (value > 0) "+${value}" else if (value < 0) "-${value}" else value.toString()
}

fun Int.asCurrency(currency: String = "R"): String {
    val numFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
    }

    numFormat.currency = when (currency) {
        "R" -> Currency.getInstance("RUB")
        "D" -> Currency.getInstance("USD")
        "E" -> Currency.getInstance("EUR")
        else -> Currency.getInstance("RUB")
    }

    var formatted = numFormat.format(this)

    if (currency == "R") {
        formatted = formatted.replace("RUB", "").plus("₽")
    }

    return formatted
}

fun getCaliberImage(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/17/300_BlackoutAmmo.gif/revision/latest/scale-to-width-down/64?cb=20210106213942"
        "Caliber86x70" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/c/ce/338_lapua.gif/revision/latest/scale-to-width-down/64?cb=20210106212708"
        "Caliber366TKM" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/39/.366_TKM.gif/revision/latest/scale-to-width-down/64?cb=20200727194118"
        "Caliber1143x23ACP" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/.45_Icon.gif/revision/latest/scale-to-width-down/64?cb=20200727195602"
        "Caliber127x55" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/7/78/12.7x55.gif/revision/latest/scale-to-width-down/64?cb=20191109223753"
        "Caliber12g" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/92/12x70.gif/revision/latest/scale-to-width-down/64?cb=20191109235500"
        "Caliber20g" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/63/20-70.gif/revision/latest/scale-to-width-down/64?cb=20191109233805"
        "Caliber23x75" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/96/23x75.gif/revision/latest/scale-to-width-down/64?cb=20201020101316"
        "Caliber46x30" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/0/05/4.6x30.gif/revision/latest/scale-to-width-down/64?cb=20190519132652"
        "Caliber40x46" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/19/40x46mm.gif/revision/latest/scale-to-width-down/64?cb=20200727194146"
        "Caliber545x39" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/34/5.45x39.gif/revision/latest/scale-to-width-down/64?cb=20190519132729"
        "Caliber556x45NATO" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/c/ce/5.56x45_NATO.gif/revision/latest/scale-to-width-down/64?cb=20210331145303"
        "Caliber57x28" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/97/5.7x28.gif/revision/latest/scale-to-width-down/64?cb=20191109213543"
        "Caliber762x25TT" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/ff/TT_7.62x25.gif/revision/latest/scale-to-width-down/64?cb=20190519132412"
        "Caliber762x39" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/9f/7.62x39.gif/revision/latest/scale-to-width-down/64?cb=20210331145228"
        "Caliber762x51" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/1f/7.62x51_NATO.gif/revision/latest/scale-to-width-down/64?cb=20200805184031"
        "Caliber762x54R" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/5/59/7.62x54R.gif/revision/latest/scale-to-width-down/64?cb=20191228194724"
        "Caliber9x18PM" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/c/c2/9x18_PM.gif/revision/latest/scale-to-width-down/64?cb=20190519132449"
        "Caliber9x19PARA" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/db/9x19_para.gif/revision/latest/scale-to-width-down/64?cb=20200727195027"
        "Caliber9x21" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/5/53/9x21_gyurza.gif/revision/latest/scale-to-width-down/64?cb=20190519132559"
        "Caliber9x39" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/.45_Icon.gif/revision/latest/scale-to-width-down/64?cb=20200727195602"
        else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/.45_Icon.gif/revision/latest/scale-to-width-down/64?cb=20200727195602"
    }
}

fun String.traderIcon(): String {
    return when (this) {
        "prapor" -> "https://tarkov-tools.com/images/prapor-icon.jpg"
        "therapist" -> "https://tarkov-tools.com/images/therapist-icon.jpg"
        "fence" -> "https://tarkov-tools.com/images/fence-icon.jpg"
        "skier" -> "https://tarkov-tools.com/images/skier-icon.jpg"
        "peacekeeper" -> "https://tarkov-tools.com/images/peacekeeper-icon.jpg"
        "mechanic" -> "https://tarkov-tools.com/images/mechanic-icon.jpg"
        "ragman" -> "https://tarkov-tools.com/images/ragman-icon.jpg"
        "jaeger" -> "https://tarkov-tools.com/images/jaeger-icon.jpg"
        else -> "https://tarkov-tools.com/images/prapor-icon.jpg"
    }
}

fun String.sourceTitle(): String {
    return when (this) {
        "prapor" -> "Prapor"
        "therapist" -> "Therapist"
        "fence" -> "Fence"
        "skier" -> "Skier"
        "peacekeeper" -> "Peacekeeper"
        "mechanic" -> "Mechanic"
        "ragman" -> "Ragman"
        "jaeger" -> "Jaeger"
        else -> "Flea Market"
    }
}

fun Pricing.BuySellPrice.toName(showRequirement: Boolean = false): String? {
    val source = this.source
    return if (showRequirement && this.requirements.isNotEmpty()) {
        return if (source == "fleaMarket" && requirements.first().type == "playerLevel") {
            "Flea Market L${requirements.first().value}"
        } else {
            "$source ${requirements.first().value.getTraderLevel()}"
        }
        ""
    } else {
        if (source == "fleaMarket") "Flea Market"
        else source
    }
}

fun Pricing.BuySellPrice.traderImage(): String {
    //Flea Market Icon
    if (this.source == "fleaMarket") return "https://tarkov-tools.com/images/flea-market-icon.jpg"

    when {
        requirements.isNullOrEmpty() -> {
            return this.traderImage()
        }
        requirements.first().type == "loyaltyLevel" -> {
            val level = requirements.first().value
            return when (this.source) {
                "prapor" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fc/Prapor_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110125"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/7/75/Prapor_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110134"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/64/Prapor_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110141"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f1/Prapor_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110153"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fc/Prapor_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110125"
                }
                "therpaist" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fb/Therapist_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110312"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/5/5f/Therapist_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110321"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f6/Therapist_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110328"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Therapist_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110338"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Therapist_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110338"
                }
                "fence" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f7/Fence_Portrait.png/revision/latest/scale-to-width-down/127?cb=20180425012754"
                "ragman" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e5/Ragman_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110204"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/2/20/Ragman_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110215"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ed/Ragman_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110221"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/1c/Ragman_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110230"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e5/Ragman_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110204"
                }
                "peacekeeper" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Peacekeeper_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110041"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/96/Peacekeeper_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110052"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/95/Peacekeeper_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110059"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3e/Peacekeeper_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110108"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Peacekeeper_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110041"
                }
                "skier" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/eb/Skier_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110238"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/Skier_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110248"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/65/Skier_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110257"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e8/Skier_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110304"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/eb/Skier_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110238"
                }
                "mechanic" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3f/Mechanic_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105848"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/9b/Mechanic_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105910"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/b8/Mechanic_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110019"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/bd/Mechanic_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110029"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3f/Mechanic_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105848"
                }
                "jaeger" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d4/Jaeger_1_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221027"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/18/Jaeger_2_icon.png/revision/latest/scale-to-width-down/130?cb=20191101214208"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ed/Jaeger_3_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221028"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f2/Jaeger_4_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221026"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d4/Jaeger_1_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221027"
                }
                else -> "https://tarkov-tools.com/images/prapor-icon.jpg"
            }
        }
        else -> {
            return "https://tarkov-tools.com/images/flea-market-icon.jpg"
        }
    }
}

fun Int.getTraderLevel(): String {
    return when (this) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        else -> ""
    }
}

fun JSONObject.getItemType(): ItemTypes {
    val props = getJSONObject("_props")
    return when {
        props.has("weapFireType") -> ItemTypes.WEAPON
        props.has("Prefab") && props.getJSONObject("Prefab").getString("path").contains("assets/content/items/mods") -> ItemTypes.MOD
        props.has("Caliber") -> ItemTypes.AMMO
        this.getString("_parent").equals("5448e54d4bdc2dcc718b4568") -> ItemTypes.ARMOR
        this.getString("_parent").equals("5448e5284bdc2dcb718b4567") -> ItemTypes.RIG
        this.getString("_parent").equals("5448e53e4bdc2d60728b4567") -> ItemTypes.BACKPACK
        this.getString("_parent").equals("5a341c4686f77469e155819e") -> ItemTypes.FACECOVER
        this.getString("_parent").equals("5448e5724bdc2ddf718b4568") -> ItemTypes.GLASSES
        this.getString("_parent").equals("543be6564bdc2df4348b4568") -> ItemTypes.GRENADE
        this.getString("_parent").equals("5645bcb74bdc2ded0b8b4578") -> ItemTypes.HEADSET
        this.getString("_parent").equals("5a341c4086f77401f2541505") || this.getString("_parent").equals("57bef4c42459772e8d35a53b") -> ItemTypes.HELMET
        this.getString("_parent").equals("5c99f98d86f7745c314214b3") || this.getString("_parent").equals("5c164d2286f774194c5e69fa") -> ItemTypes.KEY
        this.getString("_parent").equals("5448f3a64bdc2d60728b456a") -> ItemTypes.STIM
        this.getString("_parent").equals("5448f39d4bdc2d0a728b4568") -> ItemTypes.MED
        this.getString("_parent").equals("5448f3ac4bdc2dce718b4569") -> ItemTypes.MED
        this.getString("_parent").equals("5448f3a14bdc2d27728b4569") -> ItemTypes.MED
        this.getString("_parent").equals("5448e8d04bdc2ddf718b4569") -> ItemTypes.FOOD
        this.getString("_parent").equals("5448e8d64bdc2dce718b4568") -> ItemTypes.FOOD
        else -> ItemTypes.NONE
    }
}

val ammoArmorPenValues = mutableMapOf(
    "5cc80f53e4a949000e1ea4f8" to "666322", //5.7x28mm L191
    "5cc86832d7f00c000d3a6e6c" to "400000", //5.7x28mm R37.F
    "5cc86840d7f00c002412c56c" to "610000", //5.7x28mm R37.X
    "5cc80f67e4a949035e43bbba" to "666432", //5.7x28mm SB193
    "5cc80f38e4a949001152b560" to "666543", //5.7x28mm SS190
    "5cc80f8fe4a949033b0224a2" to "662000", //5.7x28mm SS197SR
    "5cc80f79e4a949033c7343b2" to "610000", //5.7x28mm SS198LF
    "5e81f423763d9f754677bf2e" to "651000", //.45 ACP Match FMJ
    "5ea2a8e200685063ec28c05a" to "100000", //.45 ACP RIP
    "5efb0fc6aeb21837e749c801" to "630000", //.45 ACP Hydra-Shok
    "5efb0d4f4bc50b58e81710f3" to "651000", //.45 ACP Lasermatch FMJ
    "5efb0cabfb3e451d70735af5" to "666532", //.45 ACP AP
    "5cadf6ddae9215051e1c23b2" to "665210", //12.7x55 mm PS12
    "5cadf6e5ae921500113bb973" to "600000", //12.7x55 mm PS12A
    "5cadf6eeae921500134b2799" to "666654", //12.7x55 mm PS12B
    "5ede474b0c226a66f5402622" to "------", //40x46mm M381(HE) grenade
    "5ede475b549eed7c6d5c18fb" to "------", //40x46mm M386 (HE) grenade
    "5ede4739e0350d05467f73e8" to "------", //40x46mm M406 (HE) grenade
    "5ede47405b097655935d7d16" to "------", //40x46mm M441(HE) grenade
    "5ede475339ee016e8c534742" to "------", //40x46mm M576 (MP-APERS) grenade
    "59e6542b86f77411dc52a77a" to "664100", //.366 TKM FMJ
    "59e655cb86f77411dc52a77b" to "666310", //.366 TKM Eko
    "59e6658b86f77411d949b250" to "630000", //.366 TKM Geksa
    "5f0596629e22f464da6bbdd9" to "666654", //.366 TKM AP-M
    "5d6e6891a4b9361bd473feea" to "651000", //12/70 "Poleva-3" Slug
    "5d6e689ca4b9361bc8618956" to "662000", //12/70 "Poleva-6u" Slug
    "5d6e6772a4b936088465b17c" to "333333", //12/70 5.25mm Buckshot
    "5d6e6806a4b936088465b17e" to "333333", //12/70 8.5mm "Magnum" Buckshot
    "5d6e68a8a4b9360b6c0d54e2" to "666543", //12/70 AP-20 Slug
    "5d6e68dea4b9361bcc29e659" to "652000", //12/70 Dual Sabot Slug
    "5d6e68e6a4b9361c140bcfe0" to "662000", //12/70 FTX Custom Lite Slug
    "5d6e6911a4b9361bd5780d52" to "666555", //12/70 Flechette
    "5d6e6869a4b9361c140bcfde" to "620000", //12/70 Grizzly 40 Slug
    "5d6e68d1a4b93622fe60e845" to "000000", //12/70 SuperFormance HP Slug
    "5d6e68b3a4b9361bca7e50b5" to "631000", //12/70 Copper Sabot Premier HP Slug
    "5d6e67fba4b9361bc73bc779" to "333333", //12/70 6.5mm Express buckshot
    "560d5e524bdc2d25448b4571" to "333333", //12/70 7mm buckshot
    "58820d1224597753c90aeb13" to "641000", //12/70 Lead slug
    "5d6e68c4a4b9361b93413f79" to "665310", //12/70 shell with .50 BMG bullet
    "5c0d591486f7744c505b416f" to "000000", //12/70 RIP
    "5d6e695fa4b936359b35d852" to "333333", //20/70 5.6mm Buckshot
    "5d6e69b9a4b9361bc8618958" to "333333", //20/70 6.2mm Buckshot
    "5d6e69c7a4b9360b6c0d54e4" to "333333", //20/70 7.3mm Buckshot
    "5d6e6a5fa4b93614ec501745" to "100000", //20/70 Devastator Slug
    "5d6e6a53a4b9361bd473feec" to "620000", //20/70 "Poleva-3" slug
    "5d6e6a42a4b9364f07165f52" to "651000", //20/70 "Poleva-6u" slug
    "5d6e6a05a4b93618084f58d0" to "651000", //20/70 Star Slug
    "5a38ebd9c4a282000d722a5b" to "333333", //20/70 7.5mm Buckshot
    "5ba26812d4351e003201fef1" to "621000", //4.6x30mm Action SX
    "5ba2678ad4351e44f824b344" to "666643", //4.6x30mm FMJ SX
    "5ba26835d4351e0035628ff5" to "666665", //4.6x30mm AP SX
    "5ba26844d4351e00334c9475" to "666532", //4.6x30mm Subsonic SX
    "56dff3afd2720bba668b4567" to "664100", //5.45x39mm PS gs
    "56dff4a2d2720bbd668b456a" to "661000", //5.45x39mm T gs
    "56dff338d2720bbd668b4569" to "620000", //5.45x39mm PRS gs
    "56dff2ced2720bb4668b4567" to "666320", //5.45x39mm PP gs
    "56dff216d2720bbd668b4568" to "610000", //5.45x39mm HP
    "56dff421d2720b5f5a8b4567" to "610000", //5.45x39mm SP
    "56dff0bed2720bb0668b4567" to "651000", //5.45x39mm FMJ
    "56dff4ecd2720b5f5a8b4568" to "630000", //5.45x39mm US gs
    "56dfef82d2720bbd668b4567" to "666431", //5.45x39mm BP gs
    "56dff026d2720bb8668b4567" to "666665", //5.45x39mm BS gs
    "56dff061d2720bb5668b4567" to "666543", //5.45x39mm BT gs
    "5c0d5e4486f77478390952fe" to "666666", //5.45x39mm PPBS gs "Igolnik"
    "54527a984bdc2d4e668b4567" to "666320", //5.56x45mm M855
    "54527ac44bdc2d36668b4567" to "666654", //5.56x45mm M855A1
    "59e6920f86f77411d82aa167" to "664100", //5.56x45mm FMJ
    "59e6927d86f77411da468256" to "500000", //5.56x45mm HP
    "59e68f6f86f7746c9f75e846" to "663100", //5.56x45mm M856
    "59e6906286f7746c9f75e847" to "666543", //5.56x45mm M856A1
    "59e6918f86f7746c9f75e849" to "641000", //5.56x45mm MK 255 Mod 0 (RRLP)
    "5c0d5ae286f7741e46554302" to "100000", //5.56x45mm Warmageddon
    "59e690b686f7746c9f75e848" to "666665", //5.56x45mm M995
    "60194943740c5d77f6705eea" to "662000", //5.56x45mm MK 318 Mod 0 (SOST)
    "601949593ae8f707c4608daa" to "666665", //5.56x45mm SSA AP
    "5736026a245977644601dc61" to "630000", //7.62x25mm TT P gl
    "5735fdcd2459776445391d61" to "620000", //7.62x25mm TT AKBS
    "5735ff5c245977640e39ba7e" to "610000", //7.62x25mm TT FMJ43
    "573602322459776445391df1" to "500000", //7.62x25mm TT LRNPC
    "573601b42459776410737435" to "500000", //7.62x25mm TT LRN
    "573603562459776430731618" to "664100", //7.62x25mm TT Pst gzh
    "573603c924597764442bd9cb" to "630000", //7.62x25mm TT PT gzh
    "5656d7c34bdc2d9d198b4587" to "666432", //7.62x39mm PS gzh
    "59e4d3d286f774176a36250a" to "641000", //7.62x39mm HP
    "59e4d24686f7741776641ac7" to "665310", //7.62x39mm US gzh
    "59e4cf5286f7741778269d8a" to "666310", //7.62x39mm T-45M1 gzh
    "59e0d99486f7744a32234762" to "666654", //7.62x39mm BP gzh
    "601aa3d2b2bcb34913271e6d" to "666665", //7.62x39mm MAI AP
    "5e023e53d4353e3302577c4c" to "666200", //7.62x51mm BCP FMJ
    "58dd3ad986f77403051cba8f" to "666654", //7.62x51mm M80
    "5a6086ea4f39f99cd479502f" to "666666", //7.62x51mm M61
    "5a608bf24f39f98ffc77720e" to "666655", //7.62x51mm M62
    "5e023e6e34d52a55c3304f71" to "666520", //7.62x51mm TCW SP
    "5e023e88277cce2b522ff2b1" to "640000", //7.62x51mm Ultra Nosler
    "5efb0c1bd79ff02a1f5e68d9" to "666666", //7.62x51mm M993
    "59e77a2386f7742ee578960a" to "666655", //7.62x54R PS
    "5e023d34e8a400319a28ed44" to "666666", //7.62x54R BT gzh
    "5e023d48186a883be655e551" to "666666", //7.62x54R BS
    "5887431f2459777e1612938f" to "666643", //7.62x54R LPS gzh
    "560d61e84bdc2da74d8b4571" to "666666", //7.62x54R SNB
    "5e023cf8186a883be655e54f" to "666643", //7.62x54R T-46M
    "573719762459775a626ccbc1" to "200000", //9x18mm PM P gzh
    "57371f2b24597761224311f1" to "300000", //9x18mm PM PS gs PPO
    "5737201124597760fc4431f1" to "610000", //9x18mm PM Pst gzh
    "57371f8d24597761006c6a81" to "200000", //9x18mm PM PSO gzh
    "57371e4124597760ff7b25f1" to "510000", //9x18mm PM PPT gzh
    "573718ba2459775a75491131" to "651000", //9x18mm PM BZhT gzh
    "57371b192459775a9f58a5e0" to "400000", //9x18mm PM PPe gzh
    "57371eb62459776125652ac1" to "300000", //9x18mm PM PRS gs
    "5737207f24597760ff7b25f2" to "000000", //9x18mm PM PSV
    "573719df2459775a626ccbc2" to "665100", //9x18mm PM PBM gzh
    "57371aab2459775a77142f22" to "664000", //9x18mm PMM PstM gzh
    "57372140245977611f70ee91" to "000000", //9x18mm PM SP7 gzh
    "5737218f245977612125ba51" to "000000", //9x18mm PM SP8 gzh
    "573720e02459776143012541" to "620000", //9x18mm PM RG028 gzh
    "56d59d3ad2720bdb418b4577" to "662000", //9x19mm Pst gzh
    "58864a4f2459770fcc257101" to "620000", //9x19mm PSO gzh
    "5a3c16fe86f77452b62de32a" to "620000", //9x19mm Luger CCI
    "5c925fa22e221601da359b7b" to "666421", //9x19mm AP 6.3
    "5c3df7d588a4501f290594e5" to "631000", //9x19mm T gzh
    "5c0d56a986f774449d5de529" to "000000", //9x19mm RIP
    "5efb0da7a29a85116f6ea05f" to "666543", //9x19mm PBP gzh
    "5efb0e16aeb21837e749c7ff" to "610000", //9x19mm QuakeMaker
    "5a269f97c4a282000b151807" to "666432", //9x21mm PS gzh
    "5a26abfac4a28232980eabff" to "630000", //9x21mm P gzh
    "5a26ac06c4a282000c5a90a8" to "620000", //9x21mm PE gzh
    "5a26ac0ec4a28200741e1e18" to "666543", //9x21mm BT gzh
    "57a0dfb82459774d3078b56c" to "666532", //9x39 mm SP5 gs
    "57a0e5022459774d1673f889" to "666654", //9x39 mm SP6 gs
    "5c0d668f86f7747ccb7f13b2" to "666665", //9x39mm SPP gs
    "5c0d688c86f77413ae3407b2" to "666665", //9x39mm BP gs
    "5fd20ff893a8961fc660a954" to "666654", //.300 AAC Blackout AP
    "5fbe3ffdf8b6a877a729ea82" to "665320", //.300 AAC Blackout BCP FMJ
    "5fc382a9d724d907e2077dab" to "666666", //.338 Lapua Magnum AP
    "5fc275cf85fd526b824a571a" to "666655", //.338 Lapua Magnum FMJ
    "5fc382b6d6fa9c00c571bbc3" to "653100", //.338 Lapua Magnum TAC-X
    "5fc382c1016cce60e8341b20" to "666542", //.338 Lapua Magnum UPZ
    "5e85aa1a988a8701445df1f5" to "666644", //23x75mm "Barrikada" slug
    "5e85a9f4add9fe03027d9bf1" to "000000", //23x75mm "Zvezda" flashbang round
    "5e85a9a6eacf8c039e4e2ac1" to "643333", //23x75mm "Shrapnel-10" buckshot
    "5f647f31b6238e5dd066e196" to "643333", //23x75mm Shrapnel-25 buckshot
)

val Armor0 = Color(0xffce0b04)
val Armor1 = Color(0xffdc3b07)
val Armor2 = Color(0xffea6c0a)
val Armor3 = Color(0xfff99d0e)
val Armor4 = Color(0xffc0b825)
val Armor5 = Color(0xff86d43d)
val Armor6 = Color(0xff4bf056)