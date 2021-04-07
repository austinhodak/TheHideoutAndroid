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