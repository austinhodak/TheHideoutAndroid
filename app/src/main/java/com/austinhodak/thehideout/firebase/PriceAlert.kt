package com.austinhodak.thehideout.firebase

import androidx.annotation.DrawableRes
import com.austinhodak.thehideout.R
import com.google.firebase.database.DatabaseReference

data class PriceAlert(
    var uid: String? = null,
    var itemID: String? = null,
    var price: Long? = null,
    var condition: String? = null,
    var enabled: Boolean? = null,
    var persistent: Boolean? = null,
    var token: String? = null,
    var reference: DatabaseReference? = null
) {
    fun getConditionString(): String? {
        return when (condition) {
            "above" -> "GREATER THAN"
            "below" -> "LESS THAN"
            else -> condition
        }
    }

    @DrawableRes
    fun getConditionIcon(): Int {
        return when (condition) {
            "above" -> {
                return R.drawable.icons8_greater_than_96
                if (persistent == true) {
                    R.drawable.icons8_greater_than_96_persist
                } else {
                    R.drawable.icons8_greater_than_96
                }
            }
            "below" -> {
                return R.drawable.icons8_less_than_96
                if (persistent == true) {
                    R.drawable.icons8_less_than_96_persist
                } else {
                    R.drawable.icons8_less_than_96
                }
            }
            else -> R.drawable.icons8_less_than_96
        }
    }
}