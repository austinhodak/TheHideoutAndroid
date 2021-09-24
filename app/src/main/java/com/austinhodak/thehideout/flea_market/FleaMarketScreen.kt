package com.austinhodak.thehideout.flea_market

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.components.ShoppingCartScreen
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.questPrefs
import com.austinhodak.thehideout.utils.isDebug
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.database.ServerValue
import com.skydoves.only.onlyOnce
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalAnimationApi
@ExperimentalCoilApi
@SuppressLint("CheckResult")
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun FleaMarketScreen(
    navViewModel: NavViewModel,
    fleaViewModel: FleaViewModel,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val data by tarkovRepo.getAllItems().collectAsState(initial = null)
    val isSearchOpen by fleaViewModel.isSearchOpen.observeAsState(false)
    val sort by fleaViewModel.sortBy.observeAsState()
    val userData by fleaViewModel.userData.observeAsState()

    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            fleaViewModel.setSearchOpen(false)
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
                                fleaViewModel.setSearchOpen(true)
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
        },
        floatingActionButton = {

        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = FleaMarketScreens.Items.route) {
            composable(FleaMarketScreens.Items.route) {
                FleaMarketListScreen(data, fleaViewModel, paddingValues)
            }
            composable(FleaMarketScreens.Favorites.route) {
                FleaMarketFavoritesList(data, fleaViewModel, paddingValues)
            }
            composable(FleaMarketScreens.Needed.route) {
                FleaMarketNeededScreen(data, userData, fleaViewModel)
            }
            composable(FleaMarketScreens.ShoppingCart.route) {
                ShoppingCartScreen(data, userData, fleaViewModel, paddingValues)
            }
        }
    }
}


@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun FleaMarketNeededScreen(
    itemList: List<Item>?,
    userData: User?,
    fleaViewModel: FleaViewModel
) {
    val context = LocalContext.current
    onlyOnce("fleaNeededItemsHelper") {
        onDo {
            MaterialDialog(context).show {
                title(text = "Needed Items How To")
                message(text = "Add items to this list by long pressing on Hideout Modules, Quests, or Quest Requirements.\n\nSingle Tap: Increments Item Count\nDouble Tap: Decrements Item Count\nLong Click: Opens item page.\n\nThis feature will be getting improved soon.")
                positiveButton(text = "GOT IT")
            }
        }
    }

    val sortBy = fleaViewModel.sortBy.observeAsState()
    val searchKey by fleaViewModel.searchKey.observeAsState("")

    val neededItems = itemList?.filter { userData?.items?.containsKey(it.id) == true }.let { data ->
        when (sortBy.value) {
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
    }

    if (neededItems.isNullOrEmpty()) {
        EmptyText(text = "No Items Added.")
        return
    }

    LazyVerticalGrid(cells = GridCells.Adaptive(52.dp)) {
        items(items = neededItems) {
            val needed = userData?.items?.get(it.id)
            val color = if (needed?.has == needed?.getTotalNeeded()) {
                Green500
            } else {
                BorderColor
            }
            val context = LocalContext.current
            Box(
                Modifier.combinedClickable(
                    onClick = {
                        if (needed?.has != needed?.getTotalNeeded()) {
                            userRefTracker("items/${it.id}/has").setValue(ServerValue.increment(1))
                        } else {
                            context.openActivity(FleaItemDetail::class.java) {
                                putString("id", it.id)
                            }
                        }
                    },
                    onDoubleClick = {
                        if (needed?.has != 0) {
                            userRefTracker("items/${it.id}/has").setValue(ServerValue.increment(-1))
                        }
                    },
                    onLongClick = {
                        context.openActivity(FleaItemDetail::class.java) {
                            putString("id", it.id)
                        }
                    }
                )
            ) {
                Image(
                    rememberImagePainter(it.pricing?.iconLink ?: ""),
                    contentDescription = "",
                    Modifier
                        .layout { measurable, constraints ->
                            val tileSize = constraints.maxWidth

                            val placeable = measurable.measure(
                                constraints.copy(
                                    minWidth = tileSize,
                                    maxWidth = tileSize,
                                    minHeight = tileSize,
                                    maxHeight = tileSize,
                                )
                            )
                            layout(placeable.width, placeable.width) {
                                placeable.place(x = 0, y = 0, zIndex = 0f)
                            }
                        }
                        .border(0.1.dp, color),
                )
                Text(
                    text = "${needed?.has ?: 0}/${needed?.getTotalNeeded()}",
                    Modifier
                        .clip(RoundedCornerShape(topStart = 5.dp))
                        .background(color)
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .align(Alignment.BottomEnd),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp
                )
            }
        }
    }
}


@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun FleaMarketFavoritesList(
    data: List<Item>?,
    fleaViewModel: FleaViewModel,
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

    if (list?.isEmpty() == true) {
        EmptyText(text = "No Favorites.")
        return
    }

    when {
        data == null -> {
            LoadingItem()
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

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun FleaMarketListScreen(
    data: List<Item>?,
    fleaViewModel: FleaViewModel,
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
fun FleaBottomNav(
    navController: NavController
) {
    val items = if (isDebug()) {
        listOf(
            FleaMarketScreens.Items,
            FleaMarketScreens.Needed,
            FleaMarketScreens.Favorites,
            FleaMarketScreens.ShoppingCart
        )
    } else {
        listOf(
            FleaMarketScreens.Items,
            FleaMarketScreens.Needed,
            FleaMarketScreens.Favorites,
            FleaMarketScreens.ShoppingCart
        )
    }

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
                        Icon(
                            painter = painterResource(id = item.iconDrawable!!),
                            contentDescription = item.resourceId,
                            modifier = Modifier.size(24.dp)
                        )
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
    object Items : FleaMarketScreens("Items", "Items", null, R.drawable.ic_baseline_storefront_24)
    object Needed : FleaMarketScreens("Needed", "Needed", null, R.drawable.icons8_wish_list_96)
    object Favorites : FleaMarketScreens("Favorites", "Favorites", null, R.drawable.ic_baseline_favorite_24)
    object ShoppingCart : FleaMarketScreens("Cart", "Cart", null, R.drawable.ic_baseline_shopping_cart_24)
    //object Alerts : FleaMarketScreens("Alerts", "Price Alerts", null, R.drawable.ic_baseline_notifications_active_24)
}