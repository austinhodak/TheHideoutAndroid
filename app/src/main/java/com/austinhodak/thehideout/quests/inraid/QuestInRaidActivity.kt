package com.austinhodak.thehideout.quests.inraid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class QuestInRaidActivity : GodActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapString = intent.getStringExtra("map") ?: "Customs"

        setContent {
            HideoutTheme {

                val scaffoldState = rememberScaffoldState()
                val navController = rememberNavController()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        AmmoDetailToolbar(
                            title = "In Raid at $mapString",
                            onBackPressed = {
                                finish()
                            }
                        )
                    },
                    bottomBar = {
                        QuestBottomNav(navController = navController)
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {

                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "", tint = Color.Black)
                        }
                    },
                    isFloatingActionButtonDocked = true,
                    floatingActionButtonPosition = FabPosition.Center
                ) {
                    NavHost(navController = navController, startDestination = BottomNavigationScreens.Tasks.route) {
                        composable(BottomNavigationScreens.Tasks.route) {

                        }
                        composable(BottomNavigationScreens.Items.route) {

                        }
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
        object Tasks : BottomNavigationScreens("Tasks", "Tasks", null, R.drawable.ic_baseline_fact_check_24)
        object Items : BottomNavigationScreens("Items", "Items", null, R.drawable.icons8_box_96)
    }

    @Composable
    private fun QuestBottomNav(
        navController: NavController
    ) {
        val items = listOf(
            BottomNavigationScreens.Tasks,
            BottomNavigationScreens.Items
        )

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEachIndexed { _, item ->
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