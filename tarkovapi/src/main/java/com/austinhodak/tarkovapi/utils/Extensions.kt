package com.austinhodak.tarkovapi.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.QuestsQuery
import com.austinhodak.tarkovapi.room.enums.ItemTypes
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
        quest.requirements,
        quest.reputation?.map { it.fragments.repFragment },
        quest.objectives.map { it?.fragments?.objectiveFragment!! }
    )
}

fun ItemsByTypeQuery.ItemsByType.toPricing(): Pricing {
    val item = this.fragments.itemFragment
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
        else -> ItemTypes.NONE
    }
}