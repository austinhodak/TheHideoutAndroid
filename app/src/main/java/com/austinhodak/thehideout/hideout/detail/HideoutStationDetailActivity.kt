package com.austinhodak.thehideout.hideout.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
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
import com.austinhodak.thehideout.hideout.detail.components.CraftsPage
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
            "module" -> 1
            "crafts" -> 2
            "station" -> 0
            else -> 0
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
                val pagerState = rememberPagerState(pageCount = modules?.size ?: 0)
                val pagerStateCrafts = rememberPagerState(pageCount = modules?.size?.plus(1) ?: 0)
                val coroutineScope = rememberCoroutineScope()
                val searchKey by navViewModel.searchKey.observeAsState("")
                val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)

                val crafts by tarkovRepo.getAllCrafts().collectAsState(initial = emptyList())

                val items = listOf(
                    FleaItemDetail.NavItem("Station", R.drawable.ic_baseline_info_24),
                    FleaItemDetail.NavItem("Modules", R.drawable.ic_baseline_handyman_24),
                    FleaItemDetail.NavItem("Crafts", R.drawable.ic_baseline_build_circle_24, crafts.any { it.source?.contains(station?.getName().toString(), true) == true }),
                )

                var selectedNavItem by remember { mutableStateOf(startDestination) }

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
                                        if (selectedNavItem == 2) {
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
                            if (selectedNavItem != 0) {
                                if (selectedNavItem == 1) {
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
                                if (selectedNavItem == 2) {
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
                        BottomBar(selectedNavItem, items) { selectedNavItem = it }
                    }
                ) {
                    Box(modifier = Modifier.padding(it)) {
                        Crossfade(targetState = selectedNavItem) {
                            when (it) {
                                0 -> {

                                }
                                1 -> {
                                    HideoutDetailModuleScreen(
                                        navViewModel,
                                        pagerState,
                                        modules,
                                        station
                                    )
                                }
                                2 -> {
                                    CraftsPage(
                                        crafts,
                                        navViewModel,
                                        pagerStateCrafts,
                                        modules,
                                        station
                                    ) { noCrafts ->

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BottomBar(
        selected: Int,
        items: List<FleaItemDetail.NavItem>,
        onItemSelected: (Int) -> Unit
    ) {

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = { Icon(painter = painterResource(id = item.icon), contentDescription = null) },
                    label = { Text(item.title) },
                    selected = selected == index,
                    onClick = { onItemSelected(index) },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = if (item.enabled == true) Color(0x99FFFFFF) else Color(0x33FFFFFF),
                    enabled = item.enabled ?: true,
                )
            }
        }
    }
}