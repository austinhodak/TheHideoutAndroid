package com.austinhodak.thehideout.flea_market

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.FleaItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import com.austinhodak.thehideout.questPrefs
import com.austinhodak.thehideout.utils.openActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalMaterialApi
@Composable
fun FleaMarketScreen(
    navViewModel: NavViewModel,
    fleaViewModel: FleaVM,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val data by tarkovRepo.getAllItems().collectAsState(initial = null)
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val sort by fleaViewModel.sortBy.observeAsState()

    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            navViewModel.setSearchOpen(false)
                            fleaViewModel.clearSearch()
                        },
                        onValue = {
                            fleaViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    MainToolbar(
                        title = "Flea Market",
                        navViewModel = navViewModel,
                        actions = {
                            IconButton(onClick = {
                                navViewModel.setSearchOpen(true)
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                            }
                            IconButton(onClick = {
                                val items = listOf(
                                    "Name",
                                    "Price: Low to High",
                                    "Price: High to Low",
                                    "Price Per Slot",
                                    "Change Last 48H: Low to High",
                                    "Change Last 48H: High to Low",
                                    "Insta Profit: Low to High",
                                    "Insta Profit: High to Low"
                                )
                                MaterialDialog(context).show {
                                    title(text = "Sort By")
                                    listItemsSingleChoice(items = items, initialSelection = sort ?: 2) { _, index, _ ->
                                        fleaViewModel.setSort(index)
                                    }
                                }
                            }) {
                                Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "Sort Ammo", tint = Color.White)
                            }
                        }
                    )
                }

            }
        },
        bottomBar = {
            FleaBottomNav(navController = navController)
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = FleaMarketScreens.Items.route) {
            composable(FleaMarketScreens.Items.route) {
                FleaMarketListScreen(data, fleaViewModel, paddingValues)
            }
            composable(FleaMarketScreens.Favorites.route) {
                FleaMarketFavoritesList(data, fleaViewModel, paddingValues)
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalMaterialApi
@Composable
private fun FleaMarketFavoritesList(
    data: List<Item>?,
    fleaViewModel: FleaVM,
    paddingValues: PaddingValues
) {
    val sortBy = fleaViewModel.sortBy.observeAsState()
    val searchKey by fleaViewModel.searchKey.observeAsState("")

    val list = when (sortBy.value) {
        0 -> data?.sortedBy { it.Name }
        1 -> data?.sortedBy { it.getPrice() }
        2 -> data?.sortedByDescending { it.getPrice() }
        3 -> data?.sortedByDescending { it.getPricePerSlot() }
        4 -> data?.sortedBy { it.pricing?.changeLast48h }
        5 -> data?.sortedByDescending { it.pricing?.changeLast48h }
        6 -> data?.sortedBy { it.pricing?.getInstaProfit() }
        7 -> data?.sortedByDescending { it.pricing?.getInstaProfit() }
        else -> data?.sortedBy { it.getPrice() }
    }?.filter {
        it.ShortName?.contains(searchKey, ignoreCase = true) == true
                || it.Name?.contains(searchKey, ignoreCase = true) == true
                || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
    }?.filter {
        questPrefs.favoriteItems?.contains(it.id) ?: false
    }

    when {
        data == null -> {
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
        }
        data.isEmpty() -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
            ) {
                Text(text = "No Favorites")
            }
        }
        else -> {
            val context = LocalContext.current
            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(top = 4.dp, bottom = paddingValues.calculateBottomPadding())
            ) {
                items(items = list ?: emptyList()) { item ->
                    FleaItem(item = item) {
                        context.openActivity(FleaItemDetail::class.java) {
                            putString("id", item.id)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalMaterialApi
@Composable
private fun FleaMarketListScreen(
    data: List<Item>?,
    fleaViewModel: FleaVM,
    paddingValues: PaddingValues
) {
    val sortBy = fleaViewModel.sortBy.observeAsState()
    val searchKey by fleaViewModel.searchKey.observeAsState("")

    val list = when (sortBy.value) {
        0 -> data?.sortedBy { it.Name }
        1 -> data?.sortedBy { it.getPrice() }
        2 -> data?.sortedByDescending { it.getPrice() }
        3 -> data?.sortedByDescending { it.getPricePerSlot() }
        4 -> data?.sortedBy { it.pricing?.changeLast48h }
        5 -> data?.sortedByDescending { it.pricing?.changeLast48h }
        6 -> data?.sortedBy { it.pricing?.getInstaProfit() }
        7 -> data?.sortedByDescending { it.pricing?.getInstaProfit() }
        else -> data?.sortedBy { it.getPrice() }
    }?.filter {
        it.ShortName?.contains(searchKey, ignoreCase = true) == true
                || it.Name?.contains(searchKey, ignoreCase = true) == true
                || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
    }

    if (data.isNullOrEmpty()) {
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
            contentPadding = PaddingValues(top = 4.dp, bottom = paddingValues.calculateBottomPadding())
        ) {
            items(items = list ?: emptyList()) { item ->
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
                alwaysShowLabel = true, // This hides the title for the unselected items
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