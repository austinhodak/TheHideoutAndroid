package com.austinhodak.thehideout.gear

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.utils.asCurrency
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun GearListScreen(
    category: String? = "Armor",
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {
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
                MainToolbar(
                    title = category ?: "Armor",
                    navViewModel = navViewModel,
                    elevation = when (type) {
                        ItemTypes.HELMET,
                        ItemTypes.RIG -> 0.dp
                        else -> 4.dp
                    }
                )
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
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(items = items) { item ->
                        val visibleState = remember { MutableTransitionState(false) }
                        visibleState.targetState = true
                        AnimatedVisibility(
                            visibleState,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            when {
                                type == ItemTypes.GLASSES && page == 0 -> HeadsetCard(item = item)
                                type == ItemTypes.RIG && page == 0 -> BackpackCard(item = item)
                                type == ItemTypes.HELMET && page == 1 -> HeadsetCard(item = item)
                                else -> GearCard(item = item)
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
                }
                items(items = items) { item ->
                    val visibleState = remember { MutableTransitionState(false) }
                    visibleState.targetState = true
                    AnimatedVisibility(
                        visibleState,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        when (type) {
                            ItemTypes.BACKPACK -> BackpackCard(item = item)
                            ItemTypes.FACECOVER,
                            ItemTypes.HEADSET -> HeadsetCard(item = item)
                            else -> GearCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun GearCard(
    item: Item
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {

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
                    rememberImagePainter(item.pricing?.gridImageLink),
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
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
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

@ExperimentalMaterialApi
@Composable
private fun BackpackCard(
    item: Item
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {

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
                        text = "${item.ShortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
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

@ExperimentalMaterialApi
@Composable
private fun HeadsetCard(
    item: Item
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {

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
                    rememberImagePainter(item.pricing?.gridImageLink),
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