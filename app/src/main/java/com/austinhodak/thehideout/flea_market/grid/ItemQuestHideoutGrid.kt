package com.austinhodak.thehideout.flea_market.grid

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.hideoutList
import com.austinhodak.thehideout.quests.Chip
import com.austinhodak.thehideout.quests.QuestFilter
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemQuestHideoutGridScreen(navViewModel: NavViewModel, tarkovRepo: TarkovRepo, questViewModel: QuestMainViewModel, hideoutViewModel: HideoutMainViewModel) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val isSearchOpen by questViewModel.isSearchOpen.observeAsState(false)
    val searchKey by questViewModel.searchKey.observeAsState("")
    val userData by questViewModel.userData.observeAsState()
    val allItems by navViewModel.allItems.observeAsState()
    val allQuests by questViewModel.questsList.observeAsState(emptyList())
    val allHideoutModules = hideoutList.hideout?.modules
    val selectedView by questViewModel.view.observeAsState()
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
                TopBar(questViewModel, navViewModel, sort) {
                    sort = it
                }
            }
        }
    ) {
        val pagerState = rememberPagerState(pageCount = 3)
        Column(
            Modifier.fillMaxWidth()
        ) {
            Tabs(
                pagerState = pagerState,
                scope = scope,
                items = listOf("BOTH", "QUESTS", "HIDEOUT")
            )
            val questObjectives = when (selectedView) {
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
            }

            val modules = when (selectedView) {
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
            }

            var data = when (pagerState.currentPage) {
                0 -> {
                    questObjectives.map { obj ->
                        val item = allItems?.find { it.id.equals(obj.target) || it.id == obj.targetItem?.id }
                        Pair(item?.pricing, obj.number)
                    }.plus(
                        modules?.map { require ->
                            val item = allItems?.find { it.id == require?.name }
                            Pair(item?.pricing, require?.quantity)
                        } ?: emptyList()
                    ).groupBy {
                        it.first
                    }.map {
                        GridItemData(pricing = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                    }
                }
                1 -> {
                    questObjectives.map { obj ->
                        val item = allItems?.find { it.id.equals(obj.target) || it.id == obj.targetItem?.id }
                        Pair(item?.pricing, obj.number)
                    }.groupBy {
                        it.first
                    }.map {
                        GridItemData(pricing = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                    }
                }
                2 -> {
                    (modules?.map { require ->
                        val item = allItems?.find { it.id == require?.name }
                        Pair(item?.pricing, require?.quantity)
                    } ?: emptyList()).groupBy {
                        it.first
                    }.map {
                        GridItemData(pricing = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                    }
                }
                else -> questObjectives.map { obj ->
                    val item = allItems?.find { it.id.equals(obj.target) || it.id == obj.targetItem?.id }
                    Pair(item?.pricing, obj.number)
                }.plus(
                    modules?.map { require ->
                        val item = allItems?.find { it.id == require?.name }
                        Pair(item?.pricing, require?.quantity)
                    } ?: emptyList()
                ).groupBy {
                    it.first
                }.map {
                    GridItemData(pricing = it.key, text = it.value.sumOf { it.second ?: 0 }.toString())
                }
            }.filter {
                it.pricing?.name != null && it.text?.toLongOrNull() != null && it.text.toLong() < 1000
            }.filter {
                it.pricing?.name?.contains(searchKey, true) == true
                        || it.pricing?.shortName?.contains(searchKey, true) == true
            }

            data = when (sort) {
                0 -> data.sortedBy { it.pricing?.name }
                1 -> data.sortedByDescending { it.text?.toLongOrNull() }
                2 -> data.sortedBy { it.text?.toLongOrNull() }
                else -> data.sortedBy { it.pricing?.name }
            }

            if (data.isNullOrEmpty()) {
                LoadingItem()
            }

            LazyVerticalGrid(cells = GridCells.Adaptive(52.dp)) {
                items(items = data) { item ->
                    GridItem(url = item.pricing?.getCleanIcon(), text = item.text, onClick = {
                        item.pricing?.id?.let { id -> context.openFleaDetail(id) }
                    })
                }
            }
        }
    }
}

data class GridItemData(
    val pricing: Pricing?,
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

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun Tabs(
    pagerState: PagerState,
    scope: CoroutineScope,
    items: List<String>
) {
    TabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = Red400
            )
        },
    ) {
        items.forEachIndexed { index, string ->
            Tab(
                text = { Text(string, fontFamily = Bender) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                selectedContentColor = Red400,
                unselectedContentColor = White
            )
        }
    }
}

@Composable
private fun TopBar(questViewModel: QuestMainViewModel, navViewModel: NavViewModel, sort: Int, sortSelected: (Int) -> Unit) {
    val context = LocalContext.current
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