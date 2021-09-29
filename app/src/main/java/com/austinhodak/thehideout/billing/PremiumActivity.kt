package com.austinhodak.thehideout.billing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.austinhodak.thehideout.billing.viewmodels.BillingViewModel
import com.austinhodak.thehideout.compose.theme.DarkPrimary
import com.austinhodak.thehideout.compose.theme.TheHideoutTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumActivity : AppCompatActivity() {

    private val viewModel: BillingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheHideoutTheme {
                val items by viewModel.itemsList.observeAsState()

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
                    LazyColumn {
                        items?.filter { it.type == BillingClient.SkuType.SUBS }?.forEach {
                            item {
                                SubCard(details = it)
                            }
                        }
                        item {
                            Text(
                                text = "DONTAIONS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        items?.filter { it.type == BillingClient.SkuType.INAPP }?.forEach {
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
    private fun SubCard(details: SkuDetails) {
        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Text(text = details.title)
            }
        }
    }

    @Composable
    private fun IAPCard(details: SkuDetails) {

    }
}
