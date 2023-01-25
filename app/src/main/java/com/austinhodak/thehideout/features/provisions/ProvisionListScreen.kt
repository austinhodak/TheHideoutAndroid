package com.austinhodak.thehideout.features.provisions

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.common.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.utils.asColor
import com.austinhodak.thehideout.utils.border
import com.austinhodak.thehideout.utils.fadeImagePainterPlaceholder
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

    val keys by tarkovRepo.getItemsByType(ItemTypes.FOOD).collectAsState(initial = emptyList())

    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val searchKey by navViewModel.searchKey.observeAsState("")

    Scaffold(
        topBar = {
            Column {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            navViewModel.setSearchOpen(false)
                            navViewModel.clearSearch()
                        },
                        onValue = {
                            navViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    MainToolbar(
                        title = "Provisions",
                        navViewModel = navViewModel,
                        actions = {
                            IconButton(onClick = {
                                navViewModel.setSearchOpen(true)
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                            }
                        }
                    )
                }
            }
        }
    ) {
        val items = keys.filter {
            it.ShortName?.contains(searchKey, ignoreCase = true) == true
                    || it.Name?.contains(searchKey, ignoreCase = true) == true
                    || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
        }.sortedBy { it.Name }

        AnimatedContent(targetState = items.isNullOrEmpty()) {
            if (it) {
                LoadingItem()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(items = items, key = { it.id }) { key ->
                        ProvisionCard(item = key, Modifier.animateItemPlacement())
                    }
                }
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
    item: Item,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
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
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    fadeImagePainterPlaceholder(item.pricing?.getCleanIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .border()
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
                    SmallBuyPrice(pricing = item.pricing)
                }
                Column(
                    Modifier.width(IntrinsicSize.Min),
                ) {
                    if (item.getHydration() != null) StatItem(value = item.getHydration(), icon = R.drawable.ic_baseline_local_drink_24, item.getHydration()?.asColor(false))
                    if (item.getEnergy() != null) StatItem(value = item.getEnergy(), icon = R.drawable.ic_baseline_local_dining_24, item.getEnergy()?.asColor(false))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: Any?,
    icon: Int,
    color: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.h6,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp),
            color = color ?: MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "",
                modifier = Modifier.size(12.dp)
            )
        }
    }
}