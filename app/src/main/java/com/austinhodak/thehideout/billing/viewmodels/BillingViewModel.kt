package com.austinhodak.thehideout.billing.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.austinhodak.thehideout.billing.BillingClientWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    billingClientWrapper: BillingClientWrapper
): ViewModel() {

    val subList = MutableLiveData<MutableList<SkuDetails>>(mutableListOf())
    val itemList = MutableLiveData<MutableList<SkuDetails>>(mutableListOf())

    init {
        billingClientWrapper.queryProducts(object : BillingClientWrapper.OnQueryProductsListener {
            override fun onSuccess(products: List<SkuDetails>) {
                products.filter { it.type == BillingClient.SkuType.INAPP }.let {
                    Timber.d(it.toString())
                    val items = itemList.value ?: mutableListOf()
                    items.addAll(it)
                    itemList.value = items
                }

                products.filter { it.type == BillingClient.SkuType.SUBS }.let {
                    Timber.d(it.toString())
                    val items = itemList.value ?: mutableListOf()
                    items.addAll(it)
                    subList.value = items
                }
            }

            override fun onFailure(error: BillingClientWrapper.Error) {

            }
        })
    }

   /* suspend fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("donation_1")
        skuList.add("donation_2")
        skuList.add("donation_3")
        skuList.add("donation_4")
        skuList.add("donation_5")
        skuList.add("donation_6")
        skuList.add("donation_nice")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        val items = itemList.value ?: mutableListOf()

        skuDetailsResult.skuDetailsList?.let {
            items.addAll(it)
            itemList.value = items
        }
    }

    suspend fun querySkuSubs() {
        val skuList = ArrayList<String>()
        skuList.add("premium_1")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        val items = subList.value ?: mutableListOf()

        skuDetailsResult.skuDetailsList?.let {
            items.addAll(it)
            subList.value = items
        }
    }*/
}