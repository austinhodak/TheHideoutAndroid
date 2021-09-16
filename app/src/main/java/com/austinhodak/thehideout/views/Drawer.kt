package com.austinhodak.thehideout.views

import android.content.Context
import android.util.AttributeSet
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
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.map.MapsActivity
import com.austinhodak.thehideout.questPrefs
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.firebase.ui.auth.AuthUI
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.util.getDrawerItem
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
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
        PrimaryDrawerItem().apply { tag = "https://discord.gg/YQW36z29z6"; nameText = "Discord"; iconRes = R.drawable.icons8_discord_96; isIconTinted = true; typeface = benderFont; isSelectable = false }
    private val drawerJoinUsTwitch =
        PrimaryDrawerItem().apply { tag = "https://www.twitch.tv/theeeelegend"; nameText = "Twitch"; iconRes = R.drawable.icons8_twitch_96; isIconTinted = true; typeface = benderFont; isSelectable = false }
    private val drawerJoinUsTwitter =
        PrimaryDrawerItem().apply { tag = "https://twitter.com/austin6561"; nameText = "Twitter"; iconRes = R.drawable.icons8_twitter_squared_96; isIconTinted = true; typeface = benderFont; isSelectable = false }

    private val drawerVersion = SecondaryDrawerItem().apply {
        nameText = BuildConfig.VERSION_NAME; iconRes = R.drawable.ic_baseline_info_24; isIconTinted = true; isEnabled = false
    }
    private val drawerLogin = SecondaryDrawerItem().apply {
        nameText = "Sign In"; iconRes = R.drawable.ic_baseline_info_24; isIconTinted = true; isSelectable = false; identifier = 999
    }

    init {
        itemAdapter.add(
            drawerAmmo,
            drawerGear,
            drawerKeys,
            drawerMedical,
            drawerProvisions,
            drawerSkills,
            drawerWeaponExpandable,
            drawerWeaponMods,
            drawerDivider,
            drawerFleaMarket,
            drawerHideout,
            drawerQuests,
            drawerDamageSimulator,
            drawerDivider,
            drawerMaps,
            drawerSectionJoinUs,
            drawerJoinUsDiscord,
            drawerJoinUsTwitch,
            drawerJoinUsTwitter,
            drawerDivider,
            //drawerLogin,
            drawerVersion
        )
        recyclerView.isVerticalFadingEdgeEnabled = false
        recyclerView.isVerticalScrollBarEnabled = false
        expandableExtension.isOnlyOneExpandedItem = true
    }

}

@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun MainDrawer(
    navViewModel: NavViewModel,
    lifecycleOwner: LifecycleOwner
) {

    val selectedDrawerItem by navViewModel.selectedDrawerItem.observeAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

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
                                    route.contains("https:") -> {
                                        route.openWithCustomTab(context)
                                    }
                                }
                            }
                        }
                        true
                    }

                    drawer.onDrawerItemLongClickListener = { _, item, _ ->
                        if (item.isSelectable) {
                            questPrefs.setOpeningItem(item)
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Opening screen set!")
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