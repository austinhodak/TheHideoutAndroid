package com.austinhodak.thehideout.hideout.detail

import HideoutDetailModuleScreen
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.hideout.detail.components.CraftsPage
import com.austinhodak.thehideout.hideoutList
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalFoundationApi
@AndroidEntryPoint
class HideoutStationDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val navViewModel: NavViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = when (intent.getStringExtra("start")) {
            "module" -> 0
            "crafts" -> 1
            else -> 0
        }

        val stationID = if (intent.hasExtra("station")) {
            (intent.getSerializableExtra("station") as Hideout.Station?)?.id
        } else if (intent.hasExtra("moduleId")) {
            hideoutList.hideout?.modules?.find {
                val s = intent.getStringExtra("moduleId") ?: ""
                it?.module?.contains(s, true) == true
                //it?.id?.equals(intent.getIntExtra("moduleId", 18)) == true
            }?.stationId
        } else {
            intent.getIntExtra("stationId", 0)
        }

        val modules = hideoutList.hideout?.modules?.filter { it?.stationId?.equals(stationID) == true }
        val station = hideoutList.hideout?.stations?.find { it?.id?.equals(stationID) == true }

        setContent {
            HideoutTheme {
                val scaffoldState = rememberScaffoldState()
                val navController = rememberNavController()
                val pagerState = rememberPagerState()
                val pagerStateCrafts = rememberPagerState()
                val coroutineScope = rememberCoroutineScope()
                val searchKey by navViewModel.searchKey.observeAsState("")
                val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)

                val crafts by tarkovRepo.getAllCrafts().collectAsState(initial = emptyList())

                val items = listOf(
                    FleaItemDetail.NavItem("Modules", R.drawable.ic_baseline_handyman_24),
                    FleaItemDetail.NavItem("Crafts", R.drawable.ic_baseline_build_circle_24, crafts.any { it.source?.contains(station?.getName().toString(), true) == true }),
                )

                var selectedNavItem by remember { mutableStateOf(startDestination) }

                val allItems by navViewModel.allItems.observeAsState(initial = emptyList())

                val uiController = rememberSystemUiController()
                uiController.setNavigationBarColor(Color(0xFF1F1F1F))
                uiController.setStatusBarColor(Color(0xFF1F1F1F))

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
                                        if (selectedNavItem == 1) {
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

                            if (selectedNavItem == 0) {
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
                                                Timber.d("Clicked $index")
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
                            if (selectedNavItem == 1) {
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
                    },
                    bottomBar = {
                        BottomBar(selectedNavItem, items) { selectedNavItem = it }
                    }
                ) {

                    Box(modifier = Modifier.padding(it)) {
                        Crossfade(targetState = selectedNavItem) {
                            when (it) {
                                0 -> {
                                    if (allItems.isNullOrEmpty()) {
                                        LoadingItem()
                                        return@Crossfade
                                    }
                                    HorizontalPager(
                                        count = modules?.size ?: 1,
                                        state = pagerState,
                                    ) {
                                        HideoutDetailModuleScreen(
                                            allItems,
                                            this,
                                            modules,
                                            station,
                                        )
                                    }

                                }
                                1 -> {
                                    HorizontalPager(count = modules?.size?.plus(1) ?: 1, state = pagerStateCrafts) {
                                        CraftsPage(
                                            crafts,
                                            navViewModel,
                                            this,
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