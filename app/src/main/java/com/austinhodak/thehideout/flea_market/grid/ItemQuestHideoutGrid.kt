package com.austinhodak.thehideout.flea_market.grid

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.fsUser
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.hideoutList
import com.austinhodak.thehideout.quests.Chip
import com.austinhodak.thehideout.quests.QuestFilter
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun ItemQuestHideoutGridScreen(navViewModel: NavViewModel, tarkovRepo: TarkovRepo, questViewModel: QuestMainViewModel, hideoutViewModel: HideoutMainViewModel) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val isSearchOpen by questViewModel.isSearchOpen.observeAsState(false)
    val searchKey by questViewModel.searchKey.observeAsState("")
    val userData by fsUser.observeAsState()
    val allItems by navViewModel.allItems.observeAsState()
    val allQuests by questViewModel.questsList.observeAsState(emptyList())
    val allHideoutModules = hideoutList.hideout?.modules

    val selectedView by questViewModel.view.observeAsState()
    val selectedViews by questViewModel.views.observeAsState(initial = listOf(QuestFilter.AVAILABLE))

    val completedQuests = userData?.progress?.getCompletedQuestIDs()

    var sort by rememberSaveable {
        mutableStateOf(1)
    }

    Scaffold(
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
                TopBar(questViewModel, navViewModel, sort, {
                    questViewModel.toggleView(it)
                }) {
                    sort = it
                }
            }
        }
    ) {
        var pagerState: Int by remember {
            mutableStateOf(0)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState]),
                        color = Red400
                    )
                },
            ) {
                listOf("BOTH", "QUESTS", "HIDEOUT").forEachIndexed { index, string ->
                    Tab(
                        text = { Text(string, fontFamily = Bender) },
                        selected = pagerState == index,
                        onClick = {
                            scope.launch {
                                pagerState = index
                            }
                        },
                        selectedContentColor = Red400,
                        unselectedContentColor = White
                    )
                }
            }

            val questObjectivesNew = mutableListOf<Quest.QuestObjective>()
            if (selectedViews.contains(QuestFilter.AVAILABLE)) {
                questObjectivesNew.addAll(
                    allQuests.filter {
                        it.isAvailable(userData)
                    }.flatMap { quest ->
                        quest.objective?.filterNot { obj ->
                            userData?.progress?.isQuestObjectiveCompleted(obj) == true
                        } ?: emptyList()
                    }
                )
            }

            if (selectedViews.contains(QuestFilter.LOCKED)) {
                questObjectivesNew.addAll(
                    allQuests.filter {
                        it.isLocked(userData)
                    }.flatMap { quest ->
                        quest.objective ?: emptyList()
                    }
                )
            }

            if (selectedViews.contains(QuestFilter.COMPLETED)) {
                questObjectivesNew.addAll(
                    allQuests.filter {
                        completedQuests?.contains(it.id) == true
                    }.flatMap { quest ->
                        quest.objective ?: emptyList()
                    }
                )
            }

            if (selectedViews.contains(QuestFilter.ALL)) {
                questObjectivesNew.clear()
                questObjectivesNew.addAll(
                    allQuests.flatMap { quest ->
                        quest.objective ?: emptyList()
                    }
                )
            }

            /*val questObjectives = when (selectedView) {
                QuestFilter.AVAILABLE -> {
                    allQuests.filter {
                        it.isAvailable(userData)
                    }.flatMap { quest ->
                        quest.objective?.filterNot { obj ->
                            userData?.progress?.isQuestObjectiveCompleted(obj) == true
                        } ?: emptyList()
                    }
                }
                QuestFilter.LOCKED -> {
                    allQuests.filter {
                        it.isLocked(userData)
                    }.flatMap { quest ->
                        quest.objective ?: emptyList()
                    }
                }
                QuestFilter.ALL -> allQuests.flatMap { quest ->
                    quest.objective ?: emptyList()
                }
                QuestFilter.COMPLETED -> {
                    allQuests.filter {
                        completedQuests?.contains(it.id) == true
                    }.flatMap { quest ->
                        quest.objective ?: emptyList()
                    }
                }
                else -> allQuests.flatMap { quest ->
                    quest.objective ?: emptyList()
                }
            }*/


           /* val modules = when (selectedView) {
                QuestFilter.COMPLETED -> allHideoutModules?.filter {
                    userData?.progress?.isHideoutModuleCompleted(it?.id.toString()) == true
                }?.flatMap {
                    it?.require ?: emptyList()
                }
                QuestFilter.AVAILABLE -> allHideoutModules?.filter {
                    if (userData?.progress?.isHideoutModuleCompleted(it?.id.toString()) == true) return@filter false
                    if (it?.getModuleRequirements(allHideoutModules)?.isEmpty() == true) {
                        true
                    } else {
                        userData?.progress?.getCompletedHideoutIDs()?.containsAll(it?.getModuleRequirements(allHideoutModules)?.map { it.toString() }!!) == true
                    }
                }?.flatMap {
                    it?.require ?: emptyList()
                }
                QuestFilter.LOCKED -> allHideoutModules?.filter {
                    userData?.progress?.getCompletedHideoutIDs()?.containsAll(it?.getModuleRequirements(allHideoutModules)?.map { it.toString() }!!) == false
                }?.flatMap {
                    it?.require ?: emptyList()
                }
                QuestFilter.ALL -> allHideoutModules?.flatMap {
                    it?.require ?: emptyList()
                }
                else -> allHideoutModules?.flatMap {
                    it?.require ?: emptyList()
                }
            }*/

            val modulesNew = mutableListOf<Hideout.Module.Require>()
            if (selectedViews.contains(QuestFilter.AVAILABLE)) {
                modulesNew.addAll(
                    allHideoutModules?.filter {
                        if (userData?.progress?.isHideoutModuleCompleted(it?.id.toString()) == true) return@filter false
                        if (it?.getModuleRequirements(allHideoutModules)?.isEmpty() == true) {
                            true
                        } else {
                            userData?.progress?.getCompletedHideoutIDs()?.containsAll(it?.getModuleRequirements(allHideoutModules)?.map { it.toString() }!!) == true
                        }
                    }?.flatMap {
                        it?.require ?: emptyList()
                    }?.filterNotNull() ?: emptyList()
                )
            }

            if (selectedViews.contains(QuestFilter.LOCKED)) {
                modulesNew.addAll(
                    allHideoutModules?.filter {
                        userData?.progress?.getCompletedHideoutIDs()?.containsAll(it?.getModuleRequirements(allHideoutModules)?.map { it.toString() }!!) == false
                    }?.flatMap {
                        it?.require ?: emptyList()
                    }?.filterNotNull() ?: emptyList()
                )
            }

            if (selectedViews.contains(QuestFilter.COMPLETED)) {
                modulesNew.addAll(
                    allHideoutModules?.filter {
                        userData?.progress?.isHideoutModuleCompleted(it?.id.toString()) == true
                    }?.flatMap {
                        it?.require ?: emptyList()
                    }?.filterNotNull() ?: emptyList()
                )
            }

            if (selectedViews.contains(QuestFilter.ALL)) {
                modulesNew.clear()
                modulesNew.addAll(
                    allHideoutModules?.flatMap {
                        it?.require ?: emptyList()
                    }?.filterNotNull() ?: emptyList()
                )
            }

            var data = when (pagerState) {
                0 -> {
                    questObjectivesNew.map { obj ->
                        val item = allItems?.find { obj.target?.contains(it.id) == true || it.id.equals(obj.target) || it.id == obj.targetItem?.id }
                        Pair(item, obj.number)
                    }.plus(
                        modulesNew.map { require ->
                            val item = allItems?.find { it.id == require.name }
                            Pair(item, require.quantity)
                        } ?: emptyList()
                    ).groupBy {
                        it.first
                    }.map {
                        GridItemData(item = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                    }
                }
                1 -> {
                    questObjectivesNew.map { obj ->
                        val item = allItems?.find { obj.target?.contains(it.id) == true || it.id.equals(obj.target) || it.id == obj.targetItem?.id }
                        Pair(item, obj.number)
                    }.groupBy {
                        it.first
                    }.map {
                        GridItemData(item = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                    }
                }
                2 -> {
                    (modulesNew.map { require ->
                        val item = allItems?.find { it.id == require.name }
                        Pair(item, require.quantity)
                    } ?: emptyList()).groupBy {
                        it.first
                    }.map {
                        GridItemData(item = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                    }
                }
                else -> questObjectivesNew.map { obj ->
                    val item = allItems?.find { obj.target?.contains(it.id) == true || it.id.equals(obj.target) || it.id == obj.targetItem?.id }
                    Pair(item, obj.number)
                }.plus(
                    modulesNew.map { require ->
                        val item = allItems?.find { it.id == require.name }
                        Pair(item, require.quantity)
                    } ?: emptyList()
                ).groupBy {
                    it.first
                }.map {
                    GridItemData(item = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                }
            }.filter {
                it.item?.pricing?.name != null && it.text?.toLongOrNull() != null && it.text.toLong() < 1000
            }.filter {
                it.item?.pricing?.name?.contains(searchKey, true) == true
                        || it.item?.pricing?.shortName?.contains(searchKey, true) == true
            }

            data = when (sort) {
                0 -> data.sortedBy { it.item?.pricing?.name }
                1 -> data.sortedByDescending { it.text?.toLongOrNull() }
                2 -> data.sortedBy { it.text?.toLongOrNull() }
                else -> data.sortedBy { it.item?.pricing?.name }
            }

            if (data.isEmpty()) {
                LoadingItem()
            }

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            data.groupBy { it.item?.itemType }.forEach { (type, data) ->
                val name = when (type?.name) {
                    "NONE" -> "BARTER"
                    else -> type?.name
                }?.lowercase()?.capitalize(Locale.current)
                Text(text = name ?: "", style = MaterialTheme.typography.subtitle1, modifier = Modifier.padding(16.dp))
                FlowRow(
                    mainAxisSize = SizeMode.Expand
                ) {
                    data.forEach { item ->

                        GridItem(url = item.item?.pricing?.getCleanIcon(), text = item.text, onClick = {
                            item.item?.pricing?.id?.let { id -> context.openFleaDetail(id) }
                        }, modifier = Modifier.size(screenWidth / 8))
                    }
                }
            }
        }
    }
}

data class GridItemData(
    val item: Item?,
    val text: String?,
    val color: Color? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridItem(
    modifier: Modifier = Modifier,
    url: String?,
    text: String? = null,
    color: Color? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier.combinedClickable(
            onClick = {
                onClick?.invoke()
            },
            onLongClick = {
                onLongClick?.invoke()
            }
        )
    ) {
        Image(
            fadeImagePainter(url ?: ""),
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
                .border(0.1.dp, color ?: BorderColor),
        )
        text?.let {
            Text(
                text = "$text",
                Modifier
                    .clip(RoundedCornerShape(topStart = 5.dp))
                    .background(color ?: BorderColor)
                    .padding(horizontal = 2.dp, vertical = 1.dp)
                    .align(Alignment.BottomEnd),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun TopBar(questViewModel: QuestMainViewModel, navViewModel: NavViewModel, sort: Int, clicked: (QuestFilter) -> Unit, sortSelected: (Int) -> Unit) {
    val context = LocalContext.current
    val views by questViewModel.views.observeAsState()

    Timber.d("${views}")
    TopAppBar(
        title = {
            val selected by questViewModel.view.observeAsState()
            val scrollState = rememberScrollState()
            Row(
                Modifier.horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Chip(
                    text = "Active",
                    //selected = selected == QuestFilter.AVAILABLE
                    selected = views?.contains(QuestFilter.AVAILABLE) == true
                ) {
                    clicked(QuestFilter.AVAILABLE)
                    //questViewModel.setView(QuestFilter.AVAILABLE)
                }
                Chip(
                    text = "Locked",
                    //selected = selected == QuestFilter.LOCKED
                    selected = views?.contains(QuestFilter.LOCKED) == true
                ) {
                    clicked(QuestFilter.LOCKED)
                    //questViewModel.setView(QuestFilter.LOCKED)
                }
                Chip(
                    text = "Completed",
                    //selected = selected == QuestFilter.COMPLETED
                    selected = views?.contains(QuestFilter.COMPLETED) == true
                ) {
                    clicked(QuestFilter.COMPLETED)
                    //questViewModel.setView(QuestFilter.COMPLETED)
                }
                Chip(
                    text = "All",
                    //selected = selected == QuestFilter.ALL
                    selected = views?.contains(QuestFilter.ALL) == true
                ) {
                    clicked(QuestFilter.ALL)
                    //questViewModel.setView(QuestFilter.ALL)
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
            IconButton(onClick = {
                questViewModel.setSearchOpen(true)
            }) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = White
                )
            }
            IconButton(onClick = {
                val items = listOf(
                    "Name",
                    "Quantity Needed: High to Low",
                    "Quantity Needed: Low to High"
                )
                MaterialDialog(context).show {
                    title(text = "Sort By")
                    listItemsSingleChoice(items = items, initialSelection = sort) { _, index, _ ->
                        sortSelected(index)
                    }
                }
            }) {
                Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "Sort Ammo", tint = Color.White)
            }
        }
    )
}