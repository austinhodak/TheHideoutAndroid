package com.austinhodak.thehideout.flea_market.models

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemFleaBinding
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.hideout.models.Input
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

data class FleaItem(
    val avg24hPrice: Int? = null,
    val avg7daysPrice: Int? = null,
    val basePrice: Int? = null,
    val bsgId: String? = null,
    val diff24h: Double? = null,
    val diff7days: Double? = null,
    val icon: String? = null,
    val img: String? = null,
    val imgBig: String? = null,
    val isFunctional: Boolean? = null,
    val link: String? = null,
    val name: String? = null,
    val price: Int? = null,
    val reference: String? = null,
    val shortName: String? = null,
    val slots: Int? = 1,
    val traderName: String? = null,
    val traderPrice: Int? = null,
    val traderPriceCur: String? = null,
    val uid: String? = null,
    val updated: String? = null,
    val wikiLink: String? = null,
) : AbstractBindingItem<ItemFleaBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_id

    override fun bindView(binding: ItemFleaBinding, payloads: List<Any>) {
        Glide.with(binding.root.context).load(getItemIcon()).into(binding.fleaItemIcon)

        binding.item = this

        val resources = binding.root.resources
        //Switch to binding?
        when {
            diff24h!! > 0.0 -> {
                binding.fleaItemChange.setTextColor(resources.getColor(R.color.md_green_500))
                binding.fleaItemChangeIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                binding.fleaItemChangeIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
            }
            diff24h < 0.0 -> {
                binding.fleaItemChange.setTextColor(resources.getColor(R.color.md_red_500))
                binding.fleaItemChangeIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                binding.fleaItemChangeIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
            }
            else -> {
                binding.fleaItemChange.setTextColor(resources.getColor(R.color.primaryText60))
                binding.fleaItemChangeIcon.setImageResource(R.drawable.icons8_horizontal_line_96)
                binding.fleaItemChangeIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText60))
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemFleaBinding {
        return ItemFleaBinding.inflate(inflater, parent, false)
    }

    fun get24Diff(): String {
        return "$diff24h%"
    }

    fun getCurrentPrice(): String {
        return price?.getPrice("₽")!!
    }

    fun getCurrentTraderPrice(): String {
        return traderPrice?.getPrice(traderPriceCur!!)!!
    }

    fun getPricePerSlot(): String {
        return "${(price!!/slots!!).getPrice("₽")}/slot"
    }

    fun get24hPrice(): String {
        return avg24hPrice?.getPrice("₽")!!
    }

    fun getAvg7dPrice(): String {
        return avg7daysPrice?.getPrice("₽")!!
    }

    fun getItemIcon(): String {
        return if (icon.isNullOrEmpty() && img.isNullOrEmpty()) {
            ""
        } else if (icon.isNullOrEmpty()) {
            img ?: ""
        } else if (img.isNullOrEmpty()) {
            icon
        } else icon
    }

    fun getUpdatedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return "Updated ${DateUtils.getRelativeTimeSpanString(sdf.parse(updated).time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)}"
    }

    fun getTotalCostToCraft(input: List<Input>): Int {
        var total = 0.0

        for (i in input) {
            val item = i.fleaItem
            val cost = item.price!! * i.qty
            total += cost
        }

        return total.roundToInt()
    }

    fun calculateTax(salePrice: Long = price?.toLong() ?: (0).toLong(), callback: (result: Int) -> Unit) {
        val mVO = basePrice!!.toDouble()
        val mVR = salePrice.toDouble()
        val mTi = 0.05
        val mTr = 0.05
        val mQ = 1
        val mPO = log10((mVO / mVR))
        val mPR = log10((mVR / mVO))

        val mPO4 = if (mVO > mVR) {
            Math.pow(4.0, mPO.pow(1.08))
        } else {
            Math.pow(4.0, mPO)
        }

        val mPR4 = if (mVR > mVO) {
            Math.pow(4.0, mPR.pow(1.08))
        } else {
            Math.pow(4.0, mPR)
        }

        val tax = (mVO * mTi * mPO4 * mQ + mVR * mTr * mPR4 * mQ).roundToInt()
        callback.invoke(tax)
    }


}