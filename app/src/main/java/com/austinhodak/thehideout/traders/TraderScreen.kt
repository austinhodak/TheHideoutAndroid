package com.austinhodak.thehideout.traders

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.austinhodak.tarkovapi.TraderResetTimersQuery
import com.austinhodak.tarkovapi.models.TraderInfo
import com.austinhodak.tarkovapi.models.TraderReset
import com.austinhodak.tarkovapi.models.toObj
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.barters.BarterDetailActivity
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.crafts.CraftDetailActivity
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.tradersList
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(
    ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
    ExperimentalCoilApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class
)
@Composable
fun TraderScreen(trader: String?, navViewModel: NavViewModel, tarkovRepo: TarkovRepo, apolloClient: ApolloClient) {
    val titles: List<String> = listOf("LL1", "LL2", "LL3", "LL4", "ALL")

    val searchKey by navViewModel.searchKey.observeAsState("")
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val pagerState = rememberPagerState(pageCount = titles.size)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val barters by tarkovRepo.getAllBarters().collectAsState(initial = emptyList())
    val items by navViewModel.allItems.observeAsState(null)

    var resetTimers: TraderReset? by remember { mutableStateOf(null) }

    var resetTime: String? by remember { mutableStateOf("") }

    val traderInfo = tradersList.getTrader(trader ?: "")

    LaunchedEffect("traders") {
        try {
            resetTimers = apolloClient.query(TraderResetTimersQuery()).data?.toObj()

            while (true) {
                resetTime = resetTimers?.getTrader(trader ?: "")?.getResetTimeSpan() ?: ""
                delay(1000.toLong())
            }
        } catch (e: ApolloNetworkException) {
            //Most likely no internet connection.
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

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
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = trader?.capitalize(Locale.current) ?: "Trader",
                                    color = MaterialTheme.colors.onPrimary,
                                    style = MaterialTheme.typography.h6,
                                    maxLines = 1,
                                    fontSize = 18.sp,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = resetTime ?: "",
                                    color = MaterialTheme.colors.onPrimary,
                                    style = MaterialTheme.typography.caption,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                navViewModel.isDrawerOpen.value = true
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { navViewModel.setSearchOpen(true) }) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }
                        },
                        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                        elevation = 0.dp
                    )
                }
                if (navBackStackEntry?.destination?.route != BottomNavigationScreens.Info.route) {
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
            composable(BottomNavigationScreens.Info.route) {
                LazyColumn(
                    modifier = Modifier,
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = paddingValues.calculateBottomPadding() + 4.dp,
                        start = 8.dp,
                        end = 8.dp
                    )
                ) {
                    item {
                        InfoCard1(
                            traderInfo
                        )
                    }
                    traderInfo?.loyalty?.filterNot { it.level == 1 }?.forEach {
                        item {
                            InfoCardLoyalty(it, traderInfo)
                        }
                    }
                }
            }
            composable(BottomNavigationScreens.Items.route) {
                val itemList = items?.filter { item ->
                    item.pricing?.buyFor?.any {
                        it.source == trader && it.requirements.any {
                            if (pagerState.currentPage == 4) {
                                true
                            } else {
                                it.type == "loyaltyLevel" && it.value == pagerState.currentPage + 1
                            }
                        }
                    } == true
                }?.filter {
                    it.ShortName?.contains(searchKey, ignoreCase = true) == true
                            || it.Name?.contains(searchKey, ignoreCase = true) == true
                            || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
                }?.sortedBy {
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
                        items(items = itemList ?: emptyList()) { item ->
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

@Composable
private fun InfoCard1(trader: TraderInfo?) {
    val traderEnum = Traders.values().find { it.id.equals(trader?.locale?.en, true) } ?: Traders.PRAPOR
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
    ) {
        Column {
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    rememberImagePainter(traderEnum.icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(64.dp)
                        .height(64.dp)
                        .border((0.25).dp, color = BorderColor)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${trader?.name}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 16.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = "${trader?.description}",
                            style = MaterialTheme.typography.caption,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCardLoyalty(loyalty: TraderInfo.Loyalty, traderInfo: TraderInfo) {
    Card(
        Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
    ) {
        Column(
            Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "LOYALTY LEVEL ${loyalty.level}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = Bender,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Column {
                LoyaltyLine(title = "REQUIRED LEVEL", entry = "${loyalty.requiredLevel}")
                LoyaltyLine(title = "REQUIRED REP", entry = "${loyalty.requiredReputation}")
                LoyaltyLine(title = "REQUIRED SALES", entry = "${loyalty.requiredSales?.asCurrency(traderInfo.salesCurrency)}")
            }
        }
    }
}

@Composable
private fun LoyaltyLine(
    modifier: Modifier = Modifier,
    title: String,
    entry: String
) {
    Row(
        modifier = modifier.padding(horizontal = 0.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = entry,
            style = MaterialTheme.typography.body1,
            fontSize = 14.sp
        )
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
            context.openActivity(BarterDetailActivity::class.java) {
                putSerializable("barter", barter)
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
                    rememberImagePainter(data = rewardItem?.getCleanIcon()),
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
    ExperimentalCoilApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class
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
                    item?.getCleanIcon()
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
                        text = " (${(taskItem?.count?.times(cheapestBuy?.price ?: item?.basePrice ?: 0))?.asCurrency()})",
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
    object Info : BottomNavigationScreens(
        "Info",
        "Info",
        null,
        R.drawable.ic_baseline_info_24
    )

    object Items : BottomNavigationScreens(
        "Items",
        "Items",
        null,
        R.drawable.ic_baseline_storefront_24
    )

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
        BottomNavigationScreens.Info,
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

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun TraderFleaItem(
    item: Item,
    trader: String?,
    onClick: (String) -> Unit,
) {
    val context = LocalContext.current

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
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    onClick(item.id)
                },
                onLongClick = {
                    context.showDialog(
                        Pair("Wiki Page") {
                            item.pricing?.wikiLink?.openWithCustomTab(context)
                        },
                        Pair("Add to Needed Items") {
                            item.pricing?.addToNeededItemsDialog(context)
                        },
                        Pair("Add Price Alert") {
                            item.pricing?.addPriceAlertDialog(context)
                        },
                        Pair("Add to Cart") {
                            item.pricing?.addToCartDialog(context)
                        },
                    )
                }
            ),
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column {
            Row(
                Modifier
                    .padding(end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Rectangle(
                    color = color, modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 16.dp)
                )
                Image(
                    rememberImagePainter(item.pricing?.getCleanIcon()),
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
                        text = traderPrice?.getPriceAsCurrency() ?: "-",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.getPricePerSlot(traderPrice?.price ?: 0).asCurrency(traderPrice?.currency ?: "R")}/slot",
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