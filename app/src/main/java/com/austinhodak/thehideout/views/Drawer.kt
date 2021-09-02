package com.austinhodak.thehideout.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import timber.log.Timber

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

class Drawer(context: Context, attrs: AttributeSet? = null) : MaterialDrawerSliderView(context, attrs) {

    private val benderFont = ResourcesCompat.getFont(context, R.font.bender)

    private val drawerAmmo = PrimaryDrawerItem().apply { tag = "ammunition/Caliber762x35"; identifier = 101; nameText = "Ammunition"; iconRes = R.drawable.icons8_ammo_100; isIconTinted = true; typeface = benderFont }
    private val drawerKeys = PrimaryDrawerItem().apply { tag = "keys"; identifier = 104; nameText = "Keys"; iconRes = R.drawable.icons8_key_100; isIconTinted = true; typeface = benderFont }
    private val drawerMedical = PrimaryDrawerItem().apply { tag = "medical"; identifier = 105; nameText = "Medical"; iconRes = R.drawable.icons8_syringe_100; isIconTinted = true; typeface = benderFont }
    private val drawerFleaMarket = PrimaryDrawerItem().apply { identifier = 107; nameText = "Flea Market"; iconRes = R.drawable.ic_baseline_shopping_cart_24; isIconTinted = true; typeface = benderFont }
    private val drawerHideout = PrimaryDrawerItem().apply { identifier = 108; nameText = "Hideout"; iconRes = R.drawable.icons8_tent_96; isIconTinted = true; typeface = benderFont }
    private val drawerQuests = PrimaryDrawerItem().apply { identifier = 109; nameText = "Quests"; iconRes = R.drawable.ic_baseline_assignment_24; isIconTinted = true; typeface = benderFont }
    private val drawerMaps = PrimaryDrawerItem().apply { tag = "url:https://mapgenie.io/tarkov/maps/customs"; identifier = 110; nameText = "Map Genie"; iconRes = R.drawable.ic_baseline_map_24; isIconTinted = true; typeface = benderFont }
    private val drawerDamageSimulator = PrimaryDrawerItem().apply { tag = "activity:sim"; identifier = 111; nameText = "Tarkov'd Simulator"; iconRes = R.drawable.icons8_dog_tag_96; isIconTinted = true; typeface = benderFont }
    private val drawerSkills = PrimaryDrawerItem().apply { identifier = 113; nameText = "Skills"; iconRes = R.drawable.icons8_development_skill_96; isIconTinted = true; typeface = benderFont }
    private val drawerWeaponMods = PrimaryDrawerItem().apply { tag = "weaponmods"; identifier = 114; nameText = "Weapon Mods"; iconRes = R.drawable.icons8_assault_rifle_mod_96; isIconTinted = true; typeface = benderFont }

    private val drawerProvisions = PrimaryDrawerItem().apply { tag = "food"; identifier = 112; nameText = "Provisions"; iconRes = R.drawable.ic_baseline_fastfood_24; isIconTinted = true; typeface = benderFont }

    private val drawerArmor = SecondaryDrawerItem().apply { tag = "gear/Armor"; level = 2; identifier = 201; nameText = "Armor"; iconRes = R.drawable.icons8_bulletproof_vest_100; isIconTinted = true; typeface = benderFont }
    private val drawerBackpacks = SecondaryDrawerItem().apply { tag = "gear/Backpacks"; level = 2; identifier = 202; nameText = "Backpacks"; iconRes = R.drawable.icons8_rucksack_96; isIconTinted = true; typeface = benderFont }
    private val drawerChestRigs = SecondaryDrawerItem().apply { tag = "gear/Rigs"; level = 2; identifier = 203; nameText = "Chest Rigs"; iconRes = R.drawable.icons8_vest_100; isIconTinted = true; typeface = benderFont }
    private val drawerEyewear = SecondaryDrawerItem().apply { tag = "gear/Eyewear"; level = 2; identifier = 204; nameText = "Eyewear"; iconRes = R.drawable.icons8_sun_glasses_96; isIconTinted = true; typeface = benderFont }
    private val drawerFaceCover = SecondaryDrawerItem().apply { tag = "gear/Facecover"; level = 2; identifier = 205; nameText = "Face Coverings"; iconRes = R.drawable.icons8_camo_cream_96; isIconTinted = true; typeface = benderFont }
    private val drawerHeadsets = SecondaryDrawerItem().apply { tag = "gear/Headsets"; level = 2; identifier = 206; nameText = "Headsets"; iconRes = R.drawable.icons8_headset_96; isIconTinted = true; typeface = benderFont }
    private val drawerHeadwear = SecondaryDrawerItem().apply { tag = "gear/Headwear"; level = 2; identifier = 207; nameText = "Headwear"; iconRes = R.drawable.icons8_helmet_96; isIconTinted = true; typeface = benderFont }
    private val drawerTacticalClothing = SecondaryDrawerItem().apply { tag = "gear/Clothing"; level = 2; identifier = 208; nameText = "Tactical Clothing"; iconRes = R.drawable.icons8_coat_96; isIconTinted = true; typeface = benderFont; isEnabled = false }

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
                level = 1; iconRes = R.drawable.ic_blank; identifier = it.second.toLong(); nameText = it.first; typeface = benderFont; tag = "weapons/${it.third}"
            }
        }.toMutableList()
    }

    private val drawerDivider = DividerDrawerItem()

    private val drawerSectionJoinUs = SectionDrawerItem().apply { nameText = "Join us on" }
    private val drawerJoinUsDiscord = PrimaryDrawerItem().apply { nameText = "Discord"; iconRes = R.drawable.icons8_discord_96; isIconTinted = true; typeface = benderFont }
    private val drawerJoinUsTwitch = PrimaryDrawerItem().apply { nameText = "Twitch"; iconRes = R.drawable.icons8_twitch_96; isIconTinted = true; typeface = benderFont }
    private val drawerJoinUsTwitter = PrimaryDrawerItem().apply { nameText = "Twitter"; iconRes = R.drawable.icons8_twitter_squared_96; isIconTinted = true; typeface = benderFont }

    private val drawerVersion = SecondaryDrawerItem().apply { nameText = BuildConfig.VERSION_NAME; iconRes = R.drawable.ic_baseline_info_24; isIconTinted = true; isEnabled = false }

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
            drawerVersion
        )
        recyclerView.isVerticalFadingEdgeEnabled = false
        recyclerView.isVerticalScrollBarEnabled = false
        expandableExtension.isOnlyOneExpandedItem = true
    }
}

@Composable
fun MainDrawer(
    navViewModel: NavViewModel
) {
    val selectedDrawerItem: Pair<String, Int> by navViewModel.selectedDrawerItem.observeAsState(Pair("ammunition/Caliber762x35", 101))

    Timber.d(selectedDrawerItem.toString())

    Column(
        Modifier.fillMaxSize()
    ) {
        /*Row(Modifier.background(MaterialTheme.colors.surface).padding(16.dp)) {
            GameClockText(Modifier.weight(1f), timeLeft)
            GameClockText(Modifier.weight(1f), timeRight)
        }*/
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val drawer = Drawer(context)
                drawer.onDrawerItemClickListener = { _, item, _ ->
                    if (item.isSelectable) {
                        navViewModel.drawerItemSelected(Pair(item.tag.toString(), item.identifier.toInt()))
                        navViewModel.setDrawerOpen(false)
                    }
                    true
                }

                //drawer.setSelection(selectedDrawerItem.second.toLong(), false)

                drawer
            }, update = { drawer ->
                drawer.setSelection(selectedDrawerItem.second.toLong(), false)
            }
        )
    }
}