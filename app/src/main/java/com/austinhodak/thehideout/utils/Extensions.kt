package com.austinhodak.thehideout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.browser.customtabs.CustomTabsIntent
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
    if (currency == "₽") {
        return "${format.format(this).replace("RUB", "")}₽"
    } else {
        return format.format(this)
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

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(block))
}

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun userRef(ref: String? = null): DatabaseReference {
    return Firebase.database.getReference("users/${Firebase.auth.uid}/$ref/")
}

fun flea(): DatabaseReference {
    return Firebase.database("https://hideout-flea-market.firebaseio.com/").reference
}