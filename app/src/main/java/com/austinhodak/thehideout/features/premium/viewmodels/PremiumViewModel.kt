package com.austinhodak.thehideout.features.premium.viewmodels

import android.app.Activity
import coil.annotation.ExperimentalCoilApi
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.hilt.AssistedViewModelFactory
import com.airbnb.mvrx.hilt.hiltMavericksViewModelFactory
import com.austinhodak.thehideout.features.premium.PremiumThanksActivity
import com.austinhodak.thehideout.utils.openActivity
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QEntitlement
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.QonversionErrorCode
import com.qonversion.android.sdk.dto.offerings.QOffering
import com.qonversion.android.sdk.dto.offerings.QOfferings
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionOfferingsCallback
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import timber.log.Timber


class PremiumViewModel @AssistedInject constructor(
    @Assisted initialState: PremiumPusherState
): MavericksViewModel<PremiumPusherState>(initialState) {

    init {
        findOfferings()
    }

    private fun findOfferings() {
        Qonversion.shared.offers { offers, error ->
            offers?.let {
                val mainOffering = offers.main
                mainOffering?.let {
                    setState {
                        copy(mainOffering = mainOffering)
                    }
                }
            }

            error?.let {
                setState {
                    copy(snackbarText = error.toString())
                }
                Timber.e(error.toString())
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    fun purchase(activity: Activity, product: QProduct) {
        setState {
            copy(isProcessingPurchase = true)
        }
        Qonversion.shared.doPurchase(activity, product) { entitlements, error ->
            entitlements?.let {
                launchThanks(activity)
                setState {
                    copy(entitlement = entitlements["Premium"])
                }
                Timber.d("Entitlement: $entitlements")
            }

            error?.let {
                //if (error.code != QonversionErrorCode.CanceledPurchase)
                setState {
                    copy(
                        snackbarText = error.description
                    )
                }
                Timber.e(error.toString())
            }
        }
    }

    fun restorePurchases() {
        Qonversion.shared.restorePurchases { entitlements, error ->
            entitlements?.let {
                setState {
                    copy(entitlement = entitlements["Premium"])
                }
                Timber.d("Entitlement: $entitlements")
            }

            error?.let {
                setState {
                    copy(snackbarText = error.toString())
                }
                Timber.e(error.toString())
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    fun launchThanks(activity: Activity) {
        activity.openActivity(PremiumThanksActivity::class.java) {
            putBoolean("restart", true)
        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<PremiumViewModel, PremiumPusherState> {
        override fun create(state: PremiumPusherState): PremiumViewModel
    }

    companion object : MavericksViewModelFactory<PremiumViewModel, PremiumPusherState> by hiltMavericksViewModelFactory()
}

data class PremiumPusherState(
    val snackbarText: String? = null,
    val mainOffering: QOffering? = null,
    val entitlement: QEntitlement? = null,
    val isProcessingPurchase: Boolean = false
) : MavericksState {
    val isLoading = snackbarText == null && mainOffering == null && !isProcessingPurchase
}

inline fun Qonversion.offers(crossinline onResult: (QOfferings?, QonversionError?) -> Unit): QonversionOfferingsCallback {
    val callback = object : QonversionOfferingsCallback {
        override fun onError(error: QonversionError) {
            onResult(null, error)
        }

        override fun onSuccess(offerings: QOfferings) {
            onResult(offerings, null)
        }
    }
    this.offerings(callback)
    return callback
}

inline fun Qonversion.doPurchase(activity: Activity, product: QProduct, crossinline onResult: (Map<String, QEntitlement>?, QonversionError?) -> Unit): QonversionEntitlementsCallback {
    val callback = object : QonversionEntitlementsCallback {
        override fun onError(error: QonversionError) {
            onResult(null, error)
        }

        override fun onSuccess(entitlements: Map<String, QEntitlement>) {
            onResult(entitlements, null)
        }


    }
    this.purchase(activity, product, callback)
    return callback
}

inline fun Qonversion.restorePurchases(crossinline onResult: (Map<String, QEntitlement>?, QonversionError?) -> Unit): QonversionEntitlementsCallback {
    val callback = object : QonversionEntitlementsCallback {
        override fun onError(error: QonversionError) {
            onResult(null, error)
        }

        override fun onSuccess(entitlements: Map<String, QEntitlement>) {
            onResult(entitlements, null)
        }
    }
    this.restore(callback)
    return callback
}

inline fun Qonversion.checkEntitlement(crossinline onResult: (Map<String, QEntitlement>?, QonversionError?) -> Unit): QonversionEntitlementsCallback {
    val callback = object : QonversionEntitlementsCallback {
        override fun onError(error: QonversionError) {
            onResult(null, error)
        }

        override fun onSuccess(entitlements: Map<String, QEntitlement>) {
            onResult(entitlements, null)
        }
    }
    this.checkEntitlements(callback)
    return callback
}