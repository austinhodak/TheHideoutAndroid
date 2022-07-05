@file:OptIn(ExperimentalAnimationApi::class)

package com.austinhodak.thehideout.ammunition

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@SuppressLint("CheckResult")
@ExperimentalPagerApi
@Composable
fun AmmunitionListScreen(
    caliber: String?,
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {
    val pages = AmmoCalibers()
    val context = LocalContext.current

    val pagerState = rememberPagerState(pageCount = pages.size)
    val coroutineScope = rememberCoroutineScope()

    val sort = remember { mutableStateOf(UserSettingsModel.ammoSort.value) }

    if (caliber != null) {
        LaunchedEffect(key1 = "initial") {
            coroutineScope.launch {
                pagerState.scrollToPage(pages.indexOf(caliber))
            }
        }
    }

    val data by tarkovRepo.getAllAmmo.collectAsState(initial = emptyList())

    Timber.d(data.size.toString())

    val searchKey by navViewModel.searchKey.observeAsState("")
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)

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
                        title = stringResource(id = R.string.ammunition),
                        navViewModel = navViewModel,
                        elevation = 0.dp
                    ) {
                        IconButton(onClick = { navViewModel.setSearchOpen(true) }) {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.sort_ammo), tint = Color.White)
                        }
                        IconButton(onClick = {
                            val items = listOf(context.getString(R.string.name), context.getString(R.string.price_low_to_high), context.getString(R.string.price_high_to_low), context.getString(R.string.damage), context.getString(R.string.penetration), context.getString(R.string.armor_effectiveness))
                            MaterialDialog(context).show {
                                title(text = context.getString(R.string.sort_by))
                                listItemsSingleChoice(items = items, initialSelection = sort.value) { _, index, _ ->
                                    sort.value = index
                                    coroutineScope.launch {
                                        UserSettingsModel.ammoSort.update(index)
                                    }
                                }
                            }
                        }) {
                            Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "Sort Ammo", tint = Color.White)
                        }
                    }
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
                        },
                        backgroundColor = MaterialTheme.colors.primary
                    ) {
                        pages.forEachIndexed { index, title ->
                            Tab(
                                text = {
                                    Text(
                                        getCaliberName(title),
                                        fontFamily = Bender
                                    )
                                },
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
        },
        floatingActionButton = {
            // Jump to Caliber dialog.
            if (!isSearchOpen)
                FloatingActionButton(onClick = {
                    showJumpDialog(
                        context,
                        pages
                    ) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    }
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.icons8_ammo_100),
                        contentDescription = "Jump to Caliber",
                        tint = Color.Black
                    )
                }
        },

        ) {
        when {
            isSearchOpen -> {
                AmmoSearchBody(searchKey, data)
            }
            else -> {
                AnimatedContent(targetState = data.isEmpty()) {
                    if (it) {
                        LoadingItem()
                    } else {
                        HorizontalPager(state = pagerState) { page ->
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                var items = data.filter { it.Caliber == pages[page] }
                                Timber.d(items.size.toString())
                                items = when (sort.value) {
                                    0 -> items.sortedBy { it.shortName }
                                    1 -> items.sortedBy {
                                        it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles()
                                    }
                                    2 -> items.sortedByDescending {
                                        it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles()
                                    }
                                    3 -> items.sortedByDescending { it.ballistics?.damage }
                                    4 -> items.sortedByDescending { it.ballistics?.penetrationPower }
                                    5 -> items.sortedByDescending { it.getArmorValues() }
                                    else -> items.sortedBy { it.shortName }
                                }
                                items(items = items, key = { it.id }) { ammo ->
                                    AmmoCard(
                                        ammo,
                                        Modifier
                                            .padding(vertical = 4.dp)
                                            .animateItemPlacement()
                                    ) {
                                        context.openActivity(AmmoDetailActivity::class.java) {
                                            putString("ammoID", ammo.id)
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
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun AmmoSearchBody(
    searchKey: String,
    data: List<Ammo>
) {
    val context = LocalContext.current

    val items = data.filter {
        if (searchKey.isBlank()) return@filter false
        it.shortName?.contains(searchKey, ignoreCase = true) == true
                || it.name?.contains(searchKey, ignoreCase = true) == true
    }.sortedBy { it.shortName }

    if (items.isNullOrEmpty()) {
        EmptyText(stringResource(R.string.search_ammunition))
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
    ) {
        items(items = items, key = { it.id }) { ammo ->
            AmmoCard(
                ammo,
                Modifier
                    .padding(vertical = 4.dp)
                    .animateItemPlacement()
            ) {
                context.openActivity(AmmoDetailActivity::class.java) {
                    putString("ammoID", ammo.id)
                }
            }
        }
    }
}

@SuppressLint("CheckResult")
fun showJumpDialog(
    context: Context,
    pages: List<String>,
    selected: (Int) -> Unit
) {
    MaterialDialog(context).show {
        title(text = "Jump To")
        listItems(items = pages.map { getCaliberName(it) }) { _, index, _ ->
            selected(index)
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AmmoCard(
    ammo: Ammo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
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
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .padding(end = 16.dp)
                        .width(3.dp)
                        .fillMaxHeight()
                ) {
                    ArmorBox(ammo.getColor(1), Modifier.weight(1f))
                    ArmorBox(ammo.getColor(2), Modifier.weight(1f))
                    ArmorBox(ammo.getColor(3), Modifier.weight(1f))
                    ArmorBox(ammo.getColor(4), Modifier.weight(1f))
                    ArmorBox(ammo.getColor(5), Modifier.weight(1f))
                    ArmorBox(ammo.getColor(6), Modifier.weight(1f))
                }
                Image(
                    fadeImagePainterPlaceholder(url = ammo.pricing?.getIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .border(0.25.dp, BorderColor)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${ammo.shortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${ammo.getPrice()}",
                            style = MaterialTheme.typography.caption
                        )
                    }*/
                    SmallBuyPrice(pricing = ammo.pricing)
                }
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${ammo.ballistics?.damage}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = stringResource(id = R.string.damage),
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
                        text = "${ammo.ballistics?.penetrationPower?.roundToInt()}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "PEN",
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

@Composable
fun ArmorBox(
    color: Color,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .background(color)
            .fillMaxWidth(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.1f))
                    .fillMaxWidth()
                    .height(1.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.1f))
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }

    }
}