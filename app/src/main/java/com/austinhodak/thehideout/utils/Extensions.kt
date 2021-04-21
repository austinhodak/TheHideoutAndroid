package com.austinhodak.thehideout

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.util.*
import kotlin.math.round

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
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
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(this))
}

fun getCurrencyString(string: String): String {
    return when (string) {
        "$" -> "USD"
        "€" -> "EURO"
        "₽" -> "RUB"
        else -> "RUB"
    }
}

fun userRef(ref: String? = null): DatabaseReference {
    return Firebase.database.getReference("users/${Firebase.auth.uid}/$ref/")
}

fun uid(): String? {
    return Firebase.auth.uid
}

fun pushToken(token: String) {
    if (Firebase.auth.currentUser != null)
    Firebase.database.getReference("users/${Firebase.auth.uid}/").updateChildren(hashMapOf<String, Any>("token" to token))
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
        "find" -> R.drawable.ic_baseline_location_searching_24
        "reputation" -> R.drawable.ic_baseline_thumb_up_24
        "warning" -> R.drawable.ic_baseline_warning_24
        "skill" -> R.drawable.ic_baseline_fitness_center_24
        else -> R.drawable.icons8_sniper_96
    }
}