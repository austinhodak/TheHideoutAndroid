package com.austinhodak.thehideout.flea_market

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.FleaItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import com.austinhodak.thehideout.utils.openActivity

@ExperimentalMaterialApi
@Composable
fun FleaMarketScreen(
    navViewModel: NavViewModel,
    fleaViewModel: FleaVM,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val data = tarkovRepo.getAllItems().collectAsState(initial = emptyList())

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                MainToolbar(
                    title = "Flea Market",
                    navViewModel = navViewModel
                )
            }
        },
        bottomBar = {
            FleaBottomNav(navController = navController)
        }
    ) {
        NavHost(navController = navController, startDestination = FleaMarketScreens.Items.route) {
            composable(FleaMarketScreens.Items.route) {
                FleaMarketListScreen(data, fleaViewModel)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun FleaMarketListScreen(
    data: State<List<Item>>,
    fleaViewModel: FleaVM
) {
    val sortBy = fleaViewModel.sortBy.observeAsState()

    val list = when (sortBy.value) {
        0 -> data.value.sortedBy { it.Name }
        1 -> data.value.sortedByDescending { it.getPrice() }
        2 -> data.value.sortedByDescending { it.getPricePerSlot() }
        3 -> data.value.sortedByDescending { it.pricing?.changeLast48h }
        4 -> data.value.sortedBy { it.pricing?.changeLast48h }
        else -> data.value.sortedBy { it.getPrice() }
    }

    if (data.value.isNullOrEmpty()) {
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
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = list) { item ->
                FleaItem(item = item) {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", item.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun FleaBottomNav(
    navController: NavController
) {
    val items = listOf(
        FleaMarketScreens.Items,
        FleaMarketScreens.Favorites,
        FleaMarketScreens.Alerts,
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
                selectedContentColor = Red400,
                unselectedContentColor = White
            )
        }
    }
}

sealed class FleaMarketScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Items : FleaMarketScreens("Items", "Items", null, R.drawable.ic_baseline_shopping_cart_24)
    object Favorites : FleaMarketScreens("Favorites", "Favorites", null, R.drawable.ic_baseline_favorite_24)
    object Alerts : FleaMarketScreens("Alerts", "Price Alerts", null, R.drawable.ic_baseline_notifications_active_24)
}