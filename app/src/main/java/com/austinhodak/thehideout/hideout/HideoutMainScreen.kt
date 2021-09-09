package com.austinhodak.thehideout.hideout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.detail.CraftItem
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.hideoutList
import com.austinhodak.thehideout.quests.Chip
import com.austinhodak.thehideout.utils.asCurrency
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.glide.rememberGlidePainter
import timber.log.Timber

@ExperimentalMaterialApi
@Composable
fun HideoutMainScreen(
    navViewModel: NavViewModel,
    hideoutViewModel: HideoutMainViewModel,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    HideoutTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            bottomBar = {
                HideoutBottomBar(navController = navController)
            },
            topBar = {
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
                        if (navBackStackEntry?.destination?.route == HideoutNavigationScreens.Crafts.route) {
                            IconButton(onClick = {

                            }) {
                                Icon(painterResource(id = R.drawable.ic_baseline_filter_alt_24), contentDescription = "Filter", tint = Color.White)
                            }
                        }
                    }
                )
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

@Composable
private fun HideoutModulesPage(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    padding: PaddingValues
) {
    val modules = hideoutList.hideout?.modules

    val userData by hideoutViewModel.userData.observeAsState()
    val view by hideoutViewModel.view.observeAsState()

    Timber.d(userData?.hideoutModules.toString())

    val data = when (view) {
        HideoutFilter.CURRENT -> modules?.filter {
            userData?.isHideoutModuleComplete(it?.id ?: return) == true
        }
        HideoutFilter.ALL -> modules
        else -> modules
    }?.sortedWith(compareBy({ it?.level }, { it?.module }))

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
    ) {
        items(items = data ?: emptyList()) { module ->
            HideoutModuleCard(module, hideoutViewModel = hideoutViewModel, tarkovRepo, userData)
        }
    }
}

@Composable
private fun HideoutModuleCard(
    module: Hideout.Module?,
    hideoutViewModel: HideoutMainViewModel,
    tarkovRepo: TarkovRepo,
    userData: User?
) {
    if (module == null) return
    val isComplete = userData?.isHideoutModuleComplete(module.id ?: return)

    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                                    hideoutViewModel,
                                    module,
                                    requirement,
                                    userData
                                )
                            }
                            "module" -> {
                                HideoutRequirementModule(
                                    hideoutViewModel = hideoutViewModel,
                                    module = module,
                                    requirement = requirement,
                                    userData
                                )
                            }
                            "trader" -> {
                                HideoutRequirementTrader(
                                    hideoutViewModel = hideoutViewModel,
                                    module = module,
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
                if (isComplete == true) {
                    TextButton(onClick = {
                        //Undo module
                    }) {
                        Text(
                            "UNDO",
                            color = Color(0xFF555555)
                        )
                    }
                } else {
                    TextButton(onClick = {
                        //Build module
                    }) {
                        Text(
                            "BUILD LEVEL ${module.level}",
                            color = Red400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HideoutRequirementItem(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    module: Hideout.Module,
    requirement: Hideout.Module.Require,
    userData: User?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val item by tarkovRepo.getItemByID(requirement.name.toString()).collectAsState(initial = null)
    val pricing = item?.pricing

    Row (
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
            rememberGlidePainter(
                request = pricing?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
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
                    text = "${requirement.quantity} x ${pricing?.avg24hPrice?.asCurrency()} = ${
                        (requirement.quantity?.times(pricing?.avg24hPrice ?: 1))?.asCurrency()
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
private fun HideoutRequirementModule(
    hideoutViewModel: HideoutMainViewModel,
    module: Hideout.Module,
    requirement: Hideout.Module.Require,
    userData: User?
) {
    val context = LocalContext.current

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
    hideoutViewModel: HideoutMainViewModel,
    module: Hideout.Module,
    requirement: Hideout.Module.Require,
    userData: User?
) {
    val context = LocalContext.current
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

@ExperimentalMaterialApi
@Composable
private fun HideoutCraftsPage(
    tarkovRepo: TarkovRepo,
    hideoutViewModel: HideoutMainViewModel,
    padding: PaddingValues
) {
    val crafts by tarkovRepo.getAllCrafts().collectAsState(initial = emptyList())

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
    ) {
        items(items = crafts.sortedBy { it.rewardItems?.first()?.item?.name }) { craft ->
            CraftItem(craft, null)
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