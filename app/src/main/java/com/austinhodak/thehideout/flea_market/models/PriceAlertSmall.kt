package com.austinhodak.thehideout.flea_market.models

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemFleaDetailPriceAlertBinding
import com.austinhodak.thehideout.utils.getPrice
import com.google.firebase.database.DatabaseReference
import com.mikepenz.fastadapter.binding.AbstractBindingItem

/**
 * @param price The alert price
 * @param uid User ID
 * @param itemID Items ID
 * @param when Either `above` or `below`.
 * @param token Firebase token for user.
 */

data class PriceAlertSmall (
    val price: Int? = null,
    val uid: String? = null,
    val itemID: String? = null,
    val `when`: String? = null,
    val token: String? = null,
    var reference: DatabaseReference? = null
) : AbstractBindingItem<ItemFleaDetailPriceAlertBinding>() {
    override val type: Int
        get() = R.id.fast_adapter_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemFleaDetailPriceAlertBinding {
        return ItemFleaDetailPriceAlertBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemFleaDetailPriceAlertBinding, payloads: List<Any>) {
        binding.alert = this
    }

    fun getPriceString(): String? {
        return price?.getPrice("₽")
    }

    fun getWhenText(): String {
        return when(`when`) {
            "above" -> "When price rises above"
            "below" -> "When price drops below"
            else -> ""
        }
    }
}