package com.austinhodak.thehideout.weapons.mods

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.tarkovapi.room.models.getModName
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.ammunition.DataRow
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.utils.asColor
import com.austinhodak.thehideout.utils.getCaliberName
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.weapons.detail.WeaponDetailActivity
import com.bumptech.glide.Glide
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@AndroidEntryPoint
class ModDetailActivity : GodActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @Inject
    lateinit var modRepo: ModsRepo

    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modID = intent.getStringExtra("id") ?: "5ea172e498dacb342978818e"

        val titles = listOf("STATS", "FITS", "ATTACHMENTS")

        setContent {
            HideoutTheme {
                val allMods by modRepo.getAllMods().collectAsState(initial = null)
                val allItems by tarkovRepo.getAllItemsSlots().collectAsState(initial = null)
                val mod by modRepo.getModByID(modID).collectAsState(initial = null)

                var state by remember { mutableStateOf(0) }
                val pagerState = rememberPagerState(pageCount = titles.count())
                val scope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                        AutoSizeText(text = mod?.Name.toString(), textStyle = MaterialTheme.typography.h6)
                                },
                                actions = {
                                    OverflowMenu {
                                        mod?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        onBackPressed()
                                    }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                    }
                                },
                                backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            )
                            TabRow(
                                selectedTabIndex = state,
                                indicator = { tabPositions ->
                                    TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
                                }
                            ) {
                                titles.forEachIndexed { index, s ->
                                    Tab(
                                        text = {
                                            Text(
                                                s,
                                                fontFamily = Bender
                                            )
                                        },
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
                            if (mod == null || allItems == null || allMods == null) {
                                LinearProgressIndicator(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                    backgroundColor = Color.Transparent
                                )
                            }
                        }
                    }
                ) {
                    HorizontalPager(state = pagerState) { page ->
                        when (page) {
                            0 -> {
                                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 64.dp)) {
                                    mod?.let {
                                        item {
                                            ModDetailCard(mod = it)
                                        }
                                    }
                                }
                            }
                            1 -> {
                                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 64.dp)) {
                                    mod?.let {
                                        val parents = allItems?.findParentMods(it)
                                        parents?.let {
                                            item {
                                                ModListCard(mods = it.sortedByDescending { it.itemType }, "FITS ON")
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 64.dp)) {
                                    mod?.let {
                                        val mods = it.getSlotModIDs()?.map { id ->
                                            allMods?.find { it.id == id }
                                        }
                                        mods?.let {
                                            if (it.filterNotNull().isNotEmpty()) {
                                                val categories = it.filterNotNull().groupBy { it.parent }
                                                categories.forEach { (s, list) ->
                                                    item {
                                                        ModListCard(mods = list, title = s?.getModName()?.uppercase() ?: "")
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
        }
    }


    @Composable
    fun ModListCard(
        mods: List<Any>,
        title: String
    ) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) DarkPrimary else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                mods.forEach {
                    if (it is Item)
                        ModListCardChildItem(item = it)
                    if (it is Mod)
                        ModListCardChildMod(item = it)
                }
            }
        }
    }

    @Composable
    fun ModListCardChildItem(
        item: Item
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 0.dp, top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth()
                    .clickable {
                        if (item.itemType == ItemTypes.MOD) {
                            openActivity(ModDetailActivity::class.java) {
                                putString("id", item.id)
                            }
                        } else if (item.itemType == ItemTypes.WEAPON) {
                            openActivity(WeaponDetailActivity::class.java) {
                                putString("weaponID", item.id)
                            }
                        } else {
                            openActivity(FleaItemDetail::class.java) {
                                putString("id", item.id)
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    rememberImagePainter(
                        item.pricing?.iconLink ?: ""
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
                        text = "${item.Name}",
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = "${item.pricing?.getPrice()?.asCurrency()}",
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
    fun ModListCardChildMod(
        item: Mod
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 0.dp, top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth()
                    .clickable {
                        openActivity(ModDetailActivity::class.java) {
                            putString("id", item.id)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    rememberImagePainter(
                        item.pricing?.iconLink ?: ""
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
                        text = "${item.Name}",
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = "${item.pricing?.getPrice()?.asCurrency()}",
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
    fun ModDetailCard(
        mod: Mod
    ) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) DarkPrimary else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberImagePainter(mod.pricing?.iconLink ?: ""),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(52.dp)
                            .height(52.dp)
                            .border((0.25).dp, color = BorderColor)
                            .clickable {
                                StfalconImageViewer
                                    .Builder(this@ModDetailActivity, listOf(mod.pricing?.imageLink)) { view, image ->
                                        Glide
                                            .with(view)
                                            .load(image)
                                            .into(view)
                                    }
                                    .withHiddenStatusBar(false)
                                    .show()
                            }
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Last Price: ${mod.pricing?.getPrice()?.asCurrency()}",
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 16.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = "${
                                    mod.getPricePerSlot().asCurrency()
                                }/slot • ${mod.getTotalSlots()} Slots (${mod.Width}x${mod.Height})",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                        Row {
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Text(
                                    text = "Last 48h: ",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = "${mod.pricing?.changeLast48h}%",
                                    style = MaterialTheme.typography.caption,
                                    color = if (mod.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (mod.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }
                Divider(color = DividerDark)
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    DataRow(title = "RECOIL", value = Pair(mod.Recoil, mod.Recoil?.asColor(true)))
                    DataRow(title = "ERGONOMICS", value = Pair(mod.Ergonomics, mod.Ergonomics?.asColor()))
                    DataRow(title = "ACCURACY", value = Pair("${mod.Accuracy}%", mod.Accuracy?.asColor()))
                    DataRow(title = "VELOCITY", value = Pair("${mod.Velocity}", mod.Velocity?.asColor()))
                    DataRow(title = "WEIGHT", value = Pair("${mod.Weight} KG", null))
                    DataRow(title = "SIZE", value = Pair("${mod.Width}x${mod.Height}", null))
                }
            }
        }
    }
}

@Composable
fun AutoSizeText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    var scaledTextStyle by remember { mutableStateOf(textStyle) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text,
        modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = scaledTextStyle,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                scaledTextStyle =
                    scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        }
    )
}

fun List<Mod>.findParentMods(child: Mod): List<Mod> {
    val childID = child.id
    return filter { mod ->
        val slotIDS = mod.Slots?.flatMap { slot ->
            slot._props?.filters?.flatMap { filter ->
                filter?.Filter!!
            }!!
        }
        return@filter slotIDS?.contains(childID) ?: false
    }
}

@JvmName("findParentModsItem")
fun List<Item>.findParentMods(child: Mod): List<Item> {
    val childID = child.id
    return filter { mod ->
        val slotIDS = mod.Slots?.flatMap { slot ->
            slot._props?.filters?.flatMap { filter ->
                filter?.Filter!!
            }!!
        }
        return@filter slotIDS?.contains(childID) ?: false
    }
}