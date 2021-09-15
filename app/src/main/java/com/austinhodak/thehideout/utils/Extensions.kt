package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.type.ItemSourceName
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.compose.theme.Red500
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt


fun String.getMedIcon(): Int? {
    return when (this) {
        "LightBleeding" -> R.drawable.light_bleeding_icon
        "HeavyBleeding" -> R.drawable.heavy_bleeding_icon
        "FreshWounds" -> R.drawable.fresh_wound_icon
        "Fracture" -> R.drawable.fracture_icon
        "Pain" -> R.drawable.pain_icon
        "Contusion" -> R.drawable.contusion_icon
        "Tremor" -> R.drawable.tremor_icon
        else -> null
    }
}

fun Double.getColor(reverse: Boolean = false, surfaceColor: Color): Color {
    return if (this == 0.0 || this.isNaN()) {
        surfaceColor
    } else if (this > 0.0) {
        if (reverse) Red500 else Green500
    } else {
        if (reverse) Green500 else Red500
    }
}

fun Ammo.toCAmmo(): CAmmo {

    val b = this.ballistics?.projectileCount ?: 1
    return CAmmo(
        bullets = b,
        damage = this.ballistics?.damage?.toDouble()?.times(b) ?: 0.0,
        penetration = this.ballistics?.penetrationPower ?: 0.0,
        armorDamage = (this.ballistics?.armorDamage?.toDouble()?.times(b) ?: 0.0) / 100
    )
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
    Timber.d(this.toString())
    when {
        requirements.isNullOrEmpty() -> {
            return when (this.source) {
                ItemSourceName.prapor.rawValue -> "https://tarkov-tools.com/images/prapor-icon.jpg"
                ItemSourceName.therapist.rawValue -> "https://tarkov-tools.com/images/therapist-icon.jpg"
                ItemSourceName.fence.rawValue -> "https://tarkov-tools.com/images/fence-icon.jpg"
                ItemSourceName.skier.rawValue -> "https://tarkov-tools.com/images/skier-icon.jpg"
                ItemSourceName.peacekeeper.rawValue -> "https://tarkov-tools.com/images/peacekeeper-icon.jpg"
                ItemSourceName.mechanic.rawValue -> "https://tarkov-tools.com/images/mechanic-icon.jpg"
                ItemSourceName.ragman.rawValue -> "https://tarkov-tools.com/images/ragman-icon.jpg"
                ItemSourceName.jaeger.rawValue -> "https://tarkov-tools.com/images/jaeger-icon.jpg"
                else -> "https://tarkov-tools.com/images/prapor-icon.jpg"
            }
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

fun Int.convertRtoUSD(): Int {
    return (this / 116.0).roundToInt()
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

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

fun String.getCurrency(): String {
    return when (this) {
        "R" -> "₽"
        "D" -> "$"
        "E" -> "€"
        else -> ""
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

fun Int.getPrice(c: String): String {
    val currency = c.replace("R", "₽").replace("D", "$").replace("E", "€")
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun Double.getPrice(c: String): String {
    val currency = c.replace("R", "₽").replace("D", "$").replace("E", "€")
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun Long.getPrice(c: String): String {
    val currency = c.replace("R", "₽").replace("D", "$").replace("E", "€")
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun String.openWithCustomTab(context: Context) {
    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(this))
}

fun getCurrencyString(string: String): String {
    return when (string) {
        "$" -> "USD"
        "€" -> "EURO"
        "₽" -> "RUB"
        else -> "RUB"
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

fun userRef(ref: String? = null): DatabaseReference {
    return Firebase.database.getReference("users/${Firebase.auth.uid}/$ref/")
}

fun userRefTracker(ref: String? = null): DatabaseReference {
    return questsFirebase.child("users/${Firebase.auth.uid}/$ref/")
}

fun uid(): String? {
    return Firebase.auth.uid
}

fun pushToken(token: String) {
    if (Firebase.auth.currentUser != null)
        Firebase.database.getReference("users/${Firebase.auth.uid}/")
            .updateChildren(hashMapOf<String, Any>("token" to token))
}

fun log(event: String, itemID: String, itemName: String, contentType: String) {
    Firebase.analytics.logEvent(event) {
        param(FirebaseAnalytics.Param.ITEM_ID, itemID)
        param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
    }
}

fun logScreen(name: String) {
    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        param(FirebaseAnalytics.Param.SCREEN_NAME, name)
        param(FirebaseAnalytics.Param.SCREEN_CLASS, name)
    }
}

fun isDebug(): Boolean {
    return BuildConfig.DEBUG
}

fun Int.addQuotes(): String {
    return "\"$this\""
}

fun Any.addQuotes(): String {
    return "\"$this\""
}

@DrawableRes
fun String.getObjectiveIcon(): Int {
    return when (this.toLowerCase()) {
        "kill" -> R.drawable.icons8_sniper_96
        "collect" -> R.drawable.ic_baseline_swap_horizontal_circle_24
        "pickup" -> R.drawable.icons8_upward_arrow_96
        "key" -> R.drawable.icons8_key_100
        "place" -> R.drawable.icons8_low_importance_96
        "mark" -> R.drawable.icons8_low_importance_96
        "locate" -> R.drawable.ic_baseline_location_searching_24
        "find" -> R.drawable.ic_baseline_check_circle_outline_24
        "reputation" -> R.drawable.ic_baseline_thumb_up_24
        "warning" -> R.drawable.ic_baseline_warning_24
        "skill" -> R.drawable.ic_baseline_fitness_center_24
        else -> R.drawable.icons8_sniper_96
    }
}

@DrawableRes
fun getTraderIcon(trader: Traders): Int {
    return when (trader.id) {
        "Prapor" -> R.drawable.prapor_portrait
        "Therapist" -> R.drawable.therapist_portrait
        "Fence" -> R.drawable.fence_portrait
        "Skier" -> R.drawable.skier_portrait
        "Peacekeeper" -> R.drawable.peacekeeper_portrait
        "Mechanic" -> R.drawable.mechanic_portrait
        "Ragman" -> R.drawable.ragman_portrait
        "Jaeger" -> R.drawable.jaeger_portrait
        else -> R.drawable.jaeger_portrait
    }
}

fun hasInternet(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun String.modParent(): String {
    return when (this) {
        "bipod" -> "55818afb4bdc2dde698b456d"
        "foregrips" -> "55818af64bdc2d5b648b4570"
        "flashlights" -> "55818b084bdc2d5b648b4571"
        "tac" -> "55818b164bdc2ddc698b456c"
        "aux" -> "550aa4dd4bdc2dc9348b4569"
        "muzzle_flash",
        "muzzle_brakes",
        "muzzle_adapters" -> "550aa4bf4bdc2dd6348b456b"
        "muzzle_suppressor" -> "550aa4cd4bdc2dd8348b456c"
        "gear_handles" -> "55818a6f4bdc2db9688b456b"
        "gear_mags" -> "5448bc234bdc2d3c308b4569"
        "gear_mounts" -> "55818b224bdc2dde698b456f"
        "gear_stocks" -> "55818a594bdc2db9688b456a"
        "vital_barrels" -> "555ef6e44bdc2de9068b457e"
        "vital_gas" -> "56ea9461d2720b67698b456f"
        "vital_handguards" -> "55818a104bdc2db9688b4569"
        "vital_grips" -> "55818a684bdc2ddd698b456d"
        "vital_receivers" -> "55818a304bdc2db5418b457d"
        "sights_assault" -> "55818add4bdc2d5b648b456f"
        "sights_reflex" -> "55818ad54bdc2ddc698b4569"
        "sights_compact" -> "55818acf4bdc2dde698b456b"
        "sights_iron" -> "55818ac54bdc2d5b648b456e"
        "sights_scopes" -> "55818ae44bdc2dde698b456c"
        "sights_special" -> "55818aeb4bdc2ddc698b456a"
        else -> ""
    }
}

fun Modifier.horizontalFadingEdge(
    scrollState: ScrollState,
    length: Dp,
    edgeColor: Color? = null,
) = composed(
    debugInspectorInfo {
        name = "length"
        value = length
    }
) {
    val color = edgeColor ?: MaterialTheme.colors.surface

    Modifier.drawWithContent {
        val lengthValue = length.toPx()
        val scrollFromStart = scrollState.value
        val scrollFromEnd = scrollState.maxValue - scrollState.value

        val startFadingEdgeStrength = lengthValue * (scrollFromStart / lengthValue).coerceAtMost(1f)

        val endFadingEdgeStrength = lengthValue * (scrollFromEnd / lengthValue).coerceAtMost(1f)

        drawContent()

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    color,
                    Color.Transparent,
                ),
                startX = 0f,
                endX = startFadingEdgeStrength,
            ),
            size = Size(
                startFadingEdgeStrength,
                this.size.height,
            ),
        )

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color,
                ),
                startX = size.width - endFadingEdgeStrength,
                endX = size.width,
            ),
            topLeft = Offset(x = size.width - endFadingEdgeStrength, y = 0f),
        )
    }
}

fun Modifier.verticalFadingEdge(
    scrollState: ScrollState,
    length: Dp,
    edgeColor: Color? = null,
) = composed(
    debugInspectorInfo {
        name = "length"
        value = length
    }
) {
    val color = edgeColor ?: MaterialTheme.colors.surface

    Modifier.drawWithContent {
        val lengthValue = length.toPx()
        val scrollFromTop = scrollState.value
        val scrollFromBottom = scrollState.maxValue - scrollState.value

        val topFadingEdgeStrength = lengthValue * (scrollFromTop / lengthValue).coerceAtMost(1f)

        val bottomFadingEdgeStrength = lengthValue * (scrollFromBottom / lengthValue).coerceAtMost(1f)

        drawContent()

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color,
                    Color.Transparent,
                ),
                startY = 0f,
                endY = topFadingEdgeStrength,
            ),
            size = Size(
                this.size.width,
                topFadingEdgeStrength
            ),
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    color,
                ),
                startY = size.height - bottomFadingEdgeStrength,
                endY = size.height,
            ),
            topLeft = Offset(x = 0f, y = size.height - bottomFadingEdgeStrength),
        )
    }
}

val questsFirebase = Firebase.database("https://hideout-tracker.firebaseio.com").reference