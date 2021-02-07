package com.austinhodak.thehideout

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

fun Int.getPrice(currency: String): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun Long.getPrice(currency: String): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

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