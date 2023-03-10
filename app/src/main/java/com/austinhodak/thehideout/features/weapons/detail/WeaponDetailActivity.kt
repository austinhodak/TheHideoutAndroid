package com.austinhodak.thehideout.features.weapons.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImagePainter
import coil.compose.ImagePainter
import coil.compose.rememberAsyncImagePainter
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.features.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.presetList
import com.austinhodak.thehideout.utils.*
import com.austinhodak.thehideout.features.weapons.builder.WeaponBuilderActivity
import com.austinhodak.thehideout.features.weapons.mods.ModDetailActivity
import com.austinhodak.thehideout.features.weapons.viewmodel.WeaponDetailViewModel
import com.austinhodak.thehideout.ui.theme.Bender
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class WeaponDetailActivity : GodActivity() {

    private val weaponViewModel: WeaponDetailViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @Inject
    lateinit var modRepo: ModsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val weaponID = intent.getStringExtra("weaponID") ?: "5926bb2186f7744b1c6c6e60"
        weaponViewModel.getWeapon(weaponID)

        setCrashKeys(
            Pair("weaponID", weaponID)
        )

        setContent {
            HideoutTheme {
                ProvideWindowInsets {
                    val weapon by tarkovRepo.getWeaponByID(weaponID).collectAsState(initial = null)
                    val scaffoldState = rememberScaffoldState()
                    val systemUiController = rememberSystemUiController()

                    val mods by modRepo.getAllMods().collectAsState(initial = null)
                    val presetModIDs = presetList.getPresetsForWeapon(weaponID).flatMap {
                        it.partsWithoutBase().map { it.id }
                    }

                    val allItems by tarkovRepo.getItemByID(presetModIDs).collectAsState(initial = null)

                    var selectedNavItem by remember { mutableStateOf(0) }

                    val items = listOf(
                        ModDetailActivity.NavItem("Stats", R.drawable.ic_baseline_bar_chart_24),
                        ModDetailActivity.NavItem("Mods", R.drawable.icons8_assault_rifle_mod_96),
                        ModDetailActivity.NavItem("Presets", R.drawable.icons8_assault_rifle_custom),
                    )

                    val defaultAmmo by tarkovRepo.getAmmoByID(weapon?.defAmmo ?: "").collectAsState(initial = null)

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            AnimatedContent(
                                targetState = selectedNavItem,
                                transitionSpec = {
                                    fadeIn() with fadeOut()
                                }
                            ) {
                                when (it) {
                                    0 -> {
                                        systemUiController.setStatusBarColor(
                                            Color.Transparent,
                                            darkIcons = false
                                        )
                                        systemUiController.setNavigationBarColor(DarkPrimary)
                                        Column {
                                            Box {
                                                val painter = fadeImagePainter(
                                                    weapon?.getTarkovMarketImageURL()
                                                )
                                                Column {
                                                    Image(
                                                        painter,
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .then(
                                                                if (painter.state is AsyncImagePainter.State
                                                                    .Loading || painter.state is AsyncImagePainter.State.Error
                                                                ) {
                                                                    Modifier.height(0.dp)
                                                                } else {
                                                                    (painter.state as? AsyncImagePainter.State.Success)
                                                                        ?.painter
                                                                        ?.intrinsicSize
                                                                        ?.let { intrinsicSize ->
                                                                            Modifier.aspectRatio(
                                                                                intrinsicSize.width / intrinsicSize.height
                                                                            )
                                                                        } ?: Modifier
                                                                }
                                                            ),
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .offset(y = (-4).dp)
                                                            .background(if (painter.state is AsyncImagePainter.State.Success) Color.Black else DarkPrimary)
                                                            .padding(
                                                                start = 72.dp,
                                                                bottom = 16.dp,
                                                                top = if (painter.state is AsyncImagePainter.State.Success) 0.dp else 56.dp
                                                            )
                                                    ) {
                                                        Text(
                                                            text = weapon?.Name ?: "Loading...",
                                                            color = MaterialTheme.colors.onPrimary,
                                                            style = MaterialTheme.typography.h6,
                                                            maxLines = 1,
                                                            fontSize = 18.sp,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.padding(end = 16.dp)
                                                        )
                                                        Text(
                                                            text = "(${weapon?.ShortName})",
                                                            color = MaterialTheme.colors.onPrimary,
                                                            style = MaterialTheme.typography.caption,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
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
                                                            weapon?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                                        }
                                                    }
                                                )
                                            }

                                            if (weapon == null) {
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
                                    2,
                                    1 -> {
                                        systemUiController.setStatusBarColor(
                                            MaterialTheme.colors.primary,
                                            darkIcons = false
                                        )
                                        TopAppBar(
                                            title = {
                                                Column {
                                                    Text(
                                                        text = weapon?.Name ?: "Loading",
                                                        color = MaterialTheme.colors.onPrimary,
                                                        style = MaterialTheme.typography.h6,
                                                        maxLines = 1,
                                                        fontSize = 18.sp,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "(${weapon?.ShortName ?: ""})",
                                                        color = MaterialTheme.colors.onPrimary,
                                                        style = MaterialTheme.typography.caption,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            },
                                            backgroundColor = if (isSystemInDarkTheme()) DarkPrimary else MaterialTheme.colors.primary,
                                            modifier = Modifier.statusBarsPadding(),
                                            navigationIcon = {
                                                IconButton(onClick = {
                                                    onBackPressed()
                                                }) {
                                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                                }
                                            },
                                            actions = {
                                                OverflowMenu {
                                                    weapon?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                        },
                        bottomBar = {
                            BottomNav(selected = selectedNavItem, items) { selectedNavItem = it }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                this.openActivity(WeaponBuilderActivity::class.java) {
                                    weapon?.let {
                                        putSerializable("weapon", it)
                                    }
                                }
                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_construction_24), contentDescription = "Build", tint = Color.Black)
                            }
                        }
                    ) {
                        Crossfade(targetState = selectedNavItem, modifier = Modifier.padding(it)) {
                            when (it) {
                                0 -> {
                                    LazyColumn(
                                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 64.dp)
                                    ) {
                                        item {
                                            weapon?.let { WeaponDetailCard(weapon = it) }
                                        }
                                        item {
                                            if (weapon != null && defaultAmmo != null) {
                                                AmmoCard(weapon = weapon!!, defaultAmmo = defaultAmmo!!)
                                            }
                                        }
                                        item {
                                            weapon?.pricing?.let {
                                                PricingCard(pricing = it)
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    weapon?.let { it1 -> WeaponModScreen(weapon = it1, mods) }
                                }
                                2 -> {
                                    weapon?.let {
                                        PresetScreen(
                                            weapon = it,
                                            allItems
                                        )
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
    private fun PresetScreen(weapon: Weapon, allItems: List<Item>?) {
        val presets = presetList.getPresetsForWeapon(weapon.id)
        LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            items(presets) { preset ->
                Card(
                    backgroundColor = if (isSystemInDarkTheme()) Color(
                        0xFE1F1F1F
                    ) else MaterialTheme.colors.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 0.dp)
                ) {
                    Column(
                        Modifier.padding(bottom = 8.dp)
                    ) {
                        //Image(painter = fadeImagePainter(url = "https://gamepedia.cursecdn.com/escapefromtarkov_gamepedia/e/ea/${preset.name.replace(" ", "_")}.png"), contentDescription = null, modifier = Modifier.size(100.dp))
                        Row(
                            modifier = Modifier.padding(
                                bottom = 8.dp,
                                top = 16.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (preset.default) {
                                SmallBuyPrice(pricing = weapon.pricing)
                            } else {
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.body1,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(
                                start = 0.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 12.dp
                            )
                        ) {
                            preset.partsWithoutBase().forEach { part ->
                                val item = allItems?.find { it.id == part.id }
                                Row(
                                    modifier = Modifier
                                        .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            openActivity(FleaItemDetail::class.java) {
                                                putString("id", item?.id)
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box {
                                        Image(
                                            fadeImagePainter(
                                                item?.pricing?.getCleanIcon()
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(38.dp)
                                                .height(38.dp)
                                                .border((0.25).dp, color = BorderColor)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.padding(start = 16.dp),
                                    ) {
                                        Text(
                                            text = "${item?.Name}",
                                            style = MaterialTheme.typography.body1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                SmallBuyPrice(pricing = item?.pricing)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        AvgPriceRow(title = "ESTIMATED COST", price = preset.parts.sumOf { part ->
                            val item = allItems?.find { it.id == part.id }?.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles()
                            item ?: 0
                        })
                    }
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
    fun WeaponModScreen(
        weapon: Weapon,
        mods: List<Mod>?
    ) {
        val modIDs = weapon.getAllMods()
        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp)
        ) {
            weapon.Slots?.forEach { slot ->
                val m = slot._props?.filters?.first()?.Filter?.map { it2 -> mods?.find { it.id == it2 } }
                if (m?.isNotEmpty() == true) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            border = if (slot._required == true) BorderStroke(0.25.dp, Red400) else null
                        ) {
                            Column(
                                Modifier.padding(16.dp)
                            ) {
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text(
                                        text = slot.getName(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Light,
                                        fontFamily = Bender,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                m?.forEach {
                                    it?.let {
                                        ModListCardChildMod(item = it, mods)
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
    fun ModListCardChildMod(
        item: Mod,
        mods: List<Mod>?
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
                    rememberAsyncImagePainter(
                        item.pricing?.getCleanIcon()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .border((0.25).dp, color = BorderColor)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "${item.Name}",
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    //SmallBuyPrice(pricing = item.pricing)
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

            /*item.getSlotModIDs()?.let {
                it.forEach { id ->
                    Row(
                        verticalAlignment = CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_subdirectory_arrow_right_24),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp),
                            tint = BorderColor
                        )
                        mods?.find { it.id == id }?.let { it1 -> ModListCardChildMod(item = it1, mods = mods) }
                    }
                }
            }*/
        }
    }

    @Composable
    fun BottomNav(
        selected: Int,
        items: List<ModDetailActivity.NavItem>,
        onItemSelected: (Int) -> Unit
    ) {

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F),
            modifier = Modifier.navigationBarsPadding()
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

    @ExperimentalAnimationApi
    @Composable
    private fun WeaponDetailCard(
        weapon: Weapon,
    ) {
        var visible by remember {
            mutableStateOf(false)
        }
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clickable { visible = !visible },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier.padding(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                }
                Divider(color = DividerDark)
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    DataRow(
                        title = "RECOIL VERTICAL",
                        value = Pair(weapon.RecoilForceUp, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "RECOIL HORIZONTAL",
                        value = Pair(weapon.RecoilForceBack, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "EFFECTIVE DISTANCE",
                        value = Pair(weapon.bEffDist?.roundToInt(), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "ERGONOMICS",
                        value = Pair(weapon.Ergonomics?.roundToInt(), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "RATE OF FIRE",
                        value = Pair(weapon.bFirerate?.roundToInt(), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "SIGHTING RANGE",
                        value = Pair("${weapon.IronSightRange?.roundToInt()}m", MaterialTheme.colors.onSurface)
                    )
                    AnimatedVisibility(visible = visible) {
                        Column(
                            Modifier.padding(top = 4.dp, bottom = 0.dp)
                        ) {
                            Divider(color = DividerDark, modifier = Modifier.padding(bottom = 4.dp))
                            DataRow(
                                title = "DURABILITY",
                                value = Pair(weapon.Durability?.roundToInt(), MaterialTheme.colors.onSurface)
                            )
                            DataRow(
                                title = "WEIGHT",
                                value = Pair("${weapon.Weight} KG", MaterialTheme.colors.onSurface)
                            )
                            DataRow(
                                title = "SIZE",
                                value = Pair("${weapon.Width?.roundToInt()}x${weapon.Height?.roundToInt()}", MaterialTheme.colors.onSurface)
                            )
                            DataRow(
                                title = "FIRING MODES",
                                value = Pair(weapon.weapFireType?.joinToString(", ")?.toUpperCase(Locale.current), MaterialTheme.colors.onSurface)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AmmoCard(
        weapon: Weapon,
        defaultAmmo: Ammo
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                setResult(RESULT_OK, Intent().putExtra("caliber", "ammunition/${weapon.ammoCaliber}"))
                finish()
            }
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "AMMUNITION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    DataRow(
                        title = "CALIBER",
                        value = Pair(getCaliberName(weapon.ammoCaliber), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "DEFAULT AMMO",
                        value = Pair(defaultAmmo.name, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "MUZZLE VELOCITY",
                        value = Pair(defaultAmmo.ballistics?.getMuzzleVelocity(), MaterialTheme.colors.onSurface)
                    )
                }
            }
        }
    }

    @Composable
    private fun DataRow(
        title: String,
        value: Pair<Any?, Color?>?
    ) {
        Row(
            Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.caption
            )
            Text(
                text = value?.first.toString(),
                style = MaterialTheme.typography.subtitle2,
                color = value?.second ?: MaterialTheme.colors.onSurface
            )
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun PricingCard(
        pricing: Pricing
    ) {
        val context = LocalContext.current
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clickable {
                    openFleaDetail(pricing.id)
                },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "PRICING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column {
                    pricing.buyFor?.forEach { item ->
                        DataRow(
                            title = "${item.getTitle().toUpperCase(Locale.current)} ", value = Pair(
                                item.price?.asCurrency(if (item.source == "peacekeeper") "D" else "R"),
                                MaterialTheme.colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}