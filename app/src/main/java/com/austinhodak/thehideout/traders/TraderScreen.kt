package com.austinhodak.thehideout.traders

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.components.FleaItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.quests.QuestDetailActivity
import com.austinhodak.thehideout.utils.getCaliberName
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(
    ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class,
    coil.annotation.ExperimentalCoilApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class
)
@Composable
fun TraderScreen(trader: String?, navViewModel: NavViewModel, tarkovRepo: TarkovRepo) {
    val titles: List<String> = listOf("LL1", "LL2", "LL3", "LL4", "ALL")

    val searchKey by navViewModel.searchKey.observeAsState("")
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val pagerState = rememberPagerState(pageCount = titles.size)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val barters by tarkovRepo.getAllBarters().collectAsState(initial = emptyList())
    val items by tarkovRepo.getAllItems().collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier,
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
                        title = trader?.capitalize(Locale.current) ?: "Trader",
                        navViewModel = navViewModel,
                        elevation = 0.dp
                    ) {
                        IconButton(onClick = { navViewModel.setSearchOpen(true) }) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                }
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(
                                pagerState,
                                tabPositions
                            ), color = Red400
                        )
                    },
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Text(
                                    title,
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
        bottomBar = {
            TraderBottomNav(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavigationScreens.Items.route
        ) {
            composable(BottomNavigationScreens.Items.route) {
                val itemList = items.filter { item ->
                    item.pricing?.buyFor?.any {
                        it.source == trader && it.requirements.any {
                            if (pagerState.currentPage == 4) {
                                true
                            } else {
                                it.type == "loyaltyLevel" && it.value == pagerState.currentPage + 1
                            }
                        }
                    } == true
                }.filter {
                    it.ShortName?.contains(searchKey, ignoreCase = true) == true
                            || it.Name?.contains(searchKey, ignoreCase = true) == true
                            || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
                }.sortedBy {
                    it.Name
                }

                if (items.isNullOrEmpty()) {
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
                    val context = LocalContext.current
                    LazyColumn(
                        modifier = Modifier,
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = paddingValues.calculateBottomPadding() + 4.dp
                        )
                    ) {
                        items(items = itemList) { item ->
                            TraderFleaItem(item = item, trader = trader) {
                                context.openActivity(FleaItemDetail::class.java) {
                                    putString("id", item.id)
                                }
                            }
                        }
                    }
                }
            }
            composable(BottomNavigationScreens.Barters.route) {
                val barterList = barters.filter {
                    val i = it.source?.split(" ")
                    if (pagerState.currentPage == 4) {
                        i?.get(0)?.lowercase()?.equals(trader) == true
                    } else {
                        i?.get(0)?.lowercase()?.equals(trader) == true && i[1].removePrefix("LL") == (pagerState.currentPage + 1).toString()
                    }
                }.filter {
                    it.rewardItems?.first()?.item?.name?.contains(searchKey, ignoreCase = true) == true ||
                    it.rewardItems?.first()?.item?.shortName?.contains(searchKey, ignoreCase = true) == true
                }.sortedBy {
                    it.rewardItems?.first()?.item?.name
                }

                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = paddingValues.calculateBottomPadding() + 4.dp
                    )
                ) {
                    items(items = barterList) { barter ->
                        BarterItem(barter)
                    }
                }
            }
        }
    }
}

@OptIn(
    ExperimentalCoilApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@ExperimentalMaterialApi
@Composable
private fun BarterItem(
    barter: Barter
) {
    val rewardItem = barter.rewardItems?.firstOrNull()?.item
    val requiredItems = barter.requiredItems

    val context = LocalContext.current

    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundColor = Color(0xFE1F1F1F),
        onClick = {
            context.openActivity(FleaItemDetail::class.java) {
                putString("id", rewardItem?.id)
            }
        }
    ) {
        Column {
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(data = rewardItem?.iconLink
                        ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(38.dp)
                        .height(38.dp)
                        .border((0.25).dp, color = BorderColor)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = rewardItem?.name ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 16.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = barter.source ?: "",
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        val highestSell = rewardItem?.getHighestSell()

                        Text(
                            text = "${highestSell?.getPriceAsCurrency()} @ ${highestSell?.getTitle()}",
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }
            }
            Divider(
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color(0x1F000000)
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "NEEDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = Bender,
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 4.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                )
            }
            requiredItems?.forEach { taskItem ->
                BarterCraftCostItem(taskItem)
            }
            Divider(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                color = Color(0x1F000000)
            )
            AvgPriceRow(title = "COST", price = barter.totalCost())
            SavingsRow(title = "ESTIMATED SAVINGS", price = barter.estimatedProfit())
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class,
    coil.annotation.ExperimentalCoilApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class
)
@Composable
private fun BarterCraftCostItem(taskItem: Craft.CraftItem?) {
    val item = taskItem?.item
    val context = LocalContext.current
    val cheapestBuy = item?.getCheapestBuyRequirements()

    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .clickable {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", item?.id)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Image(
                rememberImagePainter(
                    item?.iconLink
                        ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .border((0.25).dp, color = BorderColor)
            )
            Text(
                text = "${taskItem?.count}",
                Modifier
                    .clip(RoundedCornerShape(topStart = 5.dp))
                    .background(BorderColor)
                    .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                    .align(Alignment.BottomEnd),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${item?.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Row {
                    SmallBuyPrice(pricing = taskItem?.item)
                    Text(
                        text = " (${(taskItem?.count?.times(cheapestBuy?.price ?: 0))?.asCurrency()})",
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
                /*Text(
                    text = item?.getTotalCostWithExplanation(taskItem.count ?: 1) ?: "",
                    style = MaterialTheme.typography.caption,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                )*/
            }
        }
    }
}

sealed class BottomNavigationScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Items :
        BottomNavigationScreens("Items", "Items", null, R.drawable.ic_baseline_storefront_24)

    object Barters : BottomNavigationScreens(
        "Barters",
        "Barters",
        null,
        R.drawable.ic_baseline_compare_arrows_24
    )
}

@Composable
private fun TraderBottomNav(
    navController: NavController
) {
    val items = listOf(
        BottomNavigationScreens.Items,
        BottomNavigationScreens.Barters
    )

    BottomNavigation(
        backgroundColor = Color(0xFE1F1F1F),
        modifier = Modifier.navigationBarsPadding()
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEachIndexed { i, item ->
            if (i == 1) {
                //BottomNavigationItem(selected = false, onClick = {}, icon = {}, enabled = false)
            }
            BottomNavigationItem(
                icon = {
                    if (item.icon != null) {
                        Icon(item.icon, "")
                    } else {
                        Icon(
                            painter = painterResource(id = item.iconDrawable!!),
                            contentDescription = item.resourceId
                        )
                    }
                },
                label = { Text(item.resourceId) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                alwaysShowLabel = true, // This hides the title for the unselected items
                onClick = {
                    try {
                        if (currentDestination?.route == item.route) return@BottomNavigationItem
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                },
                selectedContentColor = MaterialTheme.colors.secondary,
                unselectedContentColor = Color(0x99FFFFFF),
                enabled = true
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun TraderFleaItem(
    item: Item,
    trader: String?,
    onClick: (String) -> Unit,
) {

    val color = when (item.BackgroundColor) {
        "blue" -> itemBlue
        "grey" -> itemGrey
        "red" -> itemRed
        "orange" -> itemOrange
        "default" -> itemDefault
        "violet" -> itemViolet
        "yellow" -> itemYellow
        "green" -> itemGreen
        "black" -> itemBlack
        else -> itemDefault
    }

    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        //border = BorderStroke(1.dp, color = color),
        onClick = {
            onClick(item.id)
        },
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column {
            Row(
                Modifier
                    .padding(end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Rectangle(color = color, modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp))
                Image(
                    rememberImagePainter(item.pricing?.iconLink ?: "https://tarkov-tools.com/images/flea-market-icon.jpg"),
                    contentDescription = null,
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .border((0.25).dp, color = BorderColor)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.pricing?.name ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = item.getUpdatedTime(),
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    val traderPrice = item.pricing?.buyFor?.find {
                        it.source == trader
                    }
                    Text(
                        text = traderPrice?.price?.asCurrency() ?: "-",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.getPricePerSlot(traderPrice?.price ?: 0).asCurrency()}/slot",
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.pricing?.changeLast48h}%",
                            style = MaterialTheme.typography.caption,
                            color = if (item.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                            fontSize = 10.sp
                        )
                    }*/
                    TraderSmall(item = item.pricing)
                }
            }
        }
    }
}