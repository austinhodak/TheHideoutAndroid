package com.austinhodak.thehideout.viewmodels.models

import com.google.firebase.database.DatabaseReference

/**
 * @param price The alert price
 * @param uid User ID
 * @param itemID Items ID
 * @param when Either `above` or `below`.
 * @param token Firebase token for user.
 */

data class PriceAlert (
    val price: Int? = null,
    val uid: String? = null,
    val itemID: String? = null,
    val `when`: String? = null,
    val token: String? = null,
    var reference: DatabaseReference? = null
) {
    fun getWhenText(): String {
        return when(`when`) {
            "above" -> "Alert when price rises above"
            "below" -> "Alert when price drops below"
            else -> ""
        }
    }
}