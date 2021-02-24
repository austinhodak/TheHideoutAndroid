package com.austinhodak.thehideout.flea_market.models

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemFleaPriceAlertBinding
import com.austinhodak.thehideout.getPrice
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.mikepenz.fastadapter.binding.AbstractBindingItem

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
    var reference: DatabaseReference? = null,
    var fleaItem: FleaItem? = null
) : AbstractBindingItem<ItemFleaPriceAlertBinding>() {

    fun getWhenText(): String {
        return when(`when`) {
            "above" -> "Alert when price rises above"
            "below" -> "Alert when price drops below"
            else -> ""
        }
    }

    fun getPriceString(): String {
        return price?.getPrice("₽")!!
    }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemFleaPriceAlertBinding {
        return ItemFleaPriceAlertBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemFleaPriceAlertBinding, payloads: List<Any>) {
        binding.alert = this
        Glide.with(binding.root.context).load(fleaItem?.getItemIcon()).into(binding.fleaItemIcon)
    }
}