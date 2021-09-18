package com.austinhodak.thehideout.provisions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@Composable
fun ProvisionListScreen(
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {

    val keys = tarkovRepo.getItemsByType(ItemTypes.FOOD).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            Column {
                MainToolbar(
                    title = "Provisions",
                    navViewModel = navViewModel
                )
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(items = keys.value.sortedBy { it.Name }) { key ->
                ProvisionCard(item = key)
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@Composable
fun ProvisionCard(
    item: Item
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            context.openActivity(FleaItemDetail::class.java) {
                putString("id", item.pricing?.id)
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.gridImageLink),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.Name}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}