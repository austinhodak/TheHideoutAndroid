package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.asCurrency
import com.austinhodak.tarkovapi.fragment.ItemFragment

@Entity(tableName = "itemPrices")
data class PriceItem(
    @PrimaryKey val id: String,
    val priceItemFragment: ItemFragment? = null
) {
    fun name(): String? = priceItemFragment?.name
    fun price(): String? = priceItemFragment?.lastLowPrice?.asCurrency()
    fun slots(): Int? = priceItemFragment?.width?.times(priceItemFragment.height)
    fun pricePerSlot(): String? = priceItemFragment?.lastLowPrice?.div(slots() ?: 1)?.asCurrency()
}