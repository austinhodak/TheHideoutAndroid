package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestMainFragment : Fragment() {

    private val questViewModel: QuestMainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = rememberNavController()
                val scaffoldState = rememberScaffoldState()
                HideoutTheme {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        bottomBar = {
                            QuestBottomNav(navController = navController)
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {  }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "")
                            }
                        }
                    ) {
                        NavHost(navController = navController, startDestination = BottomNavigationScreens.Stats.route) {
                            composable(BottomNavigationScreens.Stats.route) {

                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QuestBottomNav(
        navController: NavController
    ) {
        val items = listOf(
            BottomNavigationScreens.Stats,
            BottomNavigationScreens.Quests,
            BottomNavigationScreens.Items,
            BottomNavigationScreens.Maps
        )

        BottomNavigation(
            backgroundColor = MaterialTheme.colors.primary
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = {
                        if (item.icon != null) {
                            Icon(item.icon, "", tint = Color.White)
                        } else {
                            Icon(painter = painterResource(id = item.iconDrawable!!), contentDescription = item.resourceId, tint = Color.White)
                        }
                    },
                    label = { Text(item.resourceId, color = Color.White) },
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
                    }
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
        object Stats : BottomNavigationScreens("Stats", "Stats", null, R.drawable.ic_baseline_dashboard_24)
        object Quests : BottomNavigationScreens("Quests", "Quests", null, R.drawable.ic_baseline_assignment_turned_in_24)
        object Items : BottomNavigationScreens("Items", "Items", null, R.drawable.ic_baseline_assignment_24)
        object Maps : BottomNavigationScreens("Maps", "Maps", null, R.drawable.ic_baseline_map_24)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuestMainFragment()
    }
}