package com.austinhodak.thehideout.weapons.mods

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.DataRow
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.FleaBottomNav
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.utils.*
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

        setContent {
            HideoutTheme {
                val allMods by modRepo.getAllMods().collectAsState(initial = null)
                val allItems by tarkovRepo.getAllItemsSlots("%${modID}%").collectAsState(initial = null)
                val mod by modRepo.getModByID(modID).collectAsState(initial = null)

                val scope = rememberCoroutineScope()

                var selectedNavItem by remember { mutableStateOf(0) }

                val items = listOf(
                    NavItem("Stats", R.drawable.ic_baseline_bar_chart_24),
                    NavItem("Parent Mods", R.drawable.icons8_scroll_up_96),
                    NavItem("Child Mods", R.drawable.icons8_scroll_down_96),
                )

                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = mod?.Name ?: "Loading",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.h6,
                                            maxLines = 1,
                                            fontSize = 18.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "(${mod?.ShortName ?: ""})",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.caption,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
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
                    },
                    bottomBar = {
                        BottomNav(selected = selectedNavItem, items) { selectedNavItem = it }
                    }
                ) {
                    Crossfade(targetState = selectedNavItem, modifier = Modifier.padding(it)) {
                        when (it) {
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
                                        parents?.groupBy { it.itemType }?.forEach { (s, list) ->
                                            item {
                                                ModListCard(mods = list.sortedBy { it.Name }, s?.name.toString())
                                            }
                                        }

                                        if (parents?.isNullOrEmpty() == true) {
                                            item {
                                                EmptyText(text = "No Parent Mods")
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
                                            } else {
                                                item {
                                                    EmptyText(text = "No Child Mods", modifier = Modifier.padding(top = 16.dp))
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
    fun BottomNav(
        selected: Int,
        items: List<NavItem>,
        onItemSelected: (Int) -> Unit
    ) {

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = { Icon(painter = painterResource(id = item.icon), contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text(item.title) },
                    selected = selected == index,
                    onClick = { onItemSelected(index) },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = if (item.enabled == true) Color(0x99FFFFFF) else Color(0x33FFFFFF),
                    enabled = item.enabled ?: true,
                )
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

    data class NavItem(
        val title: String,
        @DrawableRes val icon: Int,
        val enabled: Boolean? = true
    )

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
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
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
                Column(
                    Modifier.width(IntrinsicSize.Min),
                ) {
                    StatItem(value = item.Recoil, title = "REC", item.Recoil?.getColor(true, MaterialTheme.colors.onSurface))
                    StatItem(value = item.Ergonomics, title = "ERG", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                    StatItem(value = item.Accuracy, title = "ACC", item.Accuracy?.getColor(false, MaterialTheme.colors.onSurface))
                }
            }
        }
    }

    @Composable
    fun StatItem(
        value: Any?,
        title: String,
        color: Color? = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.h6,
                fontSize = 9.sp,
                modifier = Modifier.padding(end = 8.dp),
                color = color ?: MaterialTheme.colors.onSurface,
                textAlign = TextAlign.End
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Light,
                    fontSize = 9.sp,
                    textAlign = TextAlign.End
                )
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
                .fillMaxWidth()
                .clickable {
                    openFleaDetail(mod.id)
                },
            backgroundColor = if (isSystemInDarkTheme()) DarkPrimary else MaterialTheme.colors.primary,
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