package com.austinhodak.thehideout.features.quests

import android.annotation.SuppressLint
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
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
import com.austinhodak.thehideout.ui.common.EmptyText
import com.austinhodak.thehideout.ui.common.LoadingItem
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.firebase.Team
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.fsUser
import com.austinhodak.thehideout.features.quests.QuestDetailActivity.Types.*
import com.austinhodak.thehideout.features.quests.viewmodels.QuestDetailViewModel
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.austinhodak.thehideout.utils.*
import com.bumptech.glide.Glide
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class QuestDetailActivity : GodActivity() {

    private val questViewModel: QuestDetailViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    var quest: Quest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val questID = intent.getStringExtra("questID") ?: "8"
        
        setCrashKeys(
            Pair("questID", questID)
        )

        setContent {
            HideoutTheme {
                ProvideWindowInsets {
                    val quest by questViewModel.questDetails.observeAsState()
                    questViewModel.getQuest(questID)

                    val quests by tarkovRepo.getAllQuests().collectAsState(initial = null)

                    this.quest = quest

                    val questExtra = questViewModel.questsExtra.observeAsState().value?.find { it.id == questID.toInt() }

                    val objectiveTypes = quest?.objective?.groupBy { it.type }

                    val userData by fsUser.observeAsState()
                    val teamData by questViewModel.teamsData.observeAsState()

                    Timber.d(teamData.toString())

                    val systemUiController = rememberSystemUiController()

                    systemUiController.setStatusBarColor(
                        Color.Transparent,
                        darkIcons = false
                    )

                    systemUiController.setNavigationBarColor(DarkPrimary)

                    val navController = rememberNavController()

                    Scaffold(
                        topBar = {
                            Column {
                                Box {
                                    val painter = rememberImagePainter(
                                        questExtra?.image,
                                        builder = {
                                            crossfade(true)
                                        }
                                    )

                                    //Hide if landscape
                                    if (resources.configuration.orientation != ORIENTATION_LANDSCAPE) {
                                        Column {
                                            Image(
                                                painter,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .then(
                                                        if (painter.state is ImagePainter.State.Loading || painter.state is ImagePainter.State.Error) {
                                                            Modifier.height(0.dp)
                                                        } else {
                                                            (painter.state as? ImagePainter.State.Success)
                                                                ?.painter
                                                                ?.intrinsicSize
                                                                ?.let { intrinsicSize ->
                                                                    Modifier.aspectRatio(intrinsicSize.width / intrinsicSize.height)
                                                                } ?: Modifier
                                                        }
                                                    ),
                                                contentScale = ContentScale.FillWidth
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 72.dp,
                                                bottom = 16.dp,
                                                top = if (painter.state is ImagePainter.State.Success) 0.dp else 56.dp
                                            )
                                            .align(Alignment.BottomStart)
                                    ) {
                                        Text(
                                            text = quest?.title ?: "Loading...",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.h6.copy(
                                                shadow = Shadow(
                                                    color = Color.Black,
                                                    offset = Offset(4f, 4f),
                                                    blurRadius = 8f
                                                )
                                            ),
                                            maxLines = 1,
                                            fontSize = 22.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Unlocks at Level ${quest?.requirement?.level}",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.caption.copy(
                                                shadow = Shadow(
                                                    color = Color.Black,
                                                    offset = Offset(4f, 4f),
                                                    blurRadius = 8f
                                                )
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    TopAppBar(
                                        title = { Spacer(modifier = Modifier.fillMaxWidth()) },
                                        backgroundColor = Color.Transparent,
                                        modifier = Modifier.statusBarsPadding(),
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                onBackPressed()
                                            }) {
                                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                            }
                                        },
                                        elevation = 0.dp,
                                        actions = {
                                            OverflowMenu {
                                                quest?.wikiLink?.let {
                                                    WikiItem(url = it)
                                                }
                                                //weapon?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                            }
                                        }
                                    )
                                }

                                if (quest == null) {
                                    LinearProgressIndicator(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(2.dp),
                                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                        backgroundColor = Color.Transparent
                                    )
                                }
                            }
                            /*QuestDetailToolbar(
                                title = "${quest?.title}",
                                onBackPressed = {
                                    finish()
                                },
                                actions = {
                                    OverflowMenu {
                                        quest?.wikiLink?.let { WikiItem(url = it) }
                                    }
                                }
                            )*/
                        },
                        floatingActionButton = {
                            //if (isDebug())
                            quest?.let {
                                if (it.isAvailable(userData) || userData == null) {
                                    ExtendedFloatingActionButton(
                                        icon = {
                                            Icon(
                                                painterResource(id = R.drawable.ic_baseline_assignment_turned_in_24),
                                                contentDescription = null,
                                                tint = Color.Black
                                            )
                                        },
                                        text = { Text("COMPLETED", color = Color.Black, style = MaterialTheme.typography.button) },
                                        onClick = {
                                            it.completed()
                                        },
                                        //modifier = Modifier.navigationBarsPadding()
                                    )
                                }

                                if (userData?.progress?.isQuestCompleted(it) == true) {
                                    ExtendedFloatingActionButton(
                                        icon = {
                                            Icon(
                                                painterResource(id = R.drawable.ic_baseline_assignment_return_24),
                                                contentDescription = null,
                                                tint = Color.Black
                                            )
                                        },
                                        text = { Text("UNDO", color = Color.Black, style = MaterialTheme.typography.button) },
                                        onClick = {
                                            it.undo(true)
                                        },
                                        //modifier = Modifier.navigationBarsPadding()
                                    )
                                }

                                if (it.isLocked(userData)) {
                                    ExtendedFloatingActionButton(
                                        icon = {
                                            Icon(
                                                painterResource(id = R.drawable.ic_baseline_lock_24),
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        },
                                        text = { Text("LOCKED", color = Color.White, style = MaterialTheme.typography.button) },
                                        onClick = {

                                        },
                                        backgroundColor = Color.DarkGray,
                                        //modifier = Modifier.navigationBarsPadding()
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            QuestDetailBottomNav(navController)
                        },
                        isFloatingActionButtonDocked = true,
                        floatingActionButtonPosition = FabPosition.Center
                    ) {
                        if (quest == null) return@Scaffold
                        NavHost(navController = navController, startDestination = BottomNavigationScreens.You.route) {
                            composable("You") { _ ->
                                LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = it.calculateBottomPadding() + 64.dp)) {
                                    item {
                                        DetailCardTop(quest!!, questExtra)
                                    }
                                    objectiveTypes?.forEach { entry ->
                                        val type = entry.key
                                        val objectives = entry.value
                                        if (type == null) return@forEach
                                        item {
                                            ObjectiveCategoryCard(type = valueOf(type.uppercase()), objectives, questExtra, userData)
                                        }
                                    }
                                    if (quest!!.requirement?.quests?.isNotEmpty() == true) {
                                        val preQuests = quest!!.requirement?.quests?.flatMap { it ?: emptyList() }
                                        preQuests?.let {
                                            item {
                                                PreQuestCard(it, userData)
                                            }
                                        }
                                    }
                                    val postQuests = quests?.filter { it.requiredQuestsList()?.contains(quest?.id?.toInt()) == true }
                                    postQuests?.let {
                                        if (it.isNotEmpty()) {
                                            item {
                                                PostQuestCard(list = it.map { it.id.toInt() }, userData = userData)
                                            }
                                        }
                                    }
                                    if (questExtra?.guideImages?.isNullOrEmpty() == false) {
                                        item {
                                            GuideImages(questExtra)
                                        }
                                    }
                                }
                            }
                            composable("Team") { _ ->
                                if (teamData == null) {
                                    LoadingItem()
                                } else if (teamData?.isEmpty() == true) {
                                    EmptyText(text = "No teams.")
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(
                                            top = 4.dp,
                                            bottom = it.calculateBottomPadding() + 64.dp
                                        )
                                    ) {
                                        teamData?.forEach {
                                            Timber.d(it.toString())
                                            item {
                                                TeamCard(it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @Composable
    private fun TeamCard(team: Team) {
        team.let {
            Card(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .fillMaxWidth(),
                backgroundColor = DarkGrey
            ) {
                Column(
                    Modifier.padding(top = 16.dp, start = 0.dp, end = 0.dp, bottom = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        Text(text = it.name ?: "", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Divider(color = DividerDark)

                    it.members?.forEach {
                        TeamMemberItem(id = it.key, it.value)
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TeamMemberItem(id: String, value: Team.MemberSettings) {
        var teamMember by remember { mutableStateOf<FSUser?>(null) }
        Firebase.firestore.collection("users").document(id).get().addOnSuccessListener {
            teamMember = it.toObject<FSUser>()
        }

        teamMember?.let {
            Row(
                Modifier
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Rectangle(color = value.getColorM(), modifier = Modifier.fillMaxHeight())
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${it.getUsername()}${
                            if (id == uid()) {
                                " (You)"
                            } else ""
                        }", fontWeight = if (id == uid()) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    quest?.let {
                        if (teamMember?.progress?.isQuestCompleted(quest!!) == true) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_check_circle_outline_24),
                                contentDescription = "",
                                Modifier.size(16.dp),
                                tint = Green400
                            )
                        }
                        if (quest?.isLocked(teamMember) == true) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_lock_24),
                                contentDescription = "",
                                Modifier.size(16.dp),
                                tint = Red400
                            )
                        } else if (quest?.isAvailable(teamMember) == true) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_lock_open_24),
                                contentDescription = "",
                                Modifier.size(16.dp),
                                tint = Amber500
                            )
                        }
                    }
                }
            }

        }
    }

    @Composable
    fun Rectangle(
        color: Color,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .width(2.dp)
                .clip(RectangleShape)
                .background(color)
        )
    }

    @Composable
    fun GuideImages(questExtra: QuestExtra.QuestExtraItem?) {
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
                            painter = painterResource(id = R.drawable.ic_baseline_image_search_24),
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = White
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "GUIDE IMAGES",
                            style = MaterialTheme.typography.caption,
                            color = White
                        )
                    }
                }
                LazyRow(
                    Modifier
                        .padding(top = 0.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    questExtra?.guideImages?.forEachIndexed { index, media ->
                        item {
                            Image(
                                painter = fadeImagePainter(media),
                                contentDescription = "",
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(150.dp)
                                    .padding(horizontal = 4.dp)
                                    .border(1.dp, BorderColor)
                                    .clickable {
                                        StfalconImageViewer
                                            .Builder(
                                                this@QuestDetailActivity,
                                                questExtra.guideImages
                                            ) { view, image ->
                                                Glide
                                                    .with(view)
                                                    .load(image)
                                                    .into(view)
                                            }
                                            .withStartPosition(index)
                                            .withHiddenStatusBar(false)
                                            .show()
                                    },
                                contentScale = ContentScale.FillHeight
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PreQuestCard(
        list: List<Int?>,
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
                            painter = painterResource(id = R.drawable.ic_baseline_assignment_turned_in_24),
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = White
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "PREREQUISITE QUESTS",
                            style = MaterialTheme.typography.caption,
                            color = White
                        )
                    }
                }
                list.forEach {
                    val quest by tarkovRepo.getQuestByID(it.toString()).collectAsState(initial = null)
                    quest?.let { quest ->
                        SmallQuestItem(quest, userData)
                    }
                }
            }
        }
    }

    @Composable
    fun PostQuestCard(
        list: List<Int?>,
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
                            painter = painterResource(id = R.drawable.ic_baseline_lock_24),
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = White
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "QUESTS LOCKED BEHIND",
                            style = MaterialTheme.typography.caption,
                            color = White
                        )
                    }
                }
                list.forEach {
                    val quest by tarkovRepo.getQuestByID(it.toString()).collectAsState(initial = null)
                    quest?.let { quest ->
                        SmallQuestItem(quest, userData)
                    }
                }
            }
        }
    }

    @Composable
    fun SmallQuestItem(quest: Quest, userData: FSUser?) {

        val isCompleted = userData?.progress?.isQuestCompleted(quest) == true

        Row(
            Modifier
                .clickable {
                    openActivity(this::class.java) {
                        putString("questID", quest.id)
                    }
                }
                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .padding(horizontal = 0.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = quest.title ?: "",
                    style = MaterialTheme.typography.h6,
                    fontSize = 15.sp
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = if (!isCompleted) "Unlocks at Level ${quest.requirement?.level}" else "✓ Completed",
                        style = MaterialTheme.typography.caption,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    @Composable
    fun DetailCardTop(quest: Quest, questExtra: QuestExtra.QuestExtraItem?) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier.padding(end = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = quest.trader().icon),
                        contentDescription = "",
                        modifier = Modifier.size(80.dp)
                    )
                    Column(
                        Modifier
                            .padding(start = 16.dp)
                            .height(80.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = quest.trader().id, style = MaterialTheme.typography.h6)
                        Text(text = "${if(questExtra?.nokappa == true) "Not " else ""}Needed for Kappa", style = MaterialTheme.typography.body2)
                    }
                    Column(
                        Modifier.padding(start = 16.dp, top = 16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (quest.exp != null && quest.exp!! > 0) {
                            Text(text = "+${quest.exp} EXP", style = MaterialTheme.typography.overline)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ObjectiveCategoryCard(
        type: Types,
        objectives: List<Quest.QuestObjective>,
        questExtra: QuestExtra.QuestExtraItem?,
        userData: FSUser?
    ) {

        val isAllObjectivesCompleted = objectives.all {
            userData?.progress?.isQuestObjectiveCompleted(it) == true
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
            border = BorderStroke(width = 1.dp, color = if (isAllObjectivesCompleted) Green400 else Color.Transparent)
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
                    ObjectiveItem(objective, type, questExtra, userData)
                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun ObjectiveItem(
        objective: Quest.QuestObjective,
        type: Types,
        questExtra: QuestExtra.QuestExtraItem?,
        userData: FSUser?
    ) {
        var text by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        scope.launch {
            text = questViewModel.getObjectiveText(objective)
        }

        val subtitle = questExtra?.objectives?.find { it?.id == objective.id?.toInt() }?.with?.joinToString(", ")

        when (type) {
            KILL, PICKUP, PLACE -> {
                ObjectiveItemBasic(
                    title = text,
                    subtitle = subtitle,
                    icon = Maps.values().find { it.int == objective.location?.toInt() }?.icon,
                    userData = userData,
                    objective = objective
                )
            }
            COLLECT, FIND, KEY, BUILD -> {
                val item: Item? by tarkovRepo.getItemByID(objective.target?.first() ?: "").collectAsState(initial = null)
                if (item != null)
                    ObjectiveItemPricing(objective = objective, pricing = item?.pricing)
            }
            else -> ObjectiveItemBasic(
                title = text,
                subtitle = subtitle,
                icon = Maps.values().find { it.int == objective.location?.toInt() }?.icon,
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
        objective: Quest.QuestObjective
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
                .clickable {
                    quest?.let {
                        userData?.toggleObjective(it, objective)
                    }
                }
                .padding(end = 16.dp)
                .defaultMinSize(minHeight = 18.dp)
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
                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                        .height(36.dp)

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
        val hasKey = fsUser.value?.keys?.containsKey(pricing?.id) == true

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
                Box(
                    modifier = Modifier.size(38.dp)
                ) {
                    Image(
                        rememberImagePainter(pricing?.getCleanIcon()),
                        contentDescription = null,
                        modifier = Modifier
                            .width(38.dp)
                            .height(38.dp)
                            .border((0.25).dp, color = if (hasKey) Green400 else BorderColor)
                    )
                    if (hasKey || objective.type == "find") {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_check_circle_outline_24),
                            null,
                            tint = if (hasKey) Green400 else if (objective.type == "find") Amber500 else Color.Transparent,
                            modifier = Modifier
                                .size(12.dp)
                                .padding(bottom = 2.dp, end = 2.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
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

    sealed class BottomNavigationScreens(
        val route: String,
        val resourceId: String,
        val icon: ImageVector? = null,
        @DrawableRes val iconDrawable: Int? = null
    ) {
        object You : BottomNavigationScreens("You", "You", null, R.drawable.ic_baseline_person_24)

        object Team : BottomNavigationScreens("Team", "Team", null, R.drawable.ic_groups_white_24dp)
    }

    @Composable
    private fun QuestDetailBottomNav(
        navController: NavController
    ) {
        val items = listOf(
            BottomNavigationScreens.You,
            BottomNavigationScreens.Team
        )

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F),
            modifier = Modifier.navigationBarsPadding()
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEachIndexed { i, item ->
                if (i == 1) {
                    BottomNavigationItem(selected = false, onClick = {}, icon = {}, enabled = false)
                }
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
                    enabled = true
                )
            }
        }
    }
}