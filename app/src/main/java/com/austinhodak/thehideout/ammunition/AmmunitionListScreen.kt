package com.austinhodak.thehideout.ammunition

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.utils.AmmoCalibers
import com.austinhodak.thehideout.utils.getCaliberName
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    val sort = remember { mutableStateOf(0) }

    if (caliber != null) {
        LaunchedEffect(key1 = "initial") {
            coroutineScope.launch {
                pagerState.scrollToPage(pages.indexOf(caliber))
            }
        }
    }

    val data by tarkovRepo.getAllAmmo.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            Column {
                MainToolbar(
                    title = "Ammunition",
                    navViewModel = navViewModel,
                    elevation = 0.dp
                ) {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Sort Ammo", tint = Color.White)
                    }
                    IconButton(onClick = {
                        val items = listOf("Name", "Price: Low to High", "Price: High to Low", "Damage", "Penetration", "Armor Effectiveness")
                        MaterialDialog(context).show {
                            title(text = "Sort By")
                            listItemsSingleChoice(items = items, initialSelection = sort.value) { _, index, _ ->
                                sort.value = index
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
        },
        floatingActionButton = {
            // Jump to Caliber dialog.
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
                Icon(modifier = Modifier.size(24.dp), painter = painterResource(id = R.drawable.icons8_ammo_100), contentDescription = "Jump to Caliber", tint = Color.Black)
            }
        },

    ) {
        if (data.isNullOrEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.secondary
                )
            }
        } else {
            HorizontalPager(state = pagerState) { page ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    var items = data.filter { it.Caliber == pages[page] }
                    items = when (sort.value) {
                        0 -> items.sortedBy { it.shortName }
                        1 -> items.sortedBy { it.pricing?.lastLowPrice }
                        2 -> items.sortedByDescending { it.pricing?.lastLowPrice }
                        3 -> items.sortedByDescending { it.ballistics?.damage }
                        4 -> items.sortedByDescending { it.ballistics?.penetrationPower }
                        5 -> items.sortedByDescending { it.getArmorValues() }
                        else -> items.sortedBy { it.shortName }
                    }
                    items(items = items) { ammo ->
                        AmmoCard(
                            ammo,
                            Modifier.padding(vertical = 4.dp)
                        )
                    }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AmmoCard(
    ammo: Ammo,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            context.openActivity(AmmoDetailActivity::class.java) {
                putString("ammoID", ammo.id)
            }
        }
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
                    rememberImagePainter(ammo.pricing?.gridImageLink),
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
                        text = "${ammo.shortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${ammo.getPrice()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
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
                            text = "DAMAGE",
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
            Box (
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.1f))
                    .fillMaxWidth()
                    .height(1.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box (
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.1f))
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }

    }
}