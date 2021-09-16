package com.austinhodak.thehideout.hideout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.hideoutList
import com.austinhodak.thehideout.quests.Chip
import com.austinhodak.thehideout.utils.addQuotes
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.userRefTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.util.*

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun HideoutMainScreen(
    navViewModel: NavViewModel,
    hideoutViewModel: HideoutMainViewModel,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isSearchOpen by hideoutViewModel.isSearchOpen.observeAsState(false)

    HideoutTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            bottomBar = {
                HideoutBottomBar(navController = navController)
            },
            topBar = {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            hideoutViewModel.setSearchOpen(false)
                            hideoutViewModel.clearSearch()
                        },
                        onValue = {
                            hideoutViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            val selected by hideoutViewModel.view.observeAsState()
                            val scrollState = rememberScrollState()
                            Row(
                                Modifier.horizontalScroll(scrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (navBackStackEntry?.destination?.route) {
                                    HideoutNavigationScreens.Crafts.route -> {
                                        Text(
                                            "Crafts",
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }
                                    else -> {
                                        Chip(text = "Current", selected = selected == HideoutFilter.CURRENT) {
                                            hideoutViewModel.setView(HideoutFilter.CURRENT)
                                        }
                                        Chip(text = "Available", selected = selected == HideoutFilter.AVAILABLE) {
                                            hideoutViewModel.setView(HideoutFilter.AVAILABLE)
                                        }
                                        Chip(text = "Locked", selected = selected == HideoutFilter.LOCKED) {
                                            hideoutViewModel.setView(HideoutFilter.LOCKED)
                                        }
                                        Chip(text = "All", selected = selected == HideoutFilter.ALL) {
                                            hideoutViewModel.setView(HideoutFilter.ALL)
                                        }
                                    }
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
                        elevation = 4.dp,
                        actions = {
                            IconButton(onClick = {
                                hideoutViewModel.setSearchOpen(true)
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                            }
                            if (navBackStackEntry?.destination?.route == HideoutNavigationScreens.Crafts.route) {
                                IconButton(onClick = {

                                }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_baseline_filter_alt_24),
                                        contentDescription = "Filter",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(navController = navController, startDestination = HideoutNavigationScreens.Modules.route) {
                composable(HideoutNavigationScreens.Modules.route) {
                    HideoutModulesPage(tarkovRepo, hideoutViewModel, padding)
                }
                composable(HideoutNavigationScreens.Crafts.route) {
                    HideoutCraftsPage(tarkovRepo, hideoutViewModel, padding)
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun HideoutModulesPage(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    padding: PaddingValues
) {
    val modules = hideoutList.hideout?.modules

    val searchKey by hideoutViewModel.searchKey.observeAsState("")

    val userData by hideoutViewModel.userData.observeAsState()
    val view by hideoutViewModel.view.observeAsState()

    Timber.d(userData?.hideoutModules.toString())

    val data = when (view) {
        HideoutFilter.CURRENT -> modules?.filter {
            userData?.isHideoutModuleComplete(it?.id ?: return) == true
        }
        HideoutFilter.AVAILABLE -> modules?.filter {
            if (userData?.isHideoutModuleComplete(it?.id!!) == true) return@filter false
            if (it?.getModuleRequirements(modules)?.isEmpty() == true) {
                true
            } else {
                userData?.completedHideoutIDs()?.containsAll(it?.getModuleRequirements(modules)!!) == true
            }
        }
        HideoutFilter.LOCKED -> modules?.filter {
            userData?.completedHideoutIDs()?.containsAll(it?.getModuleRequirements(modules)!!) == false
        }
        HideoutFilter.ALL -> modules
        else -> modules
    }?.sortedWith(compareBy({ it?.level }, { it?.module }))?.filter {
        it?.module?.contains(searchKey, true) == true
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
    ) {
        items(items = data ?: emptyList()) { module ->
            HideoutModuleCard(module, tarkovRepo, userData, hideoutViewModel)
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun HideoutModuleCard(
    module: Hideout.Module?,
    tarkovRepo: TarkovRepo,
    userData: User?,
    hideoutViewModel: HideoutMainViewModel
) {
    if (module == null) return
    val isComplete = userData?.isHideoutModuleComplete(module.id ?: return)

    val view by hideoutViewModel.view.observeAsState()
    val modules = hideoutList.hideout?.modules

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    module.require
                        ?.filter { it?.type == "item" }
                        ?.forEach {
                            val itemID = it?.name
                            val quantity = it?.quantity ?: 0
                            if (quantity > 500) return@forEach
                            userRefTracker("items/$itemID/hideoutObjective/${it?.id?.addQuotes()}").setValue(quantity)
                        }
                }
            ),
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = module.getIcon()),
                        contentDescription = "Module Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .fillMaxWidth()
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "LEVEL ${module.level}",
                            style = MaterialTheme.typography.overline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Column(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = module.module!!, style = MaterialTheme.typography.h6)
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = module.getStation(hideoutList)?.function ?: "",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
                //Text(text = "Level ${module.level}", style = MaterialTheme.typography.overline)
            }
            if (isComplete == false) {
                Divider(color = itemBlack)
                Column(
                    Modifier.padding(vertical = 8.dp)
                ) {
                    module.require?.sortedByDescending { it?.type }?.forEach { requirement ->
                        when (requirement?.type) {
                            "item" -> {
                                HideoutRequirementItem(
                                    tarkovRepo,
                                    requirement,
                                    userData
                                )
                            }
                            "module" -> {
                                HideoutRequirementModule(
                                    module = module,
                                    requirement = requirement,
                                    userData = userData
                                )
                            }
                            "trader" -> {
                                HideoutRequirementTrader(
                                    requirement = requirement,
                                    userData = userData
                                )
                            }
                        }
                    }
                }
            }
            Divider(color = itemBlack)
            Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                when (view) {
                    HideoutFilter.AVAILABLE -> {
                        TextButton(onClick = {
                            //Build module
                            hideoutViewModel.buildModule(module)
                        }) {
                            Text(
                                "BUILD LEVEL ${module.level}",
                                color = Red400
                            )
                        }
                    }
                    HideoutFilter.LOCKED -> {
                        TextButton(onClick = {
                            //Undo module
                        }) {
                            Text(
                                "LOCKED",
                                color = Color(0xFF555555)
                            )
                        }
                    }
                    HideoutFilter.ALL -> {
                        if (module.getModuleRequirements(modules).isEmpty() && isComplete == false || userData?.completedHideoutIDs()
                                ?.containsAll(module.getModuleRequirements(modules)) == true && isComplete == false
                        ) {
                            TextButton(onClick = {
                                //Build module
                                hideoutViewModel.buildModule(module)
                            }) {
                                Text(
                                    "BUILD LEVEL ${module.level}",
                                    color = Red400
                                )
                            }
                        }
                    }
                    else -> {
                        if (isComplete == true) {
                            TextButton(onClick = {
                                //Undo module
                                hideoutViewModel.undoModule(module)
                            }) {
                                Text(
                                    "UNDO",
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun HideoutRequirementItem(
    tarkovRepo: TarkovRepo,
    requirement: Hideout.Module.Require,
    userData: User?
) {
    val context = LocalContext.current

    val item by tarkovRepo.getItemByID(requirement.name.toString()).collectAsState(initial = null)
    val pricing = item?.pricing

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
        Image(
            rememberImagePainter(
                pricing?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
            ),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border(
                    (0.25).dp,
                    color = if (userData?.isHideoutObjectiveComplete(requirement) == true) Green500 else BorderColor
                )
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${pricing?.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Text(
                    text = "${pricing?.getTotalCostWithExplanation(requirement.quantity ?: 1)}",
                    style = MaterialTheme.typography.caption,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                )
                /*Text(
                    text = "${requirement.quantity} x ${pricing?.avg24hPrice?.asCurrency()} = ${
                        (requirement.quantity?.times(pricing?.avg24hPrice ?: 1))?.asCurrency()
                    }",
                    style = MaterialTheme.typography.caption,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                )*/
            }
        }
    }
}

@ExperimentalCoilApi
@Composable
private fun HideoutRequirementModule(
    module: Hideout.Module,
    requirement: Hideout.Module.Require,
    userData: User?
) {

    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            rememberImagePainter(module.getIcon(requirement.name.toString())),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border(
                    (0.25).dp,
                    color = if (userData?.isHideoutObjectiveComplete(requirement) == true) Green500 else BorderColor
                )
                .background(DarkerGrey)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${requirement.name} Level ${requirement.quantity}",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun HideoutRequirementTrader(
    requirement: Hideout.Module.Require,
    userData: User?
) {
    val trader = Traders.values().find { it.int == (requirement.name as Double).toInt() }

    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(id = trader?.icon ?: R.drawable.prapor_portrait),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border(
                    (0.25).dp,
                    color = if (userData?.isHideoutObjectiveComplete(requirement) == true) Green500 else BorderColor
                )
                .background(DarkerGrey)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${trader?.id} LL${requirement.quantity}",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun HideoutCraftsPage(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    padding: PaddingValues
) {
    val crafts by tarkovRepo.getAllCrafts().collectAsState(initial = emptyList())
    val searchKey by hideoutViewModel.searchKey.observeAsState("")

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
    ) {
        items(items = crafts.sortedBy { it.rewardItems?.first()?.item?.name }.filter {
            it.rewardItem()?.item?.name?.contains(searchKey, true) == true
                    || it.rewardItem()?.item?.shortName?.contains(searchKey, true) == true
        }) { craft ->
            CraftItem(craft, null)
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun CraftItem(craft: Craft, userData: User?) {
    val rewardItem = craft.rewardItems?.firstOrNull()?.item
    val requiredItems = craft.requiredItems
    val context = LocalContext.current

    val alpha = if (userData == null || userData.isHideoutModuleComplete(craft.getSourceID(hideoutList.hideout) ?: 0)) {
        ContentAlpha.high
    } else {
        ContentAlpha.high
    }

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
                if (userData == null || userData.isHideoutModuleComplete(craft.getSourceID(hideoutList.hideout) ?: 0)) {

                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = Red400)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "")
                        Text(
                            text = "${craft.source?.uppercase()} NOT BUILT",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }

                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Image(
                            rememberImagePainter(
                                rewardItem?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .width(38.dp)
                                .height(38.dp)
                                .border((0.25).dp, color = BorderColor)
                        )
                    }
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = rewardItem?.name ?: "",
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
                            Text(
                                text = "${rewardItem?.avg24hPrice?.asCurrency()} @ Flea Market",
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
        Image(
            rememberImagePainter(
                item?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
            ),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border((0.25).dp, color = BorderColor)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${item?.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Text(
                    text = "${taskItem?.count} x ${item?.avg24hPrice?.asCurrency()} = ${
                        (taskItem?.count?.times(item?.avg24hPrice!!))?.asCurrency()
                    }",
                    style = MaterialTheme.typography.caption,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
private fun HideoutBottomBar(
    navController: NavController
) {
    val items = listOf(
        HideoutNavigationScreens.Modules,
        HideoutNavigationScreens.Crafts
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

sealed class HideoutNavigationScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Modules : HideoutNavigationScreens("Modules", "Modules", null, R.drawable.ic_baseline_handyman_24)
    object Crafts : HideoutNavigationScreens("Crafts", "Crafts", null, R.drawable.ic_baseline_build_circle_24)
}

enum class HideoutFilter {
    CURRENT,
    AVAILABLE,
    LOCKED,
    ALL
}