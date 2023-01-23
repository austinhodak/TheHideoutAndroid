package com.austinhodak.thehideout.features.premium

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.annotation.ExperimentalCoilApi
import com.adapty.Adapty
import com.adapty.models.PaywallModel
import com.android.billingclient.api.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.ui.theme.Bender
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.purchase
import com.austinhodak.thehideout.utils.restartNavActivity
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalPagerApi
@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
class PremiumPusherActivity : ComponentActivity() {
    private lateinit var billingClient: BillingClient

    @ExperimentalPagerApi
    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                restartNavActivity()
                Toast.makeText(this, "Purchase successful! Thank you!", Toast.LENGTH_LONG).show()
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()*/

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ProvideWindowInsets {
                HideoutTheme {
                    val scope = rememberCoroutineScope()
                    val uiController = rememberSystemUiController()
                    uiController.setNavigationBarColor(Color.Transparent)
                    uiController.setStatusBarColor(Color.Transparent)

                    var details: SkuDetails? by remember {
                        mutableStateOf(null)
                    }

                    var paywall: PaywallModel? by remember {
                        mutableStateOf(null)
                    }

                    /*billingClient.startConnection(object : BillingClientStateListener {
                        override fun onBillingServiceDisconnected() {
                            billingClient.startConnection(this)
                        }

                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                val skuList = ArrayList<String>()
                                skuList.add("premium_1")
                                val params = SkuDetailsParams.newBuilder()
                                params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

                                scope.launch {
                                    val skuDetailsResult = withContext(Dispatchers.IO) {
                                        billingClient.querySkuDetails(params.build())
                                    }
                                    details = skuDetailsResult.skuDetailsList?.firstOrNull()
                                }

                            }
                            Timber.d(billingResult.responseCode.toString())
                        }
                    })*/

                    Adapty.getPaywalls { paywalls, products, error ->
                        if (error == null) {
                            paywalls?.find { it.developerId == "premium" }?.let {
                                paywall = it
                            }
                        }
                    }

                    Pusher(paywall)
                }
            }
        }
    }

    @Composable
    fun Pusher(
        paywall: PaywallModel?
    ) {

        val image = if (paywall?.customPayload?.containsKey("background_image") == true) {
            //rememberImagePainter(data = paywall.customPayload?.get("background_image"))
            painterResource(id = R.drawable.g7z8a1hc71f51)
        } else {
            painterResource(id = R.drawable.g7z8a1hc71f51)
        }

        val currentFeatures = if (paywall?.customPayload?.containsKey("features") == true) {
            paywall.customPayload?.get("features") as String
        } else {
            "• Unlimited Loadout Slots <br>• Future access to premium features."
        }

        val comingSoon = if (paywall?.customPayload?.containsKey("coming_soon") == true) {
            paywall.customPayload?.get("coming_soon") as String
        } else {
            "• Custom Map Markers <br>• Tarkov Tracker Integration <br>• Team Quest Tracking <br>• Quest Tracker Integration with Maps <br>• Team Map Collaboration <br>• Discord Role and Special Access <br>• Lots more! "
        }

        var isProcessing by remember { mutableStateOf(false) }

        Image(
            painter = image,
            contentDescription = "Background Image",
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxHeight()
        )
        if (paywall == null || isProcessing) {
            LoadingItem()
        }
        AnimatedVisibility(
            visible = paywall != null && !isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {

                        },
                        backgroundColor = Color.Transparent,
                        modifier = Modifier.statusBarsPadding(),
                        navigationIcon = {
                            IconButton(onClick = {
                                onBackPressed()
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
                            }
                        },
                        elevation = 0.dp,
                        actions = {

                        }
                    )
                },
                backgroundColor = Color.Black.copy(alpha = 0.5f)
            ) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                        .navigationBarsPadding(bottom = true),
                    // .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 0.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(0.dp),
                                painter = painterResource(id = R.drawable.hideout_shadow_1),
                                contentDescription = "Logo"
                            )
                            Column(
                                Modifier.padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "The Hideout",
                                    style = MaterialTheme.typography.h4,
                                    color = Color.White
                                )
                                Text(
                                    text = "Premium",
                                    style = MaterialTheme.typography.h5,
                                    color = Color.White,
                                    fontStyle = FontStyle.Normal,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Red400)
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkGrey.copy(alpha = 0.9f),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                            //.weight(1f)
                        ) {
                            Column(
                                Modifier.padding(16.dp)
                            ) {
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text(
                                        text = "WHAT YOU GET",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = Bender,
                                        color = White
                                    )
                                    MarkdownText(
                                        markdown = currentFeatures,
                                        style = MaterialTheme.typography.body1,
                                        fontResource = R.font.bender,
                                        color = White,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                                    )
                                    Text(
                                        text = "WHAT'S COMING SOON FOR PREMIUM",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = Bender,
                                        color = White
                                    )
                                    MarkdownText(
                                        markdown = comingSoon,
                                        style = MaterialTheme.typography.body1,
                                        fontResource = R.font.bender,
                                        color = White,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        paywall?.products?.sortedBy { it.price }?.filterNot { it.skuDetails == null }?.forEach { product ->
                            Timber.d(product.toString())
                            Button(
                                onClick = {
                                    Adapty.getPurchaserInfo { purchaserInfo, error ->
                                        Timber.d(purchaserInfo.toString())
                                        if (purchaserInfo?.subscriptions?.containsKey("premium_lifetime") == false || purchaserInfo?.subscriptions?.get("premium_lifetime")?.isActive == false) {
                                            isProcessing = true
                                            product.purchase(this@PremiumPusherActivity) { purchaserInfo, purchaseToken, googleValidationResult, product, e ->
                                                if (e == null) {
                                                    if (intent.hasExtra("setResult")) {
                                                        openActivity(PremiumThanksActivity::class.java)
                                                        setResult(RESULT_OK)
                                                        finish()
                                                    } else {
                                                        openActivity(PremiumThanksActivity::class.java) {
                                                            putBoolean("restart", true)
                                                        }
                                                        //restartNavActivity()
                                                    }
                                                    isProcessing = false
                                                } else {
                                                    isProcessing = false
                                                }
                                            }
                                        } else {
                                            Toast.makeText(this@PremiumPusherActivity, "You already bought this.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Red400)
                            ) {
                                Column(
                                    horizontalAlignment = CenterHorizontally
                                ) {
                                    Text(text = "${product.skuDetails?.price}/${(product.subscriptionPeriod?.unit?.period ?: "LIFETIME").uppercase()}", fontSize = 16.sp, color = Color.White)
                                    product.freeTrialPeriod?.let {
                                        Text(text = "${it.numberOfUnits} ${it.unit?.period?.uppercase()} TRIAL", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}


