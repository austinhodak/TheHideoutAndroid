package com.austinhodak.thehideout.quests

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400

@Composable
fun QuestMainScreen(navViewModel: NavViewModel, tarkovRepo: TarkovRepo) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
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
                        var selected by remember { mutableStateOf(1) }
                        val scrollState = rememberScrollState()
                        Row(
                            Modifier.horizontalScroll(scrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            /*Text(
                                "Quests",
                                modifier = Modifier.padding(end = 16.dp)
                            )*/
                            Chip(text = "Available", selected = selected == 1) {
                                selected = 1
                            }
                            Chip(text = "Locked", selected = selected == 2) {
                                selected = 2
                            }
                            Chip(text = "Completed", selected = selected == 3) {
                                selected = 3
                            }
                            Chip(text = "All", selected = selected == 0) {
                                selected = 0
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
            NavHost(navController = navController, startDestination = BottomNavigationScreens.Overview.route) {
                composable(BottomNavigationScreens.Overview.route) {

                }
                composable(BottomNavigationScreens.Quests.route) {

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