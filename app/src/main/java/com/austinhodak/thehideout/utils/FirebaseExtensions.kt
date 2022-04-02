package com.austinhodak.thehideout.utils

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

fun log(event: String, itemID: String, itemName: String, contentType: String) {
    Firebase.analytics.logEvent(event) {
        param(FirebaseAnalytics.Param.ITEM_ID, itemID)
        param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
    }
}

fun logNotification(event: String, itemName: String, contentType: String) {
    Firebase.analytics.logEvent(event) {
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

val questsFirebase = Firebase.database("https://hideout-tracker.firebaseio.com").reference
val fleaFirebase = Firebase.database("https://hideout-flea-market.firebaseio.com").reference
fun userRefTracker(ref: String? = null): DatabaseReference {
    return questsFirebase.child("users/${Firebase.auth.uid}/$ref/")
}

fun uid(): String? {
    val uid = Firebase.auth.currentUser?.uid
    Timber.d("1 - $uid")
    return uid
}

fun pushToken(token: String) {
    if (Firebase.auth.currentUser != null) {
        userRefTracker("token").setValue(token)
    }
}