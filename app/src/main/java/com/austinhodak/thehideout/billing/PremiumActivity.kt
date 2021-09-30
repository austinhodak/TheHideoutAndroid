package com.austinhodak.thehideout.billing

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.android.billingclient.api.SkuDetails
import com.austinhodak.thehideout.billing.viewmodels.BillingViewModel
import com.austinhodak.thehideout.compose.theme.TheHideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import timber.log.Timber

@AndroidEntryPoint
class PremiumActivity : AppCompatActivity() {

    private val viewModel: BillingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheHideoutTheme {
                val items by viewModel.itemList.observeAsState(mutableListOf())
                val subs by viewModel.subList.observeAsState(mutableListOf())

                Timber.d(items.toString())
                Timber.d(subs.toString())

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
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        subs?.forEach {
                            item {
                                SubCard(details = it)
                            }
                        }
                        item {
                            Text(
                                text = "DONTAIONS",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items?.forEach {
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
        val json = JSONObject(details.originalJson)
        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Text(text = json.getString("name"))
            }
        }
    }

    @Composable
    private fun IAPCard(details: SkuDetails) {
        val json = JSONObject(details.originalJson)
        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Text(text = json.getString("name"))
            }
        }
    }
}
