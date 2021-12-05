package com.austinhodak.thehideout.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import coil.annotation.ExperimentalCoilApi
import com.adapty.Adapty
import com.adapty.listeners.OnPurchaserInfoUpdatedListener
import com.adapty.models.PurchaserInfoModel
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.billing.PremiumPusherActivity
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.map.MapsActivity
import com.austinhodak.thehideout.questPrefs
import com.austinhodak.thehideout.settings.SettingsActivity
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.util.*
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

val weaponCategories = mutableListOf(
    Triple("Assault Rifles", 301, "assaultRifle"),
    Triple("Assault Carbines", 302, "assaultCarbine"),
    Triple("Light Machine Guns", 303, "machinegun"),
    Triple("Marksman Rifles", 306, "marksmanRifle"),
    Triple("Pistols", 308, "pistol"),
    Triple("Sniper Rifles", 307, "sniperRifle"),
    Triple("Shotguns", 305, "shotgun"),
    Triple("Submachine Guns", 304, "smg"),
    //Triple("Grenade Launchers", 309, "grenadeLauncher"),
    //Triple("Melee Weapons", 310, ""),
    //Triple("Throwables", 311, ""),
)

@ExperimentalCoilApi
@ExperimentalMaterialApi
class Drawer(context: Context, attrs: AttributeSet? = null) : MaterialDrawerSliderView(context, attrs) {

    private val benderFont = ResourcesCompat.getFont(context, R.font.bender)

    private val drawerAmmo = PrimaryDrawerItem().apply {
        tag = "ammunition/Caliber762x35"; identifier = 101; nameText = "Ammunition"; iconRes = R.drawable.icons8_ammo_100; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerKeys = PrimaryDrawerItem().apply {
        tag = "keys"; identifier = 104; nameText = "Keys"; iconRes = R.drawable.icons8_key_100; isIconTinted = true; typeface = benderFont
    }
    private val drawerMedical = PrimaryDrawerItem().apply {
        tag = "medical"; identifier = 105; nameText = "Medical"; iconRes = R.drawable.icons8_syringe_100; isIconTinted = true; typeface = benderFont
    }
    private val drawerFleaMarket = PrimaryDrawerItem().apply {
        tag = "flea"; identifier = 107; nameText = "Flea Market"; iconRes = R.drawable.ic_baseline_storefront_24; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerHideout = PrimaryDrawerItem().apply {
        tag = "hideout"; identifier = 108; nameText = "Hideout"; iconRes = R.drawable.icons8_tent_96; isIconTinted = true; typeface = benderFont
    }
    private val drawerQuests = PrimaryDrawerItem().apply {
        tag = "quests"; identifier = 109; nameText = "Quests"; iconRes = R.drawable.ic_baseline_assignment_24; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerMaps = PrimaryDrawerItem().apply {
        tag = "activity:map"; identifier = 110; nameText = "Maps"; iconRes =
        R.drawable.ic_baseline_map_24; isIconTinted = true; typeface = benderFont; isSelectable = false
    }
    private val drawerDamageSimulator = PrimaryDrawerItem().apply {
        tag = "activity:sim"; identifier = 111; nameText = "Tarkov'd Simulator"; iconRes = R.drawable.icons8_dog_tag_96; isIconTinted =
        true; typeface = benderFont; isSelectable = false
    }
    private val drawerSkills = PrimaryDrawerItem().apply {
        tag = "skills"; identifier = 113; nameText = "Skills"; iconRes = R.drawable.icons8_development_skill_96; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerWeaponMods = PrimaryDrawerItem().apply {
        tag = "weaponmods"; identifier = 114; nameText = "Weapon Mods"; iconRes = R.drawable.icons8_assault_rifle_mod_96; isIconTinted =
        true; typeface = benderFont
    }

    private val drawerWeaponLoadouts = PrimaryDrawerItem().apply {
        tag = "weaponloadouts"; identifier = 115; nameText = "Weapon Loadouts"; iconRes = R.drawable.icons8_assault_rifle_custom; isIconTinted =
        true; typeface = benderFont
    }

    private val drawerProvisions = PrimaryDrawerItem().apply {
        tag = "food"; identifier = 112; nameText = "Provisions"; iconRes = R.drawable.ic_baseline_fastfood_24; isIconTinted = true; typeface =
        benderFont
    }

    private val drawerArmor = SecondaryDrawerItem().apply {
        tag = "gear/Armor"; level = 2; identifier = 201; nameText = "Armor"; iconRes = R.drawable.icons8_bulletproof_vest_100; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerBackpacks = SecondaryDrawerItem().apply {
        tag = "gear/Backpacks"; level = 2; identifier = 202; nameText = "Backpacks"; iconRes = R.drawable.icons8_rucksack_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerChestRigs = SecondaryDrawerItem().apply {
        tag = "gear/Rigs"; level = 2; identifier = 203; nameText = "Chest Rigs"; iconRes = R.drawable.icons8_vest_100; isIconTinted = true; typeface =
        benderFont
    }
    private val drawerEyewear = SecondaryDrawerItem().apply {
        tag = "gear/Eyewear"; level = 2; identifier = 204; nameText = "Eyewear"; iconRes = R.drawable.icons8_sun_glasses_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerFaceCover = SecondaryDrawerItem().apply {
        tag = "gear/Facecover"; level = 2; identifier = 205; nameText = "Face Coverings"; iconRes = R.drawable.icons8_camo_cream_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerHeadsets = SecondaryDrawerItem().apply {
        tag = "gear/Headsets"; level = 2; identifier = 206; nameText = "Headsets"; iconRes = R.drawable.icons8_headset_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerHeadwear = SecondaryDrawerItem().apply {
        tag = "gear/Headwear"; level = 2; identifier = 207; nameText = "Headwear"; iconRes = R.drawable.icons8_helmet_96; isIconTinted =
        true; typeface = benderFont
    }
    private val drawerTacticalClothing = SecondaryDrawerItem().apply {
        tag = "gear/Clothing"; level = 2; identifier = 208; nameText = "Tactical Clothing"; iconRes = R.drawable.icons8_coat_96; isIconTinted =
        true; typeface = benderFont; isEnabled = false
    }

    private val drawerGear = ExpandableDrawerItem().apply {
        nameText = "Gear"
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
            drawerHeadwear,
            drawerTacticalClothing
        )
    }

    private val drawerWeaponExpandable = ExpandableDrawerItem().apply {
        nameText = "Weapons"; iconRes = R.drawable.icons8_assault_rifle_100; isIconTinted = true; typeface = benderFont; isSelectable = false
        subItems = weaponCategories.map {
            SecondaryDrawerItem().apply {
                level = 1; iconRes = R.drawable.ic_blank; identifier = it.second.toLong(); nameText = it.first; typeface = benderFont; tag =
                "weapons/${it.third}"
            }
        }.toMutableList()
    }

    private val drawerDivider = DividerDrawerItem()

    private val drawerSectionJoinUs = SectionDrawerItem().apply { nameText = "Join us on" }
    private val drawerJoinUsDiscord =
        PrimaryDrawerItem().apply {
            tag = "https://discord.gg/YQW36z29z6"; nameText = "Discord"; iconRes = R.drawable.icons8_discord_96; isIconTinted = true; typeface = benderFont; isSelectable = false
        }
    private val drawerJoinUsTwitch =
        PrimaryDrawerItem().apply {
            tag = "https://www.twitch.tv/theeeelegend"; nameText = "Twitch"; iconRes = R.drawable.icons8_twitch_96; isIconTinted = true; typeface = benderFont; isSelectable = false
        }
    private val drawerJoinUsTwitter =
        PrimaryDrawerItem().apply {
            tag = "https://twitter.com/austin6561"; nameText = "Twitter"; iconRes = R.drawable.icons8_twitter_squared_96; isIconTinted = true; typeface = benderFont; isSelectable = false
        }

    private val drawerVersion = SecondaryDrawerItem().apply {
        nameText = BuildConfig.VERSION_NAME; iconRes = R.drawable.ic_baseline_info_24; isIconTinted = true; isEnabled = false
    }


    private val drawerCurrencyConverter = SecondaryDrawerItem().apply {
        tag = "currency_converter"; level = 2; identifier = 402; nameText = "Currency Converter"; iconRes = R.drawable.icons8_currency_exchange_96; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerBitcoin = SecondaryDrawerItem().apply {
        tag = "bitcoin"; level = 2; identifier = 401; nameText = "Bitcoin Price"; iconRes = R.drawable.icons8_bitcoin_96; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerSensitivity = SecondaryDrawerItem().apply {
        tag = "sensitivity"; level = 2; identifier = 403; nameText = "Sensitivity Calculator"; iconRes = R.drawable.ic_baseline_calculate_24; isIconTinted =
        true; typeface = benderFont; isEnabled = true
    }

    private val drawerExtraTools = ExpandableDrawerItem().apply {
        nameText = "Tools"
        iconRes = R.drawable.icons8_wrench_96; isIconTinted = true
        typeface = benderFont
        isSelectable = false
        subItems = mutableListOf(
            drawerBitcoin,
            drawerCurrencyConverter,
            drawerSensitivity
        )
    }

    private val drawerSettings = SecondaryDrawerItem().apply {
        tag = "settings"
        nameText = "Settings"; iconRes = R.drawable.ic_baseline_settings_24; isIconTinted = true; isSelectable = false; isEnabled = true
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
        nameText = "Traders"
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

    private val drawerNews = PrimaryDrawerItem().apply {
        tag = "news"; identifier = 601; nameText = "News"; iconRes = R.drawable.ic_baseline_rss_feed_24; isIconTinted = true; typeface = benderFont
    }

    init {
        itemAdapter.add(
            //drawerDivider,
            drawerAmmo,
            drawerGear,
            drawerKeys,
            drawerMedical,
            drawerProvisions,
            //drawerSkills,
            drawerWeaponExpandable,
            drawerWeaponLoadouts,
            drawerWeaponMods,
            drawerDivider,
            drawerFleaMarket,
            drawerHideout,
            drawerMaps,
            drawerQuests,
            drawerDamageSimulator,
            drawerExtraTools,
            drawerTraders,
            drawerDivider,
            drawerNews,
            drawerDivider,
            //drawerLogin,
            drawerSettings,
            //drawerVersion,
        )

        recyclerView.isVerticalFadingEdgeEnabled = false
        recyclerView.isVerticalScrollBarEnabled = false
        expandableExtension.isOnlyOneExpandedItem = true
    }

}

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
    context: Context
) {

    val selectedDrawerItem by navViewModel.selectedDrawerItem.observeAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val benderFont = ResourcesCompat.getFont(context, R.font.bender)

    val drawerLogin = SecondaryDrawerItem().apply {
        tag = "login"
        nameText = "Sign In/Sign Up"
        iconRes = R.drawable.icons8_lock_96_color
        isIconTinted = false
        isSelectable = false
        identifier = 999
        typeface = benderFont
        //badgeText = "BETA";
        //badgeStyle = BadgeStyle().apply { textColor = ColorHolder.fromColorRes(R.color.md_white_1000); color = ColorHolder.fromColorRes(R.color.md_red_700) }
    }

    val drawerUpgrade = PrimaryDrawerItem().apply {
        tag = "upgrade"
        nameText = "Upgrade to Premium"
        iconRes = R.drawable.icons8_buy_upgrade_96
        isSelectable = false
        typeface = benderFont
        identifier = 998
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
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(16.dp)
            ) {
                GameClockText(Modifier.weight(1f), true, navViewModel)
                GameClockText(Modifier.weight(1f), false, navViewModel)
            }
            Divider(
                modifier = Modifier.background(MaterialTheme.colors.surface),
                color = DividerDark
            )
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val drawer = Drawer(context)
                    drawer.onDrawerItemClickListener = { _, item, _ ->
                        if (item.tag != null) {
                            if (item.isSelectable || item.identifier.toInt() == 999) {
                                navViewModel.drawerItemSelected(item)
                                navViewModel.setDrawerOpen(false)
                            } else if (!item.isSelectable && item.isEnabled) {
                                val route = item.tag as String
                                when {
                                    route == "activity:sim" -> context.openActivity(CalculatorMainActivity::class.java)
                                    route == "activity:map" -> context.openActivity(MapsActivity::class.java)
                                    route == "settings" -> context.openActivity(SettingsActivity::class.java)
                                    route == "upgrade" -> context.openActivity(PremiumPusherActivity::class.java)
                                    route.contains("https:") -> {
                                        route.openWithCustomTab(context)
                                    }
                                }
                            }
                        }
                        true
                    }

                    drawer.onDrawerItemLongClickListener = { _, item, _ ->
                        if (item.isSelectable && item.identifier !in 301..308) {
                            //questPrefs.setOpeningItem(item)
                            scope.launch {
                                //scaffoldState.snackbarHostState.showSnackbar("Opening screen set!")
                            }
                        }
                        true
                    }

                    drawer
                }, update = { drawer ->
                    if (selectedDrawerItem != null) {
                        drawer.setSelection(selectedDrawerItem!!.identifier, false)
                    } else {
                        drawer.getDrawerItem(questPrefs.openingPage)?.let {
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
                        val isLoggedIn = it.currentUser != null && it.currentUser?.isAnonymous == false
                        if (!isLoggedIn) {
                            drawer.removeAllStickyFooterItems()
                            drawer.addStickyDrawerItems(drawerLogin)
                        } else {
                            drawer.removeAllStickyFooterItems()
                        }
                    }

                    /*Adapty.setOnPurchaserInfoUpdatedListener(object : OnPurchaserInfoUpdatedListener {
                        override fun onPurchaserInfoReceived(purchaserInfo: PurchaserInfoModel) {
                            if (purchaserInfo.accessLevels["premium"]?.isActive == true) {
                                //Active premium
                                drawer.removeItems(9999, 998)
                            } else {
                                drawer.removeItems(9999, 998)
                                //No premium.
                                drawer.addItemAtPosition(14, DividerDrawerItem().apply { identifier = 9999 })
                                drawer.addItemAtPosition(15, drawerUpgrade)
                            }
                        }
                    })*/

                    /*Adapty.getPurchaserInfo { purchaserInfo, error ->
                        if (error == null) {
                            //Check for premium
                            if (purchaserInfo?.accessLevels?.get("premium")?.isActive == true) {
                                //Active premium
                                drawer.removeItems(9999, 998)
                            } else {
                                drawer.removeItems(9999, 998)
                                //No premium.
                                drawer.addItemAtPosition(15, DividerDrawerItem().apply { identifier = 9999 })
                                drawer.addItemAtPosition(16, drawerUpgrade)
                            }
                        }
                    }*/
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

    val text by if (left) navViewModel.timeLeft.observeAsState("00:00:00") else navViewModel.timeRight.observeAsState("00:00:00")

    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center
    )
}