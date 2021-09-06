package com.austinhodak.thehideout.quests

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.quests.models.Traders
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun QuestMainScreen(
    navViewModel: NavViewModel,
    questViewModel: QuestMainViewModel,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    HideoutTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            bottomBar = {
                QuestBottomNav(navController = navController)
            },
            floatingActionButton = {
                /*FloatingActionButton(onClick = { }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "")
                }*/
            },
            topBar = {
                /*MainToolbar(
                    title = "Quests",
                    navViewModel = navViewModel,
                    actions = {
                        *//*IconButton(onClick = {
                            navViewModel.setSearchOpen(true)
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                        }*//*
                    }
                )*/
                TopAppBar(
                    title = {
                        val selected by questViewModel.view.observeAsState()
                        val scrollState = rememberScrollState()
                        Row(
                            Modifier.horizontalScroll(scrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            /*Text(
                                "Quests",
                                modifier = Modifier.padding(end = 16.dp)
                            )*/
                            Chip(text = "Available", selected = selected == QuestFilter.AVAILABLE) {
                                questViewModel.setView(QuestFilter.AVAILABLE)
                            }
                            Chip(text = "Locked", selected = selected == QuestFilter.LOCKED) {
                                questViewModel.setView(QuestFilter.LOCKED)
                            }
                            Chip(text = "Completed", selected = selected == QuestFilter.COMPLETED) {
                                questViewModel.setView(QuestFilter.COMPLETED)
                            }
                            Chip(text = "All", selected = selected == QuestFilter.ALL) {
                                questViewModel.setView(QuestFilter.ALL)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navViewModel.isDrawerOpen.value = true
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = null)
                        }
                    },
                    backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                    elevation = 0.dp
                )
            }
        ) {
            /*Box(
                Modifier.fillMaxWidth()
            ) {
                FloatingActionButton(
                    onClick = { },
                    shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                    modifier = Modifier.size(40.dp).align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = "Localized description")
                }
            }*/

            NavHost(navController = navController, startDestination = BottomNavigationScreens.Overview.route) {
                composable(BottomNavigationScreens.Overview.route) {
                    QuestOverviewScreen(questViewModel = questViewModel, tarkovRepo = tarkovRepo)
                }
                composable(BottomNavigationScreens.Quests.route) {
                    val selectedTrader by questViewModel.selectedTrader.observeAsState()
                    val pagerState = rememberPagerState(pageCount = Traders.values().size)

                    Column (
                        Modifier.fillMaxWidth()
                    ) {
                        ScrollableTabRow(
                            modifier = Modifier.fillMaxWidth(),
                            selectedTabIndex = pagerState.currentPage,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
                            },
                        ) {
                            Traders.values().forEachIndexed { index, trader ->
                                LeadingIconTab(
                                    text = { Text(trader.id, fontFamily = Bender) },
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    icon = {
                                        Image(
                                            painter = painterResource(id = trader.icon),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .size(24.dp)
                                        )
                                    },
                                    selectedContentColor = Red400,
                                    unselectedContentColor = White
                                )
                            }
                        }
                    }
                }
                composable(BottomNavigationScreens.Items.route) {

                }
                composable(BottomNavigationScreens.Maps.route) {

                }
            }
        }
    }
}

@Composable
private fun QuestOverviewScreen(
    questViewModel: QuestMainViewModel,
    tarkovRepo: TarkovRepo
) {
    val questTotal by tarkovRepo.numOfQuests().collectAsState(initial = 0)
    val pmcElimsTotal by questViewModel.pmcElimsTotal.observeAsState()
    val scavElimsTotal by questViewModel.scavElimsTotal.observeAsState()
    val questItemsTotal by questViewModel.questItemsTotal.observeAsState()
    val questFIRItemsTotal by questViewModel.questFIRItemsTotal.observeAsState()
    val handoverItemsTotal by questViewModel.handoverItemsTotal.observeAsState()
    val placedTotal by questViewModel.placedTotal.observeAsState()
    val pickupTotal by questViewModel.pickupTotal.observeAsState()

    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            OverviewItem(
                color = Green500,
                s1 = "Quests Completed",
                s2 = "0/$questTotal",
                progress = 0f
            )
        }
        item {
            OverviewItem(
                color = Red500,
                s1 = "PMC Eliminations",
                s2 = "0/$pmcElimsTotal",
                progress = 0f,
                icon = R.drawable.icons8_sniper_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFFFF9800),
                s1 = "Scav Eliminations",
                s2 = "0/$scavElimsTotal",
                progress = 0f,
                icon = R.drawable.icons8_target_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Quest Items",
                s2 = "0/$questItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_search_black_24dp
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Found in Raid Items",
                s2 = "0/$questFIRItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_baseline_check_circle_outline_24
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Handover Items",
                s2 = "0/$handoverItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_baseline_swap_horizontal_circle_24
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF9C27B0),
                s1 = "Placed Objectives",
                s2 = "0/$placedTotal",
                progress = 0f,
                icon = R.drawable.icons8_low_importance_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF9C27B0),
                s1 = "Pickup Objectives",
                s2 = "0/$pickupTotal",
                progress = 0f,
                icon = R.drawable.icons8_upward_arrow_96
            )
        }
    }
}

@Composable
private fun OverviewItem(
    color: Color = Color.Gray,
    icon: Int = R.drawable.ic_baseline_assignment_turned_in_24,
    s1: String = "",
    s2: String = "",
    progress: Float = 0.5f
) {
    var progress by remember { mutableStateOf(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(72.dp)
            .fillMaxWidth(),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .size(72.dp)
                    .background(color)
                    .padding(24.dp),
                painter = painterResource(id = icon),
                contentDescription = "",
                tint = Color.White
            )
            Column(
                Modifier.weight(1f)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth(),
                    progress = animatedProgress,
                    color = color
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = s1,
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        modifier = Modifier,
                        text = s2,
                        style = MaterialTheme.typography.h5,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun Chip(
    selected: Boolean = false,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = when {
            selected -> Red400
            else -> Color(0xFF2F2F2F)
        },
        contentColor = when {
            selected -> Color.Black
            else -> Color.White
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuestBottomNav(
    navController: NavController
) {
    val items = listOf(
        BottomNavigationScreens.Overview,
        BottomNavigationScreens.Quests,
        BottomNavigationScreens.Items,
        BottomNavigationScreens.Maps
    )

    BottomNavigation(
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = {
                    if (item.icon != null) {
                        Icon(item.icon, "")
                    } else {
                        Icon(painter = painterResource(id = item.iconDrawable!!), contentDescription = item.resourceId)
                    }
                },
                label = { Text(item.resourceId) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                alwaysShowLabel = false, // This hides the title for the unselected items
                onClick = {
                    if (currentDestination?.route == item.route) return@BottomNavigationItem
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                selectedContentColor = MaterialTheme.colors.secondary,
                unselectedContentColor = Color(0x99FFFFFF),
            )
        }
    }
}

sealed class BottomNavigationScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Overview : BottomNavigationScreens("Overview", "Overview", null, R.drawable.ic_baseline_dashboard_24)
    object Quests : BottomNavigationScreens("Quests", "Quests", null, R.drawable.ic_baseline_assignment_turned_in_24)
    object Items : BottomNavigationScreens("Items", "Items", null, R.drawable.ic_baseline_assignment_24)
    object Maps : BottomNavigationScreens("Maps", "Maps", null, R.drawable.ic_baseline_map_24)
}

enum class QuestFilter {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    ALL
}