package com.austinhodak.thehideout.hideout

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.fromDtoR
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.crafts.CompactItem
import com.austinhodak.thehideout.crafts.CraftDetailActivity
import com.austinhodak.thehideout.currency.euroToRouble
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.hideout.detail.HideoutStationDetailActivity
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.quests.Chip
import com.austinhodak.thehideout.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import kotlin.math.roundToInt

@SuppressLint("CheckResult")
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
    val context = LocalContext.current
    val sort by hideoutViewModel.sortBy.observeAsState()
    val filterAvailable by hideoutViewModel.filterAvailable.observeAsState()
    val allItems by navViewModel.allItems.observeAsState(initial = emptyList())

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
                                    HideoutNavigationScreens.Stations.route -> {
                                        Text(
                                            stringResource(R.string.stations),
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }
                                    HideoutNavigationScreens.Crafts.route -> {
                                        Text(
                                            stringResource(R.string.crafts),
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }
                                    else -> {
                                        Chip(text = stringResource(id = R.string.current), selected = selected == HideoutFilter.CURRENT) {
                                            hideoutViewModel.setView(HideoutFilter.CURRENT)
                                        }
                                        Chip(text = stringResource(id = R.string.available), selected = selected == HideoutFilter.AVAILABLE) {
                                            hideoutViewModel.setView(HideoutFilter.AVAILABLE)
                                        }
                                        Chip(text = stringResource(id = R.string.locked), selected = selected == HideoutFilter.LOCKED) {
                                            hideoutViewModel.setView(HideoutFilter.LOCKED)
                                        }
                                        Chip(text = stringResource(id = R.string.all), selected = selected == HideoutFilter.ALL) {
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
                            if (navBackStackEntry?.destination?.route != HideoutNavigationScreens.Stations.route) {
                                IconButton(onClick = {
                                    hideoutViewModel.setSearchOpen(true)
                                }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                                }
                            }
                            if (navBackStackEntry?.destination?.route == HideoutNavigationScreens.Crafts.route) {
                                IconButton(onClick = {
                                    val items = listOf(
                                        context.getString(R.string.name),
                                        context.getString(R.string.time_to_craft),
                                        context.getString(R.string.cost_low_to_high),
                                        context.getString(R.string.cost_high_to_low),
                                        context.getString(R.string.profit_low_to_high),
                                        context.getString(R.string.profit_high_to_low),
                                        context.getString(R.string.profit_hour_low_high),
                                        context.getString(R.string.profit_hour_high_low),
                                    )
                                    MaterialDialog(context).show {
                                        title(res = R.string.sort_by)
                                        listItemsSingleChoice(items = items, initialSelection = sort ?: 0) { _, index, _ ->
                                            hideoutViewModel.setSort(index)
                                        }
                                        checkBoxPrompt(res = R.string.show_only_available_crafts, isCheckedDefault = filterAvailable == true) {
                                            hideoutViewModel.setFilterAvailable(it)
                                        }
                                    }
                                }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_baseline_sort_24),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(navController = navController, startDestination = HideoutNavigationScreens.Stations.route) {
                composable(HideoutNavigationScreens.Stations.route) {
                    HideoutStationsPage(tarkovRepo, hideoutViewModel, padding)
                }
                composable(HideoutNavigationScreens.Modules.route) {
                    HideoutModulesPage(tarkovRepo, hideoutViewModel, padding, navViewModel)
                }
                composable(HideoutNavigationScreens.Crafts.route) {
                    HideoutCraftsPage(tarkovRepo, hideoutViewModel, padding)
                }
            }
        }
    }
}

@Composable
private fun OverviewItem(
    color: Color = DividerDark,
    icon: Int = R.drawable.ic_baseline_assignment_turned_in_24,
    s1: String = "",
    s2: String = "",
    progress: Float? = 0.5f,
    clicked: () -> Unit
) {
    val p by remember { mutableStateOf(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = p ?: 0.5f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(72.dp)
            .fillMaxWidth(),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        onClick = {
            clicked()
        }
    ) {
        Row {
            Image(
                modifier = Modifier
                    .size(72.dp)
                    .background(color)
                    .padding(16.dp),
                painter = painterResource(id = icon),
                contentDescription = ""
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HideoutStationsPage(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    padding: PaddingValues
) {
    val stations = hideoutList.hideout?.stations?.sortedBy { it?.locales?.en }
    val modules = hideoutList.hideout?.modules
    val userData by fsUser.observeAsState()
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding() + 4.dp)
    ) {
        stations?.forEach {
            it?.let { station ->
                val moduleTotal = modules?.count { it?.stationId == station.id }
                val modulesComplete = modules?.filter { it?.stationId == station.id }?.count { userData?.progress?.isHideoutModuleCompleted(it?.id) == true }

                item {
                    OverviewItem(
                        icon = station.getIcon(),
                        s1 = station.locales?.en ?: "",
                        s2 = "${modulesComplete}/${moduleTotal}",
                        color = if (moduleTotal == modulesComplete) Red400 else BorderColor,
                        progress = (modulesComplete?.toDouble()?.div(moduleTotal?.toDouble() ?: 1.0))?.toFloat()
                    ) {
                        context.openActivity(HideoutStationDetailActivity::class.java) {
                            putSerializable("station", station)
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
private fun HideoutModulesPage(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    padding: PaddingValues,
    navViewModel: NavViewModel
) {
    val modules = hideoutList.hideout?.modules

    val searchKey by hideoutViewModel.searchKey.observeAsState("")

    val userData by fsUser.observeAsState()
    val view by hideoutViewModel.view.observeAsState()

    val allItems by navViewModel.allItems.observeAsState(initial = emptyList())

    val data = when (view) {
        HideoutFilter.CURRENT -> modules?.filter {
            userData?.progress?.isHideoutModuleCompleted(it?.id ?: return) == true
        }
        HideoutFilter.AVAILABLE -> modules?.filter {
            if (userData?.progress?.isHideoutModuleCompleted(it?.id!!) == true) return@filter false
            if (it?.getModuleRequirements(modules)?.isEmpty() == true) {
                true
            } else {
                userData?.progress?.getCompletedHideoutIDs()?.containsAll(it?.getModuleRequirements(modules)?.map { it.toString() }!!) == true
            }
        }
        HideoutFilter.LOCKED -> modules?.filter {
            userData?.progress?.getCompletedHideoutIDs()?.containsAll(it?.getModuleRequirements(modules)?.map { it.toString() }!!) == false
        }
        HideoutFilter.ALL -> modules
        else -> modules
    }?.sortedWith(compareBy({ it?.level }, { it?.module }))?.filter {
        it?.module?.contains(searchKey, true) == true
    }

    if (data.isNullOrEmpty()) {
        EmptyText(text = stringResource(R.string.no_modules))
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
    ) {
        items(items = data) { module ->
            HideoutModuleCard(module, allItems, userData, hideoutViewModel)
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
    items: List<Item>,
    userData: FSUser?,
    hideoutViewModel: HideoutMainViewModel
) {
    if (module == null) return
    val isComplete = userData?.progress?.isHideoutModuleCompleted(module.id ?: return) ?: false

    val view by hideoutViewModel.view.observeAsState()
    val modules = hideoutList.hideout?.modules

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    context.openActivity(HideoutStationDetailActivity::class.java) {
                        putString("moduleId", module.module.toString())
                    }
                },
                onLongClick = {
                    MaterialDialog(context).show {
                        title(res = R.string.add_to_needed_items)
                        message(res = R.string.needed_items_desc)
                        positiveButton(res = R.string.add) {
                            module.require
                                ?.filter { it?.type == "item" }
                                ?.forEach {
                                    val itemID = it?.name
                                    val quantity = it?.quantity ?: 0
                                    if (quantity > 500) return@forEach
                                    userRefTracker("items/$itemID/hideoutObjective/${it?.id?.addQuotes()}").setValue(quantity)
                                }
                        }
                        negativeButton(res = R.string.cancel)
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
            if (!isComplete) {
                if (module.require?.isNotEmpty() == true)
                    Divider(color = DividerDark)
                Column(
                    Modifier.padding(vertical = 8.dp)
                ) {
                    module.require?.sortedByDescending { it?.type }?.forEach { requirement ->
                        when (requirement?.type) {
                            "item" -> {
                                items.find { it.id == requirement.name }?.let {
                                    HideoutRequirementItem(
                                        it,
                                        requirement,
                                        userData
                                    ) {
                                        context.openFleaDetail(it.id)
                                    }
                                }
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
            Divider(color = DividerDark)
            Row(
                Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .align(End)
            ) {
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
                                stringResource(id = R.string.locked),
                                color = Color(0xFF555555)
                            )
                        }
                    }
                    HideoutFilter.ALL -> {
                        if (module.getModuleRequirements(modules).isEmpty() && !isComplete || userData?.progress?.getCompletedHideoutIDs()
                                ?.containsAll(module.getModuleRequirements(modules).map { it.toString() }) == true && !isComplete
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
                                    stringResource(R.string.undo),
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
    item: Item,
    requirement: Hideout.Module.Require,
    userData: FSUser?,
    clicked: () -> Unit
) {
    val context = LocalContext.current

    val pricing = item.pricing

    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .clickable {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", item.id)
                }
            }
            .combinedClickable(onClick = {
                clicked()
            }, onLongClick = {
                /*if (requirement.quantity ?: 0 > 500) return@combinedClickable
                MaterialDialog(context).show {
                    title(text = "Add to Needed Items?")
                    message(text = "This will add these items to the needed items list on the Flea Market screen.")
                    positiveButton(text = "ADD") {
                        userRefTracker("items/${item?.id}/hideoutObjective/${it?.id?.addQuotes()}").setValue(quantity)
                    }
                    negativeButton(text = "CANCEL")
                }*/
            }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Image(
                rememberImagePainter(
                    pricing?.getCleanIcon()
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .border((0.25).dp, color = BorderColor)
            )
            Text(
                text = "${requirement.quantity}",
                Modifier
                    .clip(RoundedCornerShape(topStart = 5.dp))
                    .background(BorderColor)
                    .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                    .align(Alignment.BottomEnd),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${pricing?.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Row {
                    SmallBuyPrice(pricing = pricing)
                    Text(
                        text = " (${((requirement.quantity ?: 1).times(pricing?.getCheapestBuyRequirements()?.price ?: 0)).asCurrency()})",
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalCoilApi
@Composable
fun HideoutRequirementModule(
    module: Hideout.Module,
    requirement: Hideout.Module.Require,
    userData: FSUser?
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .clickable {
                context.openActivity(HideoutStationDetailActivity::class.java) {
                    putString("moduleId", requirement.name.toString())
                }
            },
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
                    color = if (userData?.progress?.isHideoutObjectiveCompleted(requirement) == true) Green500 else BorderColor
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
fun HideoutRequirementTrader(
    requirement: Hideout.Module.Require,
    userData: FSUser?
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
                    color = if (userData?.progress?.isHideoutObjectiveCompleted(requirement) == true) Green500 else BorderColor
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
    val sort by hideoutViewModel.sortBy.observeAsState()
    val filterAvailable by hideoutViewModel.filterAvailable.observeAsState()
    val userData by fsUser.observeAsState()

    val favorites by Favorites.crafts.observeAsState(emptySet())

    val items = crafts.filter {
        it.rewardItem()?.item?.name?.contains(searchKey, true) == true
                || it.rewardItem()?.item?.shortName?.contains(searchKey, true) == true
    }.let { data ->
        when (sort) {
            0 -> data.sortedBy { it.rewardItems?.first()?.item?.name }
            1 -> data.sortedBy { it.duration }
            2 -> data.sortedBy { it.totalCost() }
            3 -> data.sortedByDescending { it.totalCost() }
            4 -> data.sortedBy { it.estimatedProfit() }
            5 -> data.sortedByDescending { it.estimatedProfit() }
            6 -> data.sortedBy { it.estimatedProfitPerHour() }
            7 -> data.sortedByDescending { it.estimatedProfitPerHour() }
            else -> data
        }
    }.filter {
        if (filterAvailable == true) {
            userData == null || userData!!.progress?.isHideoutModuleCompleted(it.getSourceID(hideoutList.hideout).toString()) == true
        } else {
            true
        }
    }

    AnimatedContent(targetState = items.isNullOrEmpty()) {
        if (it) {
            LoadingItem()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
            ) {
                items(items = items, key = { it.id.toString() }) { craft ->
                    CraftItem(craft, userData, favorites.contains(craft.id))
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun CraftItem(craft: Craft, userData: FSUser?, isFavorite: Boolean) {
    val rewardItem = craft.rewardItems?.firstOrNull()?.item
    val reward = craft.rewardItems?.firstOrNull()
    val requiredItems = craft.requiredItems
    val context = LocalContext.current

    val alpha = ContentAlpha.high

    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                context.openActivity(CraftDetailActivity::class.java) {
                    putSerializable("craft", craft)
                }
            },
        ) {
            Column {
                if (userData == null || userData.progress?.isHideoutModuleCompleted(craft.getSourceID(hideoutList.hideout) ?: 0) == true) {

                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = Red400)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.BuildCircle, contentDescription = "", tint = Color.Black, modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                        )
                        Text(
                            text = "${craft.source?.uppercase()} NOT BUILT",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 34.dp)
                        )
                    }
                }

                Row(
                    Modifier
                        .padding(16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top
                ) {
                    Box {
                        Image(
                            fadeImagePainter(
                                rewardItem?.getCleanIcon()
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .width(38.dp)
                                .height(38.dp)
                                .border((0.25).dp, color = BorderColor)
                        )
                        Text(
                            text = "${reward?.count}",
                            Modifier
                                .clip(RoundedCornerShape(topStart = 5.dp))
                                .background(BorderColor)
                                .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                                .align(Alignment.BottomEnd),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                    }
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${rewardItem?.name} (x${reward?.count})",
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
                        Timber.d(rewardItem.toString())
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = "${rewardItem?.avg24hPrice?.asCurrency()} @ Flea Market",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }
                    if (isFavorite) {
                        Icon(Icons.Filled.Favorite, null, tint = Pink500, modifier = Modifier.size(16.dp))
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
                requiredItems?.sortedWith(
                    compareBy({ it?.isTool() }, {it?.item?.shortName})
                )?.forEach { item ->
                    item?.item?.let { pricing ->
                        CompactItem(
                            item = pricing, extras = CraftDetailActivity.ItemSubtitle(
                                iconText = item.count?.toString(),
                                showPriceInSubtitle = true,
                                subtitle = " (${(item.count?.times(pricing.getCheapestBuyRequirements().price ?: 0))?.asCurrency()})"
                            ), if (item.isTool()) ToolBlue else BorderColor
                        )
                    }
                    //BarterCraftCostItem(taskItem)
                }
                Divider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
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

    val cheapestBuy = item?.getCheapestBuyRequirements()?.copy()
    if (cheapestBuy?.currency == "USD") {
        cheapestBuy.price = cheapestBuy.price?.fromDtoR()?.roundToInt()
    } else if (cheapestBuy?.currency == "EUR") {
        cheapestBuy.price = euroToRouble(cheapestBuy.price?.toLong()).toInt()
    }

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
        Box {
            Image(
                rememberImagePainter(
                    item?.getCleanIcon()
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .border((0.25).dp, color = BorderColor)
            )
            Text(
                text = "${taskItem?.count}",
                Modifier
                    .clip(RoundedCornerShape(topStart = 5.dp))
                    .background(BorderColor)
                    .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                    .align(Alignment.BottomEnd),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${item?.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Row {
                    SmallBuyPrice(pricing = taskItem?.item)
                    Text(
                        text = " (${(taskItem?.count?.times(cheapestBuy?.price ?: 0))?.asCurrency()})",
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }
    }
}

@Composable
private fun HideoutBottomBar(
    navController: NavController
) {
    val items = listOf(
        HideoutNavigationScreens.Stations,
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
    object Stations : HideoutNavigationScreens("Stations", "Stations", null, R.drawable.ic_baseline_dashboard_24)
    object Modules : HideoutNavigationScreens("Modules", "Modules", null, R.drawable.ic_baseline_handyman_24)
    object Crafts : HideoutNavigationScreens("Crafts", "Crafts", null, R.drawable.ic_baseline_build_circle_24)
}

enum class HideoutFilter {
    CURRENT,
    AVAILABLE,
    LOCKED,
    ALL
}