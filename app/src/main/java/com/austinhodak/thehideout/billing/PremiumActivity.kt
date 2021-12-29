package com.austinhodak.thehideout.billing

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.adapty.Adapty
import com.adapty.models.ProductModel
import com.android.billingclient.api.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.billing.viewmodels.BillingViewModel
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.TheHideoutTheme
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.purchase
import com.austinhodak.thehideout.utils.restartNavActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import javax.inject.Inject

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@AndroidEntryPoint
class PremiumActivity : AppCompatActivity() {

    private val viewModel: BillingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TheHideoutTheme {
                var itemslist by remember { mutableStateOf<List<ProductModel>?>(null) }
                var subsList by remember { mutableStateOf<List<ProductModel>?>(null) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Upgrade") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressed()
                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primarySurface
                        )
                    }
                ) {
                    Adapty.getPaywalls (true) { paywalls, products, error ->
                        if (error == null) {
                            itemslist = products?.filter { it.skuDetails?.type == BillingClient.SkuType.INAPP }
                            subsList = products?.filter { it.skuDetails?.type == BillingClient.SkuType.SUBS }
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        subsList?.forEach {
                            item {
                                SubCard(details = it)
                            }
                        }
                        item {
                            Text(
                                text = "DONATIONS",
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        itemslist?.forEach {
                            item {
                                IAPCard(details = it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SubCard(details: ProductModel) {
        val json = JSONObject(details.skuDetails?.originalJson)
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clickable {
                    openActivity(PremiumPusherActivity::class.java)
                }
        ) {
            Column(
            ) {
                Text(
                    text = json.optString("name", "Premium"),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                MarkdownText(
                    markdown = "\u2714  Save Unlimited Weapon Builds<br>✔  Quest Tracker Integrated in Maps",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.body1,
                    fontResource = R.font.bender
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Red400)
                        .height(40.dp)
                        .clickable {
                            details.purchase(this@PremiumActivity) { purchaserInfo, purchaseToken, googleValidationResult, product, error ->
                                if (error == null) {
                                    Toast.makeText(this@PremiumActivity, "Thank you!", Toast.LENGTH_SHORT).show()
                                    restartNavActivity()
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "${details.skuDetails?.price}/MONTH", style = MaterialTheme.typography.button, color = Color.Black)
                }
            }
        }
    }

    @Composable
    private fun IAPCard(details: ProductModel) {
        val json = JSONObject(details.skuDetails?.originalJson)
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
        ) {
            Column(
            ) {
                Text(
                    text = json.optString("name", "Error"),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                MarkdownText(
                    markdown = details.skuDetails?.description ?: "",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.body1,
                    fontResource = R.font.bender
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Red400)
                        .height(40.dp)
                        .clickable {
                            details.purchase(this@PremiumActivity) { purchaserInfo, purchaseToken, googleValidationResult, product, error ->
                                if (error == null) {
                                    Toast.makeText(this@PremiumActivity, "Thank you!", Toast.LENGTH_SHORT).show()
                                    restartNavActivity()
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = details.skuDetails?.price ?: "", style = MaterialTheme.typography.button, color = Color.Black)
                }
            }
        }
    }
}
