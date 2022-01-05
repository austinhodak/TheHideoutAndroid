package com.austinhodak.thehideout.quests

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Maps
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.traderIcon
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.mapsList
import com.austinhodak.thehideout.quests.inraid.QuestInRaidActivity
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
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
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val quests by tarkovRepo.getAllQuests().collectAsState(initial = emptyList())

    val isSearchOpen by questViewModel.isSearchOpen.observeAsState(false)
    val searchKey by questViewModel.searchKey.observeAsState("")
    val userData by questViewModel.userData.observeAsState()

    HideoutTheme {
        BottomSheetScaffold(
            sheetContent = {
                Column(
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MapCard(
                        mapName = "Customs",
                        mapSubtitle = "8-12 Players • 45 Minutes",
                        icon = R.drawable.icons8_structural_96
                    )
                    MapCard(
                        mapName = "Factory",
                        mapSubtitle = "4-6 Players • 20-25 Minutes",
                        icon = R.drawable.icons8_factory_breakdown_96
                    )
                    MapCard(
                        mapName = "Interchange",
                        mapSubtitle = "10-14 Players • 50 Minutes",
                        icon = R.drawable.icons8_shopping_mall_96
                    )
                    MapCard(
                        mapName = "Lighthouse",
                        mapSubtitle = "9-12 Players • 45 Minutes",
                        icon = R.drawable.icons8_lighthouse_96
                    )
                    MapCard(
                        mapName = "Reserve",
                        mapSubtitle = "9-12 Players • 50 Minutes",
                        icon = R.drawable.icons8_knight_96
                    )
                    MapCard(
                        mapName = "Shoreline",
                        mapSubtitle = "10-13 Players • 50 Minutes",
                        icon = R.drawable.icons8_bay_96
                    )
                    MapCard(
                        mapName = "Labs",
                        mapSubtitle = "6-10 Players • 40 Minutes",
                        icon = R.drawable.icons8_laboratory_96
                    )
                    MapCard(
                        mapName = "Woods",
                        mapSubtitle = "8-14 Players • 50 Minutes",
                        icon = R.drawable.icons8_forest_96
                    )

                }
            },
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = 0.dp,
            sheetBackgroundColor = Color(0xFF303030),
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
                bottomBar = {
                    QuestBottomNav(navController = navController)
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "", tint = Color.Black)
                    }
                },
                topBar = {
                    if (isSearchOpen) {
                        SearchToolbar(
                            onClosePressed = {
                                questViewModel.setSearchOpen(false)
                                questViewModel.clearSearch()
                            },
                            onValue = {
                                questViewModel.setSearchKey(it)
                            }
                        )
                    } else {
                        TopAppBar(
                            title = {
                                val selected by questViewModel.view.observeAsState()
                                val scrollState = rememberScrollState()
                                Row(
                                    Modifier.horizontalScroll(scrollState),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    when (navBackStackEntry?.destination?.route) {
                                        BottomNavigationScreens.Overview.route -> {
                                            Text(
                                                "Quests",
                                                modifier = Modifier.padding(end = 16.dp)
                                            )
                                        }
                                        BottomNavigationScreens.Items.route,
                                        BottomNavigationScreens.Quests.route,
                                        BottomNavigationScreens.Maps.route -> {
                                            Chip(
                                                text = "Available",
                                                selected = selected == QuestFilter.AVAILABLE
                                            ) {
                                                questViewModel.setView(QuestFilter.AVAILABLE)
                                            }
                                            Chip(
                                                text = "Locked",
                                                selected = selected == QuestFilter.LOCKED
                                            ) {
                                                questViewModel.setView(QuestFilter.LOCKED)
                                            }
                                            Chip(
                                                text = "Completed",
                                                selected = selected == QuestFilter.COMPLETED
                                            ) {
                                                questViewModel.setView(QuestFilter.COMPLETED)
                                            }
                                            Chip(
                                                text = "All",
                                                selected = selected == QuestFilter.ALL
                                            ) {
                                                questViewModel.setView(QuestFilter.ALL)
                                            }
                                        }
                                        else -> {
                                            Text(
                                                "Quests",
                                                modifier = Modifier.padding(end = 16.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    navViewModel.isDrawerOpen.value = true
                                }) {
                                    Icon(Icons.Filled.Menu, contentDescription = null, tint = White)
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            elevation = 0.dp,
                            actions = {
                                when (navBackStackEntry?.destination?.route) {
                                    BottomNavigationScreens.Items.route,
                                    BottomNavigationScreens.Maps.route,
                                    BottomNavigationScreens.Quests.route -> {
                                        IconButton(onClick = {
                                            questViewModel.setSearchOpen(true)
                                        }) {
                                            Icon(
                                                Icons.Filled.Search,
                                                contentDescription = null,
                                                tint = White
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                },
                isFloatingActionButtonDocked = true
            ) { padding ->
                if (isSearchOpen && navBackStackEntry?.destination?.route != BottomNavigationScreens.Items.route) {
                    QuestSearchBody(searchKey, quests, userData, questViewModel, scope)
                    return@Scaffold
                }

                if (quests.isNullOrEmpty()) {
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
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavigationScreens.Quests.route
                    ) {
                        composable(BottomNavigationScreens.Overview.route) {
                            QuestOverviewScreen(
                                questViewModel = questViewModel,
                                tarkovRepo = tarkovRepo,
                                quests
                            )
                        }
                        composable(BottomNavigationScreens.Quests.route) {
                            QuestTradersScreen(
                                questViewModel = questViewModel,
                                scope = scope,
                                quests = quests,
                                padding = padding,
                                isMapTab = false
                            )
                        }
                        composable(BottomNavigationScreens.Items.route) {
                            QuestItemsScreen(
                                questViewModel = questViewModel,
                                scope = scope,
                                quests = quests,
                                padding = padding,
                                tarkovRepo = tarkovRepo
                            )
                        }
                        composable(BottomNavigationScreens.Maps.route) {
                            QuestTradersScreen(
                                questViewModel = questViewModel,
                                scope = scope,
                                quests = quests,
                                padding = padding,
                                isMapTab = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestItemsScreen(
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope,
    quests: List<Quest>,
    padding: PaddingValues,
    tarkovRepo: TarkovRepo
) {

    Timber.d("1")

    val userData by questViewModel.userData.observeAsState()
    val searchKey by questViewModel.searchKey.observeAsState("")
    val selectedView by questViewModel.view.observeAsState()
    val itemList by questViewModel.itemsList.observeAsState()

    Timber.d("2")

    val itemObjectives = quests.associateWith {
        it.objective?.filterNot { obj ->
            obj.type == "build" || obj.type == "reputation" || obj.number ?: 0 > 1000
        } ?: emptyList()
    }

    Timber.d("3")

    //TODO Fix this dumb bug, scrolling to bottom because of content padding, why???
    if (itemList.isNullOrEmpty() || itemObjectives.isNullOrEmpty()) {
        LoadingItem()
    } else {
        Timber.d("pre-LazyColumn")
        LazyColumn(
            contentPadding = PaddingValues(
                top = 4.dp, bottom = padding.calculateBottomPadding() + 4.dp
            ),
        ) {
            Timber.d("LazyColumn")
            itemObjectives.values.toList().forEach { items ->
                val quest = itemObjectives.entries.find { it.value == items }?.key
                quest?.let { quest ->
                    when (selectedView) {
                        QuestFilter.ALL -> items
                        QuestFilter.AVAILABLE -> {
                            if (quest.isAvailable(userData)) {
                                items.filterNot { userData?.isObjectiveCompleted(it) == true }
                            } else {
                                null
                            }
                        }
                        QuestFilter.LOCKED -> {
                            if (quest.isLocked(userData)) {
                                items.filterNot { userData?.isObjectiveCompleted(it) == true }
                            } else {
                                null
                            }
                        }
                        QuestFilter.COMPLETED -> {
                            items.filter {
                                userData?.isObjectiveCompleted(it) == true
                            }
                        }
                        else -> items
                    }?.forEach { objective ->
                        itemList?.find { item ->
                            item.id == objective.target?.first()
                        }?.let { item ->
                            if (quest.title?.contains(
                                    searchKey,
                                    true
                                ) == true || item.ShortName?.contains(
                                    searchKey,
                                    true
                                ) == true || item.Name?.contains(searchKey, true) == true
                            ) {
                                item {
                                    QuestItemsScreenItem(
                                        quest, objective, item, userData
                                    )
                                }
                                //Timber.d("Pre-QuestItemsScreenItem")
                            }

                        }
                    }
                }
            }
            /*items(itemObjectives.values.toList()) { items ->
            Timber.d("Pre-QuestLet")
            val quest = itemObjectives.entries.find { it.value == items }?.key
            quest?.let { quest ->
                val data = when (selectedView) {
                    QuestFilter.ALL -> items
                    QuestFilter.AVAILABLE -> {
                        if (quest.isAvailable(userData)) {
                            items.filterNot { userData?.isObjectiveCompleted(it) == true }
                        } else {
                            null
                        }
                    }
                    QuestFilter.LOCKED -> {
                        if (quest.isLocked(userData)) {
                            items.filterNot { userData?.isObjectiveCompleted(it) == true }
                        } else {
                            null
                        }
                    }
                    QuestFilter.COMPLETED -> {
                        items.filter {
                            userData?.isObjectiveCompleted(it) == true
                        }
                    }
                    else -> items
                }?.forEach { objective ->
                    itemList?.find { item ->
                        item.id == objective.target?.first()
                    }?.let { item ->
                        if (quest.title?.contains(searchKey, true) == true || item.ShortName?.contains(searchKey, true) == true || item.Name?.contains(searchKey, true) == true) {
                            QuestItemsScreenItem(
                                quest, objective, item, userData
                            )
                            Timber.d("Pre-QuestItemsScreenItem")
                        }

                    }
                }
            }
        }*/
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuestItemsScreenItem(
    quest: Quest,
    objective: Quest.QuestObjective,
    item: Item,
    userData: User?
) {

    val isComplete = userData?.isObjectiveCompleted(objective) ?: false
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
            .fillMaxWidth(),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isComplete) Green400 else Color.Transparent
        ),
        onClick = {
            context.openQuestDetail(quest.id)
        }
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    rememberImagePainter(
                        item.getCleanIcon()
                            ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(38.dp)
                        .height(38.dp)
                        .border((0.25).dp, color = BorderColor)
                        .clickable {
                            context.openFleaDetail(item.id)
                        }
                )
                Column(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.Name ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Image(
                                painter = rememberImagePainter(
                                    data = quest.getGiverName()?.traderIcon()
                                ),
                                contentDescription = "Trader",
                                modifier = Modifier
                                    .border(
                                        width = 0.5.dp,
                                        color = when {
                                            userData?.isQuestCompleted(quest) == true -> {
                                                Green400
                                            }
                                            quest.isLocked(userData) -> Red400
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .size(16.dp)
                                    .clip(CircleShape)
                            )
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    text = quest.title ?: "",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "", tint = Color.White,
                        modifier = Modifier
                            .clickable {
                                if (!isComplete) {
                                    objective.increment()
                                }
                            }
                            .padding(top = 4.dp)
                    )
                    Text(
                        "${userData?.getObjectiveProgress(objective) ?: 0}/${objective.number}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 13.sp
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable {
                                if (userData?.getObjectiveProgress(objective) ?: 0 > 0) {
                                    objective.decrement()
                                }
                            }
                            .padding(bottom = 4.dp)
                    )
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
private fun MapCard(
    mapName: String,
    mapSubtitle: String,
    @DrawableRes icon: Int
) {

    val context = LocalContext.current

    BottomCard({
        context.startActivity(
            Intent(context, QuestInRaidActivity::class.java).apply {
                putExtra("map", mapName)
            })
    }) {
        Row(
            Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
            Column(
                Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = mapName,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium,
                    color = White
                )
                Text(
                    text = mapSubtitle,
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    color = White
                )
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun QuestSearchBody(
    searchKey: String,
    data: List<Quest>,
    userData: User?,
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope
) {

    val items = data.filter {
        if (searchKey.isBlank()) return@filter false
        it.title?.contains(searchKey, ignoreCase = true) == true
                || it.getMaps(mapsList).contains(searchKey, ignoreCase = true)
    }.sortedBy { it.title }

    if (items.isNullOrEmpty()) {
        EmptyText("Search Quests.")
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
    ) {
        items.forEach {
            item {
                QuestCard(it, userData, questViewModel, scope)
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun QuestTradersScreen(
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope,
    quests: List<Quest>,
    padding: PaddingValues,
    isMapTab: Boolean
) {
    val userData by questViewModel.userData.observeAsState()
    val selectedView by questViewModel.view.observeAsState()
    val searchKey by questViewModel.searchKey.observeAsState("")

    val pagerState = if (!isMapTab) {
        rememberPagerState(pageCount = Traders.values().size)
    } else {
        rememberPagerState(pageCount = Maps.values().size)
    }

    val completedQuests = userData?.quests?.values?.filter { it?.completed == true }?.map { it?.id }

    Column(
        Modifier.fillMaxWidth()
    ) {
        if (!isMapTab) {
            TraderTabs(pagerState, scope)
        } else {
            MapsTab(pagerState, scope)
        }

        HorizontalPager(modifier = Modifier.fillMaxWidth(), state = pagerState) { page ->
            val questsList = if (!isMapTab) {
                val trader = Traders.values()[page]
                quests.filter { it.giver?.name == trader.id }
            } else {
                val map = Maps.values()[page]
                quests.filter {
                    if (map.int == -1) {
                        it.getMapsIDs()?.contains(map.int) == true
                    } else {
                        it.getMapsIDs()?.contains(map.int) == true
                    }
                }
            }
            val data = when (selectedView) {
                QuestFilter.AVAILABLE -> {
                    questsList.filter {
                        it.isAvailable(userData)
                    }
                }
                QuestFilter.LOCKED -> {
                    questsList.filter {
                        it.isLocked(userData)
                    }
                }
                QuestFilter.ALL -> questsList
                QuestFilter.COMPLETED -> {
                    questsList.filter {
                        completedQuests?.contains(it.id.toInt()) == true
                    }
                }
                else -> questsList
            }.filter {
                it.title?.contains(searchKey, ignoreCase = true) == true
                        || it.getMaps(mapsList).contains(searchKey, ignoreCase = true)
            }

            if (data.isEmpty()) {
                EmptyText(text = "No Quests.")
                return@HorizontalPager
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = padding.calculateBottomPadding()
                )
            ) {
                data.forEach {
                    item {
                        QuestCard(it, userData, questViewModel, scope)
                    }
                }
            }
        }
    }

}

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun QuestCard(
    quest: Quest,
    userData: User?,
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = {
                context.openActivity(QuestDetailActivity::class.java) {
                    putString("questID", quest.id)
                }
            }, onLongClick = {
                MaterialDialog(context).show {
                    title(text = "Add to Needed Items?")
                    message(text = "This will add these items to the needed items list on the Flea Market screen.")
                    positiveButton(text = "ADD") {
                        for (requirement in quest.objective!!) {
                            when (requirement.type) {
                                "collect", "find", "key", "build" -> {
                                    val itemID = requirement.target?.first()
                                    itemID?.let {
                                        userRefTracker("items/${it}/questObjective/${requirement.id?.addQuotes()}").setValue(
                                            requirement.number
                                        )
                                    }
                                }
                            }
                        }
                    }
                    negativeButton(text = "CANCEL")
                }

            }),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
    ) {
        Column {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = quest.trader().icon),
                    contentDescription = "",
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = when {
                                userData?.isQuestCompleted(quest) == true -> {
                                    Green400
                                }
                                quest.isLocked(userData) -> Red400
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .size(24.dp)

                )
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "${quest.trader().id}", style = MaterialTheme.typography.caption
                )
            }
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp)
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = quest.title.toString(),
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = quest.getMaps(mapsList),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                Column(
                    Modifier.fillMaxHeight()
                ) {
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Level ${quest.requirement?.level}",
                            style = MaterialTheme.typography.overline
                        )
                    }*/
                    when {
                        quest.isLocked(userData) -> {
                            OutlinedButton(
                                onClick = {
                                    questViewModel.skipToQuest(quest)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, color = Color.Gray)
                            ) {
                                Text("SKIP TO")
                            }
                        }
                        quest.isAvailable(userData) -> {
                            OutlinedButton(
                                onClick = { quest.completed() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Red400
                                ),
                                border = BorderStroke(1.dp, color = Red400)
                            ) {
                                Text("COMPLETE")
                            }
                        }
                        userData?.isQuestCompleted(quest) == true -> {
                            OutlinedButton(
                                onClick = { quest.undo(true) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, color = Color.Gray)
                            ) {
                                Text("UNDO")
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = {
                                    questViewModel.skipToQuest(quest)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, color = Color.Gray)
                            ) {
                                Text("SKIP TO")
                            }
                        }
                    }
                }
            }
            Divider(color = DividerDark)
            Column(
                Modifier.padding(vertical = 8.dp)
            ) {
                quest.objective?.forEach {
                    QuestObjectiveItem(it, questViewModel, scope, userData, quest)
                }
            }
        }
    }
}

@Composable
private fun QuestObjectiveItem(
    questObjective: Quest.QuestObjective,
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope,
    userData: User?,
    quest: Quest
) {
    var text by remember { mutableStateOf("") }

    Row(
        Modifier
            .height(36.dp)
            .clickable {
                userData?.toggleObjective(quest, questObjective)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        scope.launch {
            text = questViewModel.getObjectiveText(questObjective)
        }
        Box {
            if (userData?.isObjectiveCompleted(questObjective) == true) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(color = Green400)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(20.dp),
                    painter = painterResource(id = questObjective.getIcon()),
                    contentDescription = "",
                    tint = if (userData?.isObjectiveCompleted(questObjective) == true) Green400 else White
                )
                Text(
                    text = text,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp),
                    style = MaterialTheme.typography.body2,
                    color = if (userData?.isObjectiveCompleted(questObjective) == true) Green400 else White,
                    fontWeight = if (userData?.isObjectiveCompleted(questObjective) == true) FontWeight.Normal else FontWeight.Normal
                )
            }
        }


    }
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun TraderTabs(
    pagerState: PagerState,
    scope: CoroutineScope
) {
    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = Red400
            )
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

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun MapsTab(
    pagerState: PagerState,
    scope: CoroutineScope
) {
    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = Red400
            )
        },
    ) {
        Maps.values().forEachIndexed { index, map ->
            LeadingIconTab(
                text = { Text(map.id, fontFamily = Bender) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = map.icon),
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

@Composable
private fun QuestOverviewScreen(
    questViewModel: QuestMainViewModel,
    tarkovRepo: TarkovRepo,
    quests: List<Quest>
) {
    val questTotal = quests.size
    val pmcElimsTotal by questViewModel.pmcElimsTotal.observeAsState()
    val scavElimsTotal by questViewModel.scavElimsTotal.observeAsState()
    val questItemsTotal by questViewModel.questItemsTotal.observeAsState()
    val questFIRItemsTotal by questViewModel.questFIRItemsTotal.observeAsState()
    val handoverItemsTotal by questViewModel.handoverItemsTotal.observeAsState()
    val placedTotal by questViewModel.placedTotal.observeAsState()
    val pickupTotal by questViewModel.pickupTotal.observeAsState()

    val questTotalCompletedUser by questViewModel.questTotalCompletedUser.observeAsState()
    val pmcElimsTotalUser by questViewModel.pmcElimsTotalUser.observeAsState()
    val scavElimsTotalUser by questViewModel.scavElimsTotalUser.observeAsState()
    val questItemsTotalUser by questViewModel.questItemsTotalUser.observeAsState()
    val placedTotalUser by questViewModel.placedTotalUser.observeAsState()
    val pickupTotalUser by questViewModel.pickupTotalUser.observeAsState()

    val FIRItemsTotalUser by questViewModel.questFIRItemsTotalUser.observeAsState()
    val handoverItemsTotalUser by questViewModel.handoverItemsTotalUser.observeAsState()

    val expTotalUser by questViewModel.expTotal.observeAsState(0)

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = 92.dp),
    ) {
        item {
            OverviewItem(
                color = Green500,
                s1 = "Quests Completed",
                s2 = "$questTotalCompletedUser/$questTotal",
                progress = (questTotalCompletedUser?.toDouble()
                    ?.div(questTotal.toDouble()))?.toFloat(),
            )
        }
        item {
            OverviewItem(
                color = Red500,
                s1 = "PMC Eliminations",
                s2 = "$pmcElimsTotalUser/$pmcElimsTotal",
                progress = (pmcElimsTotalUser?.toDouble()
                    ?.div(pmcElimsTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_sniper_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFFFF9800),
                s1 = "Scav Eliminations",
                s2 = "$scavElimsTotalUser/$scavElimsTotal",
                progress = (scavElimsTotalUser?.toDouble()
                    ?.div(scavElimsTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_target_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Quest Items",
                s2 = "$questItemsTotalUser/$questItemsTotal",
                progress = (questItemsTotalUser?.toDouble()
                    ?.div(questItemsTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.ic_search_black_24dp
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Found in Raid Items",
                s2 = "$FIRItemsTotalUser/$questFIRItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_baseline_check_circle_outline_24
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Handover Items",
                s2 = "$handoverItemsTotalUser/$handoverItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_baseline_swap_horizontal_circle_24
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF9C27B0),
                s1 = "Placed Objectives",
                s2 = "$placedTotalUser/$placedTotal",
                progress = (placedTotalUser?.toDouble()
                    ?.div(placedTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_low_importance_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF9C27B0),
                s1 = "Pickup Objectives",
                s2 = "$pickupTotalUser/$pickupTotal",
                progress = (pickupTotalUser?.toDouble()
                    ?.div(pickupTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_upward_arrow_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFFFF9800),
                s1 = "Total EXP",
                s2 = DecimalFormat("#,###,###").format(expTotalUser),
                progress = null,
                icon = R.drawable.ic_baseline_star_half_24
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
    progress: Float? = null
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
                if (progress != null)
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
                        color = Color.White,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Chip(
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

        items.forEachIndexed { _, item ->
            BottomNavigationItem(
                icon = {
                    if (item.icon != null) {
                        Icon(item.icon, "")
                    } else {
                        Icon(
                            painter = painterResource(id = item.iconDrawable!!),
                            contentDescription = item.resourceId
                        )
                    }
                },
                label = { Text(item.resourceId) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                alwaysShowLabel = false, // This hides the title for the unselected items
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
        BottomNavigationItem(selected = false, onClick = {}, icon = {}, enabled = false)
    }
}

@ExperimentalMaterialApi
@Composable
private fun BottomCard(
    onClick: () -> Unit,
    color: Color = Color(0xFF212121),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = color,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        content = content
    )
}

sealed class BottomNavigationScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Overview :
        BottomNavigationScreens("Overview", "Overview", null, R.drawable.ic_baseline_dashboard_24)

    object Quests : BottomNavigationScreens(
        "Quests",
        "Quests",
        null,
        R.drawable.ic_baseline_assignment_turned_in_24
    )

    object Items :
        BottomNavigationScreens("Items", "Items", null, R.drawable.ic_baseline_assignment_24)

    object Maps : BottomNavigationScreens("Maps", "Maps", null, R.drawable.ic_baseline_map_24)
}

enum class QuestFilter {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    ALL
}