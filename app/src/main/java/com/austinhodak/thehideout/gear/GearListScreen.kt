package com.austinhodak.thehideout.gear

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.gear.viewmodels.GearViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun GearListScreen(
    category: String? = "Armor",
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo,
    gearViewModel: GearViewModel
) {

    val isSearchOpen by gearViewModel.isSearchOpen.observeAsState(false)
    val sort by gearViewModel.sortBy.observeAsState()
    val searchKey by gearViewModel.searchKey.observeAsState("")

    val context = LocalContext.current
    val type = when (category) {
        "Armor" -> ItemTypes.ARMOR
        "Backpacks" -> ItemTypes.BACKPACK
        "Rigs" -> ItemTypes.RIG
        "Eyewear" -> ItemTypes.GLASSES
        "Facecover" -> ItemTypes.FACECOVER
        "Headsets" -> ItemTypes.HEADSET
        "Headwear" -> ItemTypes.HELMET
        else -> ItemTypes.ARMOR
    }

    val data = tarkovRepo.getItemsByType(type).collectAsState(initial = emptyList())
    val titles: List<String>? = when (type) {
        ItemTypes.GLASSES,
        ItemTypes.RIG -> listOf("UNARMORED", "ARMORED")
        ItemTypes.HELMET -> listOf("ARMORED", "VANITY")
        else -> null
    }

    val pagerState = rememberPagerState(pageCount = titles?.size ?: 0)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            gearViewModel.setSearchOpen(false)
                            gearViewModel.clearSearch()
                        },
                        onValue = {
                            gearViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    MainToolbar(
                        title = category ?: "Armor",
                        navViewModel = navViewModel,
                        elevation = when (type) {
                            ItemTypes.HELMET,
                            ItemTypes.RIG -> 0.dp
                            else -> 4.dp
                        },
                        actions = {
                            IconButton(onClick = {
                                gearViewModel.setSearchOpen(true)
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                            }
                            IconButton(onClick = {
                                val items = listOf(
                                    "Name",
                                    "Price: Low to High",
                                    "Price: High to Low",
                                    "Armor Class",
                                    "Durability",
                                    "Efficiency"
                                )
                                MaterialDialog(context).show {
                                    title(text = "Sort By")
                                    listItemsSingleChoice(items = items, initialSelection = sort ?: 0) { _, index, _ ->
                                        gearViewModel.setSort(index)
                                    }
                                }
                            }) {
                                Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "Sort Ammo", tint = Color.White)
                            }
                        }
                    )
                }
                if (titles != null) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
                        },
                    ) {
                        titles.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                selectedContentColor = Red400,
                                unselectedContentColor = White
                            )
                        }
                    }
                }
            }
        }
    ) {
        if (titles != null) {

            HorizontalPager(state = pagerState) { page ->
                val items = when {
                    type == ItemTypes.RIG && page == 0 -> data.value.filter { it.cArmorClass() == 0 }.sortedBy { it.ShortName }
                    type == ItemTypes.RIG && page == 1 -> data.value.filter { it.cArmorClass() > 0 }.sortedBy { it.ShortName }
                    type == ItemTypes.HELMET -> {
                        when (page) {
                            0 -> data.value.filter { it.cArmorClass() > 0 }.sortedBy { it.ShortName }
                            1 -> data.value.filter { it.cArmorClass() == 0 && it.armorClass != null }.sortedBy { it.ShortName }
                            else -> data.value.filter { it.cArmorClass() > 0 }.sortedBy { it.ShortName }
                        }
                    }
                    type == ItemTypes.GLASSES && page == 0 -> data.value.filter { it.cArmorClass() == 0 }.sortedBy { it.ShortName }
                    type == ItemTypes.GLASSES && page == 1 -> data.value.filter { it.cArmorClass() > 0 }.sortedBy { it.ShortName }
                    else -> data.value.filter { it.pricing != null }.sortedBy { it.armorClass }
                }.filter {
                    it.ShortName?.contains(searchKey, ignoreCase = true) == true
                            || it.Name?.contains(searchKey, ignoreCase = true) == true
                            || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
                }.let { data ->
                    when (sort) {
                        0 -> data.sortedBy { it.ShortName }
                        1 -> data.sortedBy { it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() }
                        2 -> data.sortedByDescending { it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() }
                        3 -> data.sortedByDescending { it.armorClass }
                        4 -> data.sortedByDescending { it.Durability }
                        5 -> data.sortedByDescending { it.getInternalSlots()?.toDouble()?.div(it.getTotalSlots().toDouble()) }
                        else -> data.sortedBy { it.ShortName }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(items = items) { item ->
                        when {
                            type == ItemTypes.GLASSES && page == 0 -> HeadsetCard(item = item)
                            type == ItemTypes.RIG && page == 0 -> BackpackCard(item = item)
                            type == ItemTypes.HELMET && page == 1 -> HeadsetCard(item = item)
                            else -> GearCard(item = item) {
                                context.openActivity(GearDetailActivity::class.java) {
                                    putString("id", item.pricing?.id)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                val items = when (type) {
                    ItemTypes.HELMET -> data.value.filter { it.pricing != null && it.cArmorClass() > 0 }.sortedBy { it.ShortName }
                    ItemTypes.FACECOVER -> data.value.filter { it.pricing != null && it.cArmorClass() == 0 }.sortedBy { it.ShortName }
                    else -> data.value.filter { it.pricing != null }.sortedBy { it.armorClass }
                }.filter {
                    it.ShortName?.contains(searchKey, ignoreCase = true) == true
                            || it.Name?.contains(searchKey, ignoreCase = true) == true
                            || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
                }.let { data ->
                    when (sort) {
                        0 -> data.sortedBy { it.ShortName }
                        1 -> data.sortedBy { it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() }
                        2 -> data.sortedByDescending { it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() }
                        3 -> data.sortedByDescending { it.armorClass }
                        4 -> data.sortedByDescending { it.Durability }
                        5 -> data.sortedByDescending { it.getInternalSlots()?.toDouble()?.div(it.getTotalSlots().toDouble()) }
                        else -> data.sortedBy { it.ShortName }
                    }
                }

                items(items = items) { item ->
                    when (type) {
                        ItemTypes.BACKPACK -> BackpackCard(item = item)
                        ItemTypes.FACECOVER,
                        ItemTypes.HEADSET -> HeadsetCard(item = item)
                        else -> GearCard(item = item) {
                            context.openActivity(GearDetailActivity::class.java) {
                                putString("id", item.pricing?.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun GearCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.getCleanIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.ShortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    SmallBuyPrice(pricing = item.pricing)
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }*/
                }
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.armorClass}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "CLASS",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.Durability}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "DURABILITY",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
private fun BackpackCard(
    item: Item
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            if (item.itemType == ItemTypes.RIG) {
                context.openActivity(GearDetailActivity::class.java) {
                    putString("id", item.pricing?.id)
                }
            } else {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", item.pricing?.id)
                }
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.getCleanIcon()),
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
                        text = "${item.ShortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    SmallBuyPrice(pricing = item.pricing)
                }
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.getInternalSlots()}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "SLOTS",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.2f", item.getInternalSlots()?.toDouble()?.div(item.getTotalSlots().toDouble())),
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "EFFICIENCY",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
private fun HeadsetCard(
    item: Item
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            context.openActivity(GearDetailActivity::class.java) {
                putString("id", item.pricing?.id)
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.getCleanIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.ShortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    SmallBuyPrice(pricing = item.pricing)
                }
            }
        }
    }
}