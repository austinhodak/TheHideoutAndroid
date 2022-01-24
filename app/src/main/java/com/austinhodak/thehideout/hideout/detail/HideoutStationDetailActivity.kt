package com.austinhodak.thehideout.hideout.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.fromDtoR
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.currency.euroToRouble
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.hideoutList
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@AndroidEntryPoint
class HideoutStationDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val navViewModel: NavViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = when (intent.getStringExtra("start")) {
            "module" -> BottomNavigationScreens.Modules.route
            "crafts" -> BottomNavigationScreens.Crafts.route
            "station" -> BottomNavigationScreens.Station.route
            else -> BottomNavigationScreens.Station.route
        }

        val stationID = ((intent.getSerializableExtra("station") as Hideout.Station?) ?: Hideout.Station(
            false,
            "",
            id = 18
        )).id

        val modules = hideoutList.hideout?.modules?.filter { it?.stationId?.equals(stationID) == true }
        val station = hideoutList.hideout?.stations?.find { it?.id?.equals(stationID) == true }

        setContent {
            HideoutTheme {
                val scaffoldState = rememberScaffoldState()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val pagerState = rememberPagerState(pageCount = modules?.size ?: 0)
                val pagerStateCrafts = rememberPagerState(pageCount = modules?.size?.plus(1) ?: 0)
                val coroutineScope = rememberCoroutineScope()
                val searchKey by navViewModel.searchKey.observeAsState("")
                val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)

                val crafts by tarkovRepo.getAllCrafts().collectAsState(initial = emptyList())

                Scaffold(
                    scaffoldState = scaffoldState,
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
                                        Text(text = "${station?.getName()}")
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            onBackPressed()
                                        }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                        }
                                    },
                                    backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                                    elevation = 0.dp,
                                    actions = {
                                        if (navBackStackEntry?.destination?.route == BottomNavigationScreens.Crafts.route) {
                                            IconButton(onClick = { navViewModel.setSearchOpen(true) }) {
                                                Icon(
                                                    Icons.Filled.Search,
                                                    contentDescription = "Search",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                            if (navBackStackEntry?.destination?.route != BottomNavigationScreens.Station.route) {
                                if (navBackStackEntry?.destination?.route == BottomNavigationScreens.Modules.route) {
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
                                        modules?.forEachIndexed { index, module ->
                                            Tab(
                                                text = {
                                                    Text(
                                                        "LEVEL ${module?.level}",
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
                                if (navBackStackEntry?.destination?.route == BottomNavigationScreens.Crafts.route) {
                                    TabRow(
                                        selectedTabIndex = pagerStateCrafts.currentPage,
                                        indicator = { tabPositions ->
                                            TabRowDefaults.Indicator(
                                                Modifier.pagerTabIndicatorOffset(
                                                    pagerStateCrafts,
                                                    tabPositions
                                                ), color = Red400
                                            )
                                        },
                                        backgroundColor = MaterialTheme.colors.primary
                                    ) {
                                        modules?.forEachIndexed { index, module ->
                                            Tab(
                                                text = {
                                                    Text(
                                                        "LEVEL ${module?.level}",
                                                        fontFamily = Bender
                                                    )
                                                },
                                                selected = pagerStateCrafts.currentPage == index,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        pagerStateCrafts.animateScrollToPage(index)
                                                    }
                                                },
                                                selectedContentColor = Red400,
                                                unselectedContentColor = White
                                            )
                                        }
                                        Tab(
                                            text = {
                                                Text(
                                                    "ALL",
                                                    fontFamily = Bender
                                                )
                                            },
                                            selected = pagerStateCrafts.currentPage == modules?.size,
                                            onClick = {
                                                coroutineScope.launch {
                                                    modules?.size?.let { pagerStateCrafts.animateScrollToPage(it) }
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
                        BottomNavBar(navController = navController)
                    }
                ) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(BottomNavigationScreens.Station.route) {

                        }
                        composable(BottomNavigationScreens.Modules.route) {

                        }
                        composable(BottomNavigationScreens.Crafts.route) {
                            CraftsPage(
                                crafts,
                                navViewModel,
                                pagerStateCrafts,
                                modules,
                                station
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CraftsPage(crafts: List<Craft>, navViewModel: NavViewModel, pagerState: PagerState, modules: List<Hideout.Module?>?, station: Hideout.Station?) {
        val searchKey by navViewModel.searchKey.observeAsState("")
        val selectedModule = modules?.getOrNull(pagerState.currentPage)

        val list = crafts.filter {
            if (pagerState.currentPage == modules?.size) {

                true
            } else {
                it.source.equals(selectedModule.toString(), true)
            }
        }.filter {
            it.rewardItem()?.item?.name?.contains(searchKey, true) == true ||
            it.rewardItem()?.item?.shortName?.contains(searchKey, true) == true
        }.sortedBy { it.rewardItem()?.item?.name }

        if (list.isEmpty()) {
            EmptyText(text = "No crafts found.")
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp)
        ) {

            items(list) {
                CraftItem(craft = it)
            }
        }
    }

    @Composable
    fun CraftItem(craft: Craft) {
        val rewardItem = craft.rewardItems?.firstOrNull()?.item
        val reward = craft.rewardItems?.firstOrNull()
        val requiredItems = craft.requiredItems
        val context = LocalContext.current

        val alpha = ContentAlpha.high

        CompositionLocalProvider(LocalContentAlpha provides alpha) {
            Card(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                backgroundColor = Color(0xFE1F1F1F),
                onClick = {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", rewardItem?.id)
                    }
                },
            ) {
                Column {
                    /*if (userData == null || userData.isHideoutModuleComplete(craft.getSourceID(hideoutList.hideout) ?: 0)) {

                    } else {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(color = Red400)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning, contentDescription = "", tint = Color.Black, modifier = Modifier
                                    .height(20.dp)
                                    .width(20.dp)
                            )
                            Text(
                                text = "${craft.source?.uppercase()} NOT BUILT",
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 34.dp)
                            )
                        }
                    }*/

                    Row(
                        Modifier
                            .padding(16.dp)
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Image(
                                rememberImagePainter(
                                    rewardItem?.getCleanIcon()
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(38.dp)
                                    .border((0.25).dp, color = BorderColor)
                            )
                            Text(
                                text = "${reward?.count}",
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
                            Modifier
                                .padding(horizontal = 16.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${rewardItem?.name} (x${reward?.count})",
                                style = MaterialTheme.typography.h6,
                                fontSize = 16.sp
                            )
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Text(
                                    text = "${craft.source} • ${craft.getCraftingTime()}",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            }
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                val highestSell = rewardItem?.getHighestSell()

                                Text(
                                    text = "${highestSell?.getPriceAsCurrency()} @ ${highestSell?.getTitle()} (${highestSell?.price?.times(reward?.count ?: 1)?.asCurrency()})",
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
                            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                    requiredItems?.forEach { taskItem ->
                        BarterCraftCostItem(taskItem)
                    }
                    Divider(
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        color = Color(0x1F000000)
                    )
                    AvgPriceRow(title = "COST", price = craft.totalCost())
                    SavingsRow(title = "ESTIMATED PROFIT", price = craft.estimatedProfit())
                    SavingsRow(title = "ESTIMATED PROFIT PER HOUR", price = craft.estimatedProfitPerHour())
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }

    @ExperimentalCoilApi
    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @Composable
    private fun BarterCraftCostItem(taskItem: Craft.CraftItem?) {
        val item = taskItem?.item
        val context = LocalContext.current

        val cheapestBuy = item?.getCheapestBuyRequirements()?.copy()
        if (cheapestBuy?.currency == "USD") {
            cheapestBuy.price = cheapestBuy.price?.fromDtoR()?.roundToInt()
        } else if (cheapestBuy?.currency == "EUR") {
            cheapestBuy.price = euroToRouble(cheapestBuy.price?.toLong()).toInt()
        }
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
                            text = " (${(taskItem?.count?.times(cheapestBuy?.price ?: 0))?.asCurrency()})",
                            style = MaterialTheme.typography.caption,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
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
        object Station :
            BottomNavigationScreens("Station", "Station", null, R.drawable.ic_baseline_info_24)

        object Modules :
            BottomNavigationScreens("Modules", "Modules", null, R.drawable.ic_baseline_handyman_24)

        object Crafts :
            BottomNavigationScreens("Crafts", "Crafts", null, R.drawable.ic_baseline_build_circle_24)
    }

    @Composable
    fun BottomNavBar(
        navController: NavController
    ) {
        val items = listOf(
            BottomNavigationScreens.Station,
            BottomNavigationScreens.Modules,
            BottomNavigationScreens.Crafts
        )

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F),
            elevation = 6.dp
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEachIndexed { _, item ->
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
                )
            }
        }
    }
}