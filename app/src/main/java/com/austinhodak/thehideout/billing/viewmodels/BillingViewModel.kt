package com.austinhodak.thehideout.billing.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    @ApplicationContext context: Context
): ViewModel() {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { _, _ ->

    }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    val itemsList = MutableLiveData<MutableList<SkuDetails>>(mutableListOf())

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    viewModelScope.launch {
                        querySkuSubs()
                        querySkuDetails()
                    }
                }
                Timber.d(billingResult.responseCode.toString())
            }
        })
    }

    suspend fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("donation_1")
        skuList.add("donation_2")
        skuList.add("donation_3")
        skuList.add("donation_4")
        skuList.add("donation_5")
        skuList.add("donation_nice")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        Timber.d(skuDetailsResult.skuDetailsList.toString())

        val items = itemsList.value ?: mutableListOf()

        skuDetailsResult.skuDetailsList?.let {
            items.addAll(it)
            itemsList.value = items
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

        val items = itemsList.value ?: mutableListOf()

        skuDetailsResult.skuDetailsList?.let {
            items.addAll(it)
            itemsList.value = items
        }
    }
}