package com.austinhodak.thehideout.features.quests.inraid

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.austinhodak.tarkovapi.models.QuestExtra
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Maps
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.ui.common.EmptyText
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.fsUser
import com.austinhodak.thehideout.features.map.MapsActivity
import com.austinhodak.thehideout.features.quests.QuestDetailActivity
import com.austinhodak.thehideout.features.quests.viewmodels.QuestInRaidViewModel
import com.austinhodak.thehideout.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.austinhodak.thehideout.features.quests.inraid.QuestInRaidActivity.Types.*
import com.austinhodak.thehideout.ui.theme.HideoutTheme

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class QuestInRaidActivity : GodActivity() {

    private val questViewModel: QuestInRaidViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapString = intent.getStringExtra("map") ?: "Customs"
        val mapID = Maps.values().find { it.id == mapString }?.int

        setContent {
            HideoutTheme {

                val scaffoldState = rememberScaffoldState()
                val navController = rememberNavController()

                val userData by fsUser.observeAsState()
                val quests by tarkovRepo.getAllQuests().collectAsState(initial = emptyList())
                val questExtra = questViewModel.questsExtra.observeAsState().value?.flatMap {
                    it.objectives ?: emptyList()
                }

                val itemIDs = quests.flatMap {
                    it.objective ?: emptyList()
                }.map {
                    it.target?.first()
                }

                val items by tarkovRepo.getItemByID(itemIDs.filterNotNull()).collectAsState(initial = emptyList())

                var availableQuests: Map<String?, List<Quest.QuestObjective>>? = quests.filter {
                    it.isAvailable(userData)
                }.flatMap {
                    it.objective ?: emptyList()
                }.filter {
                    userData?.progress?.isQuestObjectiveCompleted(it) == false && (it.location == mapID.toString() || it.location == "-1")
                }.filterNot {
                    it.type == "collect" || it.type == "find" || it.type == "build" || it.type == "reputation"
                }.groupBy {
                    it.type
                }

                if (quests.isNullOrEmpty()) {
                    availableQuests = null
                }

                Timber.d(availableQuests.toString())

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        AmmoDetailToolbar(
                            title = "In Raid at $mapString",
                            onBackPressed = {
                                finish()
                            },
                            actions = {
                                IconButton(onClick = {
                                    openActivity(MapsActivity::class.java) {
                                        putString("map", mapString)
                                    }
                                }) {
                                    Icon(painterResource(id = R.drawable.ic_baseline_map_24), contentDescription = "Map", tint = Color.White)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        InRaidBottomNav(navController = navController)
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            finish()
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "", tint = Color.Black)
                        }
                    },
                    isFloatingActionButtonDocked = true,
                    floatingActionButtonPosition = FabPosition.Center
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavigationScreens.Tasks.route
                    ) {
                        composable(BottomNavigationScreens.Tasks.route) {
                            TasksScreen(availableQuests, questExtra, quests, userData)
                        }
                        composable(BottomNavigationScreens.Items.route) {
                            //EmptyText(text = "Coming soon.")
                            ItemsScreen(questExtra, quests, userData, items)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ItemsScreen(
        questExtra: List<QuestExtra.QuestExtraItem.Objective?>?,
        quests: List<Quest>,
        userData: FSUser?,
        items: List<Item>
    ) {
        val objectives = quests.filter {
            it.isAvailable(userData)
        }.flatMap {
            it.objective ?: emptyList()
        }.filter {
            userData?.progress?.isQuestObjectiveCompleted(it) == false
        }.filterNot {
            it.type == "build" || it.type == "reputation"
        }

        val neededItems: List<Item> = objectives.mapNotNull { obj ->
            items.find { it.id == obj.target?.first() }
        }

        if (neededItems?.isEmpty() == true) {
            EmptyText(text = "No items needed.")
            return
        } else if (neededItems == null) {
            LoadingItem()
            return
        }

        LazyVerticalGrid(columns = GridCells.Adaptive(52.dp)) {
            items(items = neededItems) { item ->
                val totalNeeded = objectives.filter {
                    it.target?.first() == item.id
                }.sumOf {
                    it.number ?: 0
                }

                val totalProgress = objectives.filter {
                    it.target?.first() == item.id
                }.sumOf {
                    userData?.progress?.getQuestObjectiveProgress(it) ?: 0
                }

                val color = BorderColor
                val context = LocalContext.current
                Box(
                    Modifier.combinedClickable(
                        onClick = {

                        },
                        onDoubleClick = {

                        },
                        onLongClick = {
                            context.openActivity(FleaItemDetail::class.java) {
                                putString("id", item.id)
                            }
                        }
                    )
                ) {
                    Image(
                        rememberImagePainter(item.pricing?.getCleanIcon()),
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
                        text = "$totalProgress/$totalNeeded",
                        Modifier
                            .clip(RoundedCornerShape(topStart = 5.dp))
                            .background(color)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                            .align(Alignment.BottomEnd),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun TasksScreen(
        availableQuests: Map<String?, List<Quest.QuestObjective>>?,
        questExtra: List<QuestExtra.QuestExtraItem.Objective?>?,
        quests: List<Quest>,
        userData: FSUser?
    ) {
        AnimatedContent(targetState = availableQuests) {
            when {
                it?.isEmpty() == true -> {
                    EmptyText(text = "No tasks for this map.")
                }
                it == null -> {
                    LoadingItem()
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp)) {
                        availableQuests?.forEach { entry ->
                            val type = entry.key
                            val objectives = entry.value
                            if (type == null) return@forEach
                            item {
                                ObjectiveCategoryCard(
                                    type = valueOf(type.uppercase()),
                                    objectives,
                                    questExtra,
                                    quests,
                                    userData
                                )
                            }
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
        object Tasks :
            BottomNavigationScreens("Tasks", "Tasks", null, R.drawable.ic_baseline_fact_check_24)

        object Items :
            BottomNavigationScreens("Items", "Items", null, R.drawable.ic_round_ballot_24)
    }

    @Composable
    fun InRaidBottomNav(
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
                            Icon(
                                painter = painterResource(id = item.iconDrawable!!),
                                contentDescription = item.resourceId
                            )
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

    @Composable
    fun ObjectiveCategoryCard(
        type: Types,
        objectives: List<Quest.QuestObjective>,
        questExtra: List<QuestExtra.QuestExtraItem.Objective?>?,
        quests: List<Quest>,
        userData: FSUser?
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Icon(
                            painter = painterResource(id = type.name.getObjectiveIcon()),
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = White
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = type.title,
                            style = MaterialTheme.typography.caption,
                            color = White
                        )
                    }
                }
                objectives.forEach { objective ->
                    ObjectiveItem(
                        objective,
                        type,
                        questExtra?.find { it?.id.toString() == objective.id },
                        quests,
                        userData
                    )
                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun ObjectiveItem(
        objective: Quest.QuestObjective,
        type: Types,
        questExtra: QuestExtra.QuestExtraItem.Objective?,
        quests: List<Quest>,
        userData: FSUser?
    ) {
        var text by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        scope.launch {
            text = questViewModel.getObjectiveText(objective)
        }

        val subtitle = questExtra?.with?.joinToString(", ")

        val quest = quests.find {
            it.objective?.contains(objective) == true
        }

        when (type) {
            KILL, PICKUP, PLACE -> {
                ObjectiveItemBasic(
                    title = text,
                    icon = null,
                    subtitle = subtitle,
                    quest = quest,
                    userData = userData,
                    objective = objective
                )
            }
            KEY -> {
                val item: Item? by tarkovRepo.getItemByID(objective.target?.first() ?: "")
                    .collectAsState(initial = null)
                if (item != null)
                    ObjectiveItemPricing(objective = objective, pricing = item?.pricing)
            }
            COLLECT, FIND, BUILD -> return
            else -> ObjectiveItemBasic(
                title = text,
                subtitle = subtitle,
                icon = null,
                quest = quest,
                userData = userData,
                objective = objective
            )
        }
    }

    @Composable
    private fun ObjectiveItemBasic(
        title: String,
        subtitle: Any? = null,
        icon: Int? = null,
        userData: FSUser?,
        objective: Quest.QuestObjective,
        quest: Quest?
    ) {

        val isCompleted = userData?.progress?.isQuestObjectiveCompleted(objective) ?: false

        val sub = if (subtitle is String) {
            subtitle.toString()
        } else if (subtitle is List<*>) {
            subtitle.joinToString(", ")
        } else {
            subtitle.toString()
        }

        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        quest?.let {
                            MaterialDialog(this).show {
                                title(text = "Mark Objective as Completed?")
                                //message(text = "")
                                positiveButton(text = "Complete") { dialog ->
                                    userData?.toggleObjective(it, objective)
                                }
                                negativeButton(text = "Cancel")
                            }
                        }
                    },
                    onLongClick = {
                        quest?.let {
                            openActivity(QuestDetailActivity::class.java) {
                                putString("questID", it.id)
                            }
                        }
                    }
                )
                .padding(end = 16.dp)
                .defaultMinSize(minHeight = 24.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(color = Green400)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(36.dp)
                        .padding(start = 16.dp)
                ) {

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            modifier = Modifier
                                .padding(start = 0.dp, end = 16.dp),
                            style = MaterialTheme.typography.body2,
                            color = if (isCompleted) Green400 else White,
                            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Normal
                        )
                        if (subtitle != null) {
                            Text(
                                text = "$sub",
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }

                    if (icon != null && icon != R.drawable.icons8_map_96) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = title,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(24.dp)
                        )
                    }
                }
            }

            ////

            /*Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body2
                )
                if (subtitle != null) {
                    Text(
                        text = "With $sub",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            if (icon != null && icon != R.drawable.icons8_map_96) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(24.dp)
                )
            }*/
        }
    }

    @Composable
    private fun ObjectiveItemPricing(
        objective: Quest.QuestObjective,
        pricing: Pricing?
    ) {
        val context = LocalContext.current

        Column(
            Modifier
                .combinedClickable(
                    onClick = {
                        context.openActivity(FleaItemDetail::class.java) {
                            putString("id", pricing?.id)
                        }
                    },
                    onLongClick = {
                        MaterialDialog(context).show {
                            title(text = "Add to Needed Items?")
                            message(text = "This will add these items to the needed items list on the Flea Market screen.")
                            positiveButton(text = "ADD") {
                                userRefTracker("items/${pricing?.id}/questObjective/${objective.id?.addQuotes()}").setValue(
                                    objective.number
                                )
                            }
                            negativeButton(text = "CANCEL")
                        }
                    }
                )
                .padding(vertical = 4.dp)
        ) {
            Row(
                Modifier
                    .padding(end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${objective.getNumberString()}${pricing?.name}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = pricing?.getPrice()?.asCurrency() ?: "",
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                }
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
            }
        }
    }

    enum class Types(var title: String) {
        KILL("YOU NEED TO KILL"),
        COLLECT("YOU NEED TO FIND"),
        PICKUP("YOU NEED TO PICKUP"),
        PLACE("YOU NEED TO PLACE"),
        MARK("YOU NEED TO MARK"),
        LOCATE("YOU NEED TO LOCATE"),
        FIND("YOU NEED TO FIND (IN RAID)"),
        WARNING("YOU NEED TO"),
        SURVIVE("YOU NEED TO SURVIVE"),
        KEY("YOU NEED KEYS"),
        REPUTATION("YOU NEED TO REACH"),
        BUILD("YOU NEED TO BUILD"),
        SKILL("YOU NEED TO REACH")
    }
}