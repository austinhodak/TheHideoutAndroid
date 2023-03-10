package com.austinhodak.thehideout.ui.legacy

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.annotation.ExperimentalCoilApi
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.austinhodak.tarkovapi.MenuDrawerLayout
import com.austinhodak.tarkovapi.ServerStatusQuery
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.ServerStatus
import com.austinhodak.tarkovapi.models.toObj
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.features.premium.PremiumPusherActivity
import com.austinhodak.thehideout.features.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.features.map.MapsActivity
import com.austinhodak.thehideout.extras
import com.austinhodak.thehideout.features.map.StaticMapsActivity
import com.austinhodak.thehideout.features.ocr.ItemScannerActivity
import com.austinhodak.thehideout.settings.SettingsActivity
import com.austinhodak.thehideout.features.status.ServerStatusActivity
import com.austinhodak.thehideout.utils.launchPremiumPusher
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.badgeText
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.util.*
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import kotlinx.coroutines.*

val weaponCategories = mutableListOf(
    Triple(R.string.assault_rifles, 301, "assaultRifle"),
    Triple(R.string.assault_carbines, 302, "assaultCarbine"),
    Triple(R.string.grenades, 309, "grenade"),
    Triple(R.string.lmg, 303, "machinegun"),
    Triple(R.string.marksman_rifles, 306, "marksmanRifle"),
    Triple(R.string.melee, 310, "melee"),
    Triple(R.string.pistols, 308, "pistol"),
    Triple(R.string.sniper_rifles, 307, "sniperRifle"),
    Triple(R.string.shotguns, 305, "shotgun"),
    Triple(R.string.smgs, 304, "smg")
)

@ExperimentalCoilApi
@ExperimentalMaterialApi
class Drawer(context: Context, lifecycleOwner: LifecycleOwner) :
    MaterialDrawerSliderView(context, null) {

    private val benderFont = ResourcesCompat.getFont(context, R.font.bender)
    private val benderBoldFont = ResourcesCompat.getFont(context, R.font.bender_bold)

    private val drawerBosses = PrimaryDrawerItem().apply {
        tag = "bosses"; identifier = 117; nameText = "Bosses"; iconRes = R.drawable.icons8_crown_96
        isIconTinted = true; typeface = benderFont
    }

    private val drawerNeededItemsGrid = PrimaryDrawerItem().apply {
        tag = "neededGrid"; identifier = 116; nameText = context.getString(R.string.needed_items); iconRes =
        R.drawable.ic_baseline_grid_view_24; isIconTinted = true; typeface =
        benderFont;
        badgeText = "Beta";
        badgeStyle = BadgeStyle().apply { paddingLeftRight = DimenHolder.fromDp(4); textColor = ColorHolder.fromColorRes(R.color.md_white_1000); color = ColorHolder.fromColorRes(R.color.md_orange_700) }
    }
    private val drawerAmmo = PrimaryDrawerItem().apply {
        tag = "ammunition/Caliber762x35"; identifier = 101; iconRes =
        R.drawable.icons8_ammo_100; isIconTinted =
        true; typeface = benderFont; nameRes = R.string.ammunition
    }
    private val drawerKeys = PrimaryDrawerItem().apply {
        tag = "keys"; identifier = 104; nameRes = R.string.keys; iconRes =
        R.drawable.icons8_key_100; isIconTinted = true; typeface = benderFont
    }
    private val drawerMedical = PrimaryDrawerItem().apply {
        tag = "medical"; identifier = 105; nameRes = R.string.medical; iconRes =
        R.drawable.icons8_syringe_100; isIconTinted = true; typeface = benderFont
    }
    private val drawerFleaMarket = PrimaryDrawerItem().apply {
        tag = "flea"; identifier = 107; nameRes = R.string.flea_market; iconRes =
        R.drawable.ic_baseline_storefront_24; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerHideout = PrimaryDrawerItem().apply {
        tag = "hideout"; identifier = 108; nameRes = R.string.hideout; iconRes =
        R.drawable.icons8_tent_96; isIconTinted = true; typeface = benderFont
    }
    private val drawerQuests = PrimaryDrawerItem().apply {
        tag = "quests"; identifier = 109; nameRes = R.string.quests; iconRes =
        R.drawable.ic_baseline_assignment_24; isIconTinted = true; typeface =
        benderFont
    }

    private val interactiveMaps = SecondaryDrawerItem().apply {
        level = 2; iconRes = R.drawable.ic_baseline_touch_app_24; identifier = 701
        nameText = context.getString(R.string.interactive); typeface = benderFont; tag = "map_interactive"; isIconTinted = true; isSelectable = false;
    }

    private val staticMaps = SecondaryDrawerItem().apply {
        level = 2; iconRes = R.drawable.ic_baseline_image_24; identifier = 702
        nameText = context.getString(R.string.n_static); typeface = benderFont; tag = "map_static"; isSelectable = false; isIconTinted = true
    }

    private val mapsInfo = SecondaryDrawerItem().apply {
        level = 2; iconRes = R.drawable.ic_baseline_info_24; identifier = 703
        nameText = "Information"; typeface = benderFont; tag = "map_info"; isIconTinted = true;
        badgeText = "New";
        badgeStyle = BadgeStyle().apply { paddingLeftRight = DimenHolder.fromDp(4); textColor = ColorHolder.fromColorRes(R.color.md_white_1000); color = ColorHolder.fromColorRes(R.color.md_green_500); }
    }

    private val drawerMaps = ExpandableDrawerItem().apply {
        tag = "map_selector"; identifier = 110; nameText = context.getString(R.string.maps); iconRes =
        R.drawable.ic_baseline_map_24; isIconTinted = true; typeface = benderFont; isSelectable = false;
        subItems = mutableListOf(
            mapsInfo,
            interactiveMaps,
            staticMaps
        )
    }

    private val drawerDamageSimulator = PrimaryDrawerItem().apply {
        tag = "activity:sim"; identifier = 111; nameRes = R.string.simulator; iconRes =
        R.drawable.icons8_dog_tag_96; isIconTinted =
        true; typeface = benderFont; isSelectable = false
    }
    private val drawerSkills = PrimaryDrawerItem().apply {
        tag = "skills"; identifier = 113; nameText = context.getString(R.string.skills); iconRes =
        R.drawable.icons8_development_skill_96; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerWeaponMods = PrimaryDrawerItem().apply {
        tag = "weaponmods"; identifier = 114; nameText = context.getString(R.string.weapon_mods); iconRes =
        R.drawable.icons8_assault_rifle_mod_96; isIconTinted =
        true; typeface = benderFont
    }

    private val drawerWeaponLoadouts = PrimaryDrawerItem().apply {
        tag = "weaponloadouts"; identifier = 115; nameText = context.getString(R.string.weapon_loadouts); iconRes =
        R.drawable.icons8_assault_rifle_custom; isIconTinted =
        true; typeface = benderFont
    }

    private val drawerProvisions = PrimaryDrawerItem().apply {
        tag = "food"; identifier = 112; nameText = context.getString(R.string.provisions); iconRes =
        R.drawable.ic_baseline_fastfood_24; isIconTinted = true; typeface =
        benderFont
    }

    private val drawerArmor = SecondaryDrawerItem().apply {
        tag = "gear/Armor"; level = 2; identifier = 201; nameRes = R.string.armor; iconRes =
        R.drawable.icons8_bulletproof_vest_100; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerBackpacks = SecondaryDrawerItem().apply {
        tag = "gear/Backpacks"; level = 2; identifier = 202; nameRes = R.string.backpacks; iconRes =
        R.drawable.icons8_rucksack_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerChestRigs = SecondaryDrawerItem().apply {
        tag = "gear/Rigs"; level = 2; identifier = 203; nameText = context.getString(R.string.chest_rigs); iconRes =
        R.drawable.icons8_vest_100; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerEyewear = SecondaryDrawerItem().apply {
        tag = "gear/Eyewear"; level = 2; identifier = 204; nameText = context.getString(R.string.eyewear); iconRes =
        R.drawable.icons8_sun_glasses_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerFaceCover = SecondaryDrawerItem().apply {
        tag = "gear/Facecover"; level = 2; identifier = 205; nameText = context.getString(R.string.face_coverings); iconRes =
        R.drawable.icons8_camo_cream_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerHeadsets = SecondaryDrawerItem().apply {
        tag = "gear/Headsets"; level = 2; identifier = 206; nameText = context.getString(R.string.headsets); iconRes =
        R.drawable.icons8_headset_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerHeadwear = SecondaryDrawerItem().apply {
        tag = "gear/Headwear"; level = 2; identifier = 207; nameText = context.getString(R.string.headwear); iconRes =
        R.drawable.icons8_helmet_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerTacticalClothing = SecondaryDrawerItem().apply {
        tag = "gear/Clothing"; level = 2; identifier = 208; nameText =
        context.getString(R.string.tactical_clothing); iconRes = R.drawable.icons8_coat_96; isIconTinted =
        true; typeface = benderFont; isEnabled = false
    }

    private val drawerGear = ExpandableDrawerItem().apply {
        nameText = context.getString(R.string.gear)
        iconRes = R.drawable.icons8_bulletproof_vest_100; isIconTinted = true
        typeface = benderFont
        isSelectable = false
        subItems = mutableListOf(
            drawerArmor,
            drawerBackpacks,
            drawerChestRigs,
            drawerEyewear,
            drawerFaceCover,
            drawerHeadsets,
            drawerHeadwear
            //drawerTacticalClothing
        )
    }

    private val drawerWeaponExpandable = ExpandableDrawerItem().apply {
        nameRes = R.string.weapons; iconRes = R.drawable.icons8_assault_rifle_100; isIconTinted =
        true; typeface = benderFont; isSelectable = false
        subItems = weaponCategories.map {
            SecondaryDrawerItem().apply {
                level = 1; iconRes = R.drawable.ic_blank; identifier =
                it.second.toLong(); nameRes = it.first; typeface = benderFont; tag =
                "weapons/${it.third}"
            }
        }.toMutableList()
    }

    private val drawerDivider = DividerDrawerItem()

    private val drawerCurrencyConverter = SecondaryDrawerItem().apply {
        tag = "currency_converter"; level = 2; identifier = 402; nameText =
        context.getString(R.string.currency_converter); iconRes = R.drawable.icons8_currency_exchange_96; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerBitcoin = SecondaryDrawerItem().apply {
        tag = "bitcoin"; level = 2; identifier = 401; nameText = context.getString(R.string.bitcoin_price); iconRes =
        R.drawable.icons8_bitcoin_96; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerSensitivity = SecondaryDrawerItem().apply {
        tag = "sensitivity"; level = 2; identifier = 403; nameText =
        context.getString(R.string.sens_calculator); iconRes = R.drawable.ic_baseline_calculate_24; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerRestockTimers = SecondaryDrawerItem().apply {
        tag = "restock"; level = 2; identifier = 404; nameText =
        context.getString(R.string.trader_restock_timers); iconRes = R.drawable.ic_baseline_timer_24; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerPriceAlerts = SecondaryDrawerItem().apply {
        tag = "price_alerts"; level = 2; identifier = 405; nameText =
        context.getString(R.string.price_alerts); iconRes = R.drawable.ic_baseline_notifications_active_24; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerServerPings = SecondaryDrawerItem().apply {
        tag = "server_pings"; level = 2; identifier = 406; nameText =
        context.getString(R.string.server_pings); iconRes = R.drawable.ic_baseline_network_ping_24; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerItemScanner = SecondaryDrawerItem().apply {
        tag = "item_scanner"; level = 2; identifier = 407; nameText =
        context.getString(R.string.item_scanner); iconRes = R.drawable.ic_baseline_document_scanner_24; isIconTinted =
        true; typeface = benderFont; isEnabled = true; badgeText = "Alpha";
        badgeStyle = BadgeStyle().apply { paddingLeftRight = DimenHolder.fromDp(4); textColor = ColorHolder.fromColorRes(R.color.md_white_1000); color = ColorHolder.fromColorRes(R.color.md_red_700) }
        isSelectable = false
    }

    private val drawerNews = PrimaryDrawerItem().apply {
        tag = "news"; identifier = 601; nameText = context.getString(R.string.news); iconRes = R.drawable.ic_baseline_newspaper_24; isIconTinted = true; typeface = benderFont;
    }

    private val drawerServerStatus = PrimaryDrawerItem().apply {
        tag = "server_status"; identifier = 602; nameText = context.getString(R.string.server_status); iconRes = R.drawable.icons8_server_96; isIconTinted = true; typeface = benderFont; isSelectable = false
    }

    private val drawerExtraTools = ExpandableDrawerItem().apply {
        nameText = context.getString(R.string.tools)
        iconRes = R.drawable.icons8_wrench_96; isIconTinted = true
        typeface = benderFont
        isSelectable = false
        subItems = mutableListOf(
            drawerBitcoin,
            drawerCurrencyConverter,
            drawerItemScanner,
            drawerPriceAlerts,
            drawerSensitivity,
            drawerServerPings,
            drawerRestockTimers
        )
    }

    private val drawerSettings = SecondaryDrawerItem().apply {
        tag = "settings"
        nameText = context.getString(R.string.settings_profile); iconRes =
        R.drawable.ic_baseline_settings_24; isIconTinted = true; isSelectable = false; isEnabled =
        true
        typeface = benderFont
    }

    private val drawerTraderPrapor = SecondaryDrawerItem().apply {
        tag = "trader/prapor"
        level = 2
        nameText = "Prapor"
        iconRes = R.drawable.prapor_portrait
        typeface = benderFont
        identifier = 501
    }

    private val drawerTraderTherapist = SecondaryDrawerItem().apply {
        tag = "trader/therapist"
        level = 2
        nameText = "Therapist"
        iconRes = R.drawable.therapist_portrait
        typeface = benderFont
        identifier = 502
    }

    private val drawerTraderSkier = SecondaryDrawerItem().apply {
        tag = "trader/skier"
        level = 2
        nameText = "Skier"
        iconRes = R.drawable.skier_portrait
        typeface = benderFont
        identifier = 503
    }

    private val drawerTraderPeacekeeper = SecondaryDrawerItem().apply {
        tag = "trader/peacekeeper"
        level = 2
        nameText = "Peacekeeper"
        iconRes = R.drawable.peacekeeper_portrait
        typeface = benderFont
        identifier = 504
    }

    private val drawerTraderMechanic = SecondaryDrawerItem().apply {
        tag = "trader/mechanic"
        level = 2
        nameText = "Mechanic"
        iconRes = R.drawable.mechanic_portrait
        typeface = benderFont
        identifier = 505
    }

    private val drawerTraderRagman = SecondaryDrawerItem().apply {
        tag = "trader/ragman"
        level = 2
        nameText = "Ragman"
        iconRes = R.drawable.ragman_portrait
        typeface = benderFont
        identifier = 506
    }

    private val drawerTraderJaeger = SecondaryDrawerItem().apply {
        tag = "trader/jaeger"
        level = 2
        nameText = "Jaeger"
        iconRes = R.drawable.jaeger_portrait
        typeface = benderFont
        identifier = 507
    }

    private val drawerTraders = ExpandableDrawerItem().apply {
        nameText = context.getString(R.string.traders)
        iconRes = R.drawable.ic_groups_white_24dp
        isIconTinted = true
        typeface = benderFont
        subItems = mutableListOf(
            drawerTraderPrapor,
            drawerTraderTherapist,
            drawerTraderSkier,
            drawerTraderPeacekeeper,
            drawerTraderMechanic,
            drawerTraderRagman,
            drawerTraderJaeger
        )
        isSelectable = false
    }



    init {

        UserSettingsModel.menuDrawerLayout.observe(lifecycleOwner.lifecycleScope) {  layoutSetting ->
            removeAllItems()

            when (layoutSetting) {
                MenuDrawerLayout.ORIGINAL -> {
                    itemAdapter.add(
                        drawerAmmo,
                        drawerGear,
                        drawerKeys,
                        drawerMedical,
                        drawerProvisions,
                        drawerSkills,
                        drawerWeaponExpandable,
                        drawerWeaponLoadouts,
                        drawerWeaponMods,
                        drawerDivider,
                        drawerFleaMarket,
                        drawerHideout,
                        drawerMaps,
                        drawerNeededItemsGrid,
                        drawerQuests,
                        drawerDamageSimulator,
                        drawerExtraTools,
                        drawerTraders,
                        drawerDivider,
                        drawerNews,
                        drawerServerStatus
                    )
                }
                MenuDrawerLayout.ALPHABETICAL -> {
                    itemAdapter.add(
                        drawerAmmo,
                        //drawerDivider,
                        //drawerDivider,
                        drawerFleaMarket,
                        drawerGear,
                        drawerHideout,
                        drawerKeys,
                        drawerMaps,
                        drawerMedical,
                        drawerNeededItemsGrid,
                        drawerNews,
                        drawerProvisions,
                        drawerQuests,
                        drawerServerStatus,
                        drawerSkills,
                        drawerDamageSimulator,
                        drawerExtraTools,
                        drawerTraders,
                        drawerWeaponExpandable,
                        drawerWeaponLoadouts,
                        drawerWeaponMods,
                    )
                }
                MenuDrawerLayout.FLIPPED -> {
                    itemAdapter.add(
                        drawerFleaMarket,
                        drawerHideout,
                        drawerMaps,
                        drawerNeededItemsGrid,
                        drawerQuests,
                        drawerDamageSimulator,
                        drawerExtraTools,
                        drawerTraders,
                        drawerDivider,
                        drawerAmmo,
                        drawerGear,
                        drawerKeys,
                        drawerMedical,
                        drawerProvisions,
                        drawerSkills,
                        drawerWeaponExpandable,
                        drawerWeaponLoadouts,
                        drawerWeaponMods,
                        drawerDivider,
                        drawerNews,
                        drawerServerStatus
                    )
                }
            }
        }


        addStickyFooterItem(drawerSettings)

        recyclerView.isVerticalFadingEdgeEnabled = false
        recyclerView.isVerticalScrollBarEnabled = false
        expandableExtension.isOnlyOneExpandedItem = true
    }

}

@SuppressLint("CheckResult")
@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun MainDrawer(
    navViewModel: NavViewModel,
    lifecycleOwner: LifecycleOwner,
    context: Context,
    apolloClient: ApolloClient
) {

    val selectedDrawerItem by navViewModel.selectedDrawerItem.observeAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val benderFont = ResourcesCompat.getFont(context, R.font.bender)
    val benderFontMedium = ResourcesCompat.getFont(context, R.font.bender_bold)


    var playerLevel by remember {
        mutableStateOf(UserSettingsModel.playerLevel.value)
    }

    UserSettingsModel.playerLevel.observe(scope) {
        playerLevel = it
    }

    val drawerLogin = SecondaryDrawerItem().apply {
        tag = "login"
        nameText = stringResource(R.string.sign_in_up)
        iconRes = R.drawable.icons8_lock_96_color
        isIconTinted = false
        isSelectable = false
        identifier = 999
        typeface = benderFont
    }

    var status: ServerStatus? by remember { mutableStateOf(null) }
    val isPremium = UserSettingsModel.isPremiumUser.value
    var hideBanner: Boolean by remember { mutableStateOf(UserSettingsModel.hidePremiumBanner.value) }

    UserSettingsModel.hidePremiumBanner.observe(scope) {
        hideBanner = it
    }

    var isTimersRunning = false

    LaunchedEffect("drawer") {
        try {
            status = apolloClient.query(ServerStatusQuery()).execute().data?.status?.toObj()
        } catch (e: ApolloNetworkException) {
            //Most likely no internet connection.
            e.printStackTrace()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    backgroundColor = Green500,
                    snackbarData = data
                )
            }
        }
    ) { _ ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
        ) {
            Row(
                Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameClockText(Modifier.weight(1f), true, navViewModel)
                FloatingActionButton(onClick = {
                    MaterialDialog(context).show {
                        title(res = R.string.set_level)
                        input(inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED, prefill = playerLevel.toString(), hintRes = R.string.level) { _, text ->
                            scope.launch {
                                UserSettingsModel.playerLevel.update(text.toString().toIntOrNull() ?: 71)
                            }
                        }
                        positiveButton(text = "SAVE")
                    }
                }, modifier = Modifier.size(40.dp), backgroundColor = LightGray) {
                    Text(
                        text = playerLevel.toString(),
                        modifier = Modifier,
                        color = Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                GameClockText(Modifier.weight(1f), false, navViewModel)
            }
            if (!isPremium && !hideBanner) {
                Surface(
                    color = Red400,
                    modifier = Modifier,
                ) {
                    Row(
                        Modifier
                            .clickable {
                                context.launchPremiumPusher()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.go_premium), color = Black, style = MaterialTheme.typography.button, modifier = Modifier.padding(start = 2.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_24), contentDescription = "", tint = Black)
                    }
                }
            }
            Divider(
                modifier = Modifier.background(MaterialTheme.colors.surface),
                color = DividerDark
            )
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val drawer = Drawer(context, lifecycleOwner)
                    drawer.onDrawerItemClickListener = { _, item, _ ->
                        if (item.tag != null) {
                            if (item.isSelectable || item.identifier.toInt() == 999) {
                                navViewModel.drawerItemSelected(item)
                                navViewModel.setDrawerOpen(false)
                            } else if (!item.isSelectable && item.isEnabled) {
                                val route = item.tag as String
                                when {
                                    route == "activity:sim" -> context.openActivity(
                                        CalculatorMainActivity::class.java
                                    )
                                    route == "map_interactive" -> context.openActivity(MapsActivity::class.java)
                                    route == "map_static" -> context.openActivity(StaticMapsActivity::class.java)
                                    route == "settings" -> context.openActivity(SettingsActivity::class.java)
                                    route == "upgrade" -> context.openActivity(PremiumPusherActivity::class.java)
                                    route == "server_status" -> context.openActivity(
                                        ServerStatusActivity::class.java)
                                    route == "item_scanner" -> context.openActivity(
                                        ItemScannerActivity::class.java)
                                    route.contains("https:") -> {
                                        route.openWithCustomTab(context)
                                    }
                                }
                            }
                        }
                        true
                    }

                    drawer
                }, update = { drawer ->
                    val drawerServerStatus = PrimaryDrawerItem().apply {
                        tag = "server_status"
                        identifier = 602
                        nameRes = R.string.server_status
                        iconRes = R.drawable.icons8_server_96
                        isIconTinted = true
                        typeface = benderFont
                        isSelectable = false
                        badgeText = status?.getBadgeText() ?: ""
                        badgeStyle = BadgeStyle().apply {
                            color = ColorHolder.fromColor(status?.currentStatusColor()?.toArgb() ?: 0x00000000)
                            typeface = benderFont;
                            paddingLeftRight = DimenHolder.fromDp(4);
                        }
                    }
                    if (status != null && !status?.getBadgeText().isNullOrBlank()) {
                        drawer.updateItem(
                            drawerServerStatus
                        )
                    }

                    if (selectedDrawerItem != null) {
                        drawer.setSelection(selectedDrawerItem!!.identifier, false)
                    } else {
                        drawer.getDrawerItem(extras.openingPage)?.let {
                            navViewModel.drawerItemSelected(it)
                            drawer.setSelection(it.identifier, false)
                        }
                    }

                    navViewModel.selectedDrawerItemIdentifier.observe(lifecycleOwner) {
                        if (it != null) {
                            drawer.setSelectionAtPosition(-1, false)
                            drawer.setSelection(it.first, false)
                            navViewModel.selectedDrawerItemIdentifier.value = null
                        }
                    }

                    Firebase.auth.addAuthStateListener {
                        val isLoggedIn =
                            it.currentUser != null && it.currentUser?.isAnonymous == false
                        if (!isLoggedIn) {
                            try {
                                drawer.removeStickyFooterItemAtPosition(1)
                            } catch (e: Exception) {

                            }
                            drawer.addStickyFooterItemAtPosition(drawerLogin, 1)
                        } else {
                            try {
                                drawer.removeStickyFooterItemAtPosition(1)
                            } catch (e: Exception) {

                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GameClockText(
    modifier: Modifier = Modifier,
    left: Boolean,
    navViewModel: NavViewModel
) {
    val text by if (left) navViewModel.timeLeft.observeAsState("00:00:00") else navViewModel.timeRight.observeAsState(
        "00:00:00"
    )

    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center
    )
}