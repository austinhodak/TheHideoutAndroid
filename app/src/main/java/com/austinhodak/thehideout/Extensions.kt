package com.austinhodak.thehideout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

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
