package com.austinhodak.thehideout

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.ammunition.models.Ammo
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.databinding.ActivityMainBinding
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.keys.viewmodels.KeysViewModel
import com.austinhodak.thehideout.utils.Time
import com.austinhodak.thehideout.views.OutdatedDrawerItem
import com.austinhodak.thehideout.weapons.WeaponDetailActivity
import com.austinhodak.thehideout.weapons.models.Weapon
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.SuggestionModel
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.addStickyFooterItem
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.skydoves.only.Only
import com.skydoves.only.onlyOnce
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.idik.lib.slimadapter.SlimAdapter
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var searchItem: MenuItem? = null
    private var hideSearch = false
    private lateinit var ammoViewModel: AmmoViewModel
    //private lateinit var bsgViewModel: BSGViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var mSearchAdapter: SlimAdapter
    private lateinit var keysViewModel: KeysViewModel
    private lateinit var fleaViewModel: FleaViewModel
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TheHideout)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)
        keysViewModel = ViewModelProvider(this).get(KeysViewModel::class.java)
        fleaViewModel = ViewModelProvider(this).get(FleaViewModel::class.java)
        //bsgViewModel = ViewModelProvider(this).get(BSGViewModel::class.java)

        setupDrawer(savedInstanceState)
        setupSearchAdapter()
        setupNavigation()

        if (isDebug()) {
            Only.clearOnly("mapGenie")
            Only.clearOnly("damageSim")
        }
    }

    private fun setupDrawer(savedInstanceState: Bundle?) {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender)
        val discord = PrimaryDrawerItem().apply {
            typeface = benderFont
            nameText = getString(R.string.discord)
            iconRes = R.drawable.icons8_discord_96
            isIconTinted = true
            isSelectable = false
            onDrawerItemClickListener = { _, _, _ ->
                "https://discord.gg/YQW36z29z6".openWithCustomTab(this@MainActivity)
                false
            }
        }

        val twitch = PrimaryDrawerItem().apply {
            typeface = benderFont
            nameText = getString(R.string.twitch)
            iconRes = R.drawable.icons8_twitch_96
            isIconTinted = true
            isSelectable = false
            onDrawerItemClickListener = { _, _, _ ->
                "https://www.twitch.tv/theeeelegend".openWithCustomTab(this@MainActivity)
                false
            }
        }

        val twitter = PrimaryDrawerItem().apply {
            typeface = benderFont
            nameText = getString(R.string.twitter)
            iconRes = R.drawable.icons8_twitter_squared_96
            isIconTinted = true
            isSelectable = false
            onDrawerItemClickListener = { _, _, _ ->
                "https://twitter.com/austin6561".openWithCustomTab(this@MainActivity)
                false
            }
        }

        binding.slider.apply {
            if (Firebase.remoteConfig.getBoolean("drawer_announcement_enabled")) {
                val announcementObject = JSONObject(Firebase.remoteConfig.getString("drawer_announcement"))
                if (Firebase.remoteConfig.getString("drawer_announcement").isEmpty()) return@apply
                addStickyFooterItem(
                    OutdatedDrawerItem().apply {
                        typeface = benderFont
                        iconRes = R.drawable.icons8_megaphone_96
                        nameText = announcementObject.getString("title")
                        descriptionText = announcementObject.getString("subtitle")
                        isSelectable = false
                        isSelected = true
                        isClickable = false
                    },
                )
            }

            addItems(
                NavigationDrawerItem(R.id.FirstFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.ammunition))
                    iconRes = R.drawable.icons8_ammo_100
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.armorTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.armor))
                    iconRes = R.drawable.icons8_bulletproof_vest_100
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.backpackRigTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.backpacks_rigs))
                    iconRes = R.drawable.icons8_rucksack_96
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.keysListFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.keys))
                    iconRes = R.drawable.icons8_key_100
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.medicalTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.medical))
                    iconRes = R.drawable.icons8_syringe_100
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.WeaponFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.weapons))
                    iconRes = R.drawable.icons8_assault_rifle_100
                }, options = getNavOptions()),
                DividerDrawerItem(),
                /*NavigationDrawerItem(R.id.dealersTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.barters))
                    iconRes = R.drawable.icons8_available_updates_96
                    isEnabled = isDebug()
                }, options = getNavOptions()),*/
                NavigationDrawerItem(R.id.fleaMarketListFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.flea_market))
                    iconRes = R.drawable.ic_baseline_shopping_cart_24
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.hideoutMainFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.hideout))
                    iconRes = R.drawable.icons8_tent_96
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.questMainFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.quests))
                    iconRes = R.drawable.ic_baseline_assignment_24
                }, options = getNavOptions()),
                PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder(context.getString(R.string.simulator))
                    iconRes = R.drawable.icons8_dog_tag_96
                    isSelectable = false
                    onDrawerItemClickListener = { _, _, _ ->
                        startActivity(Intent(this@MainActivity, CalculatorMainActivity::class.java))
                        false
                    }
                },
                /*PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    nameText = "Loadout Creator"
                    iconRes = R.drawable.icons8_ammo_100
                    isSelectable = false
                    onDrawerItemClickListener = { _, _, _ ->
                        startActivity(Intent(this@MainActivity, CreatorMainActivity::class.java))
                        false
                    }
                },*/
                DividerDrawerItem(),
                PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    nameText = "Map Genie"
                    iconRes = R.drawable.ic_baseline_map_24

                    isSelectable = false
                    onDrawerItemClickListener = { _, _, _ ->
                        onlyOnce("mapGenie") {
                            onDo {
                                MaterialDialog(this@MainActivity).show {
                                    title(text = "Warning")
                                    message(text = "We are not affiliated with Map Genie, anything you do on their site will not link back to this app.")
                                    positiveButton(text = "GOT IT") { dialog ->
                                        openMapGenie()
                                    }
                                    negativeButton(text = "NEVERMIND") { dialog ->
                                        Only.clearOnly("mapGenie")
                                    }
                                }
                            }
                            onDone {
                                openMapGenie()
                            }
                        }
                        false
                    }
                },
                SectionDrawerItem().apply {
                    nameText = context.getString(R.string.join_us)
                },
                discord,
                twitch,
                twitter,
                DividerDrawerItem(),
                SecondaryDrawerItem().apply {
                    isEnabled = false
                    typeface = benderFont
                    isIconTinted = true
                    nameText = "${BuildConfig.VERSION_NAME}"
                    iconRes = R.drawable.ic_baseline_info_24
                },
                /*PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder("Settings")
                    isSelectable = false
                    iconRes = R.drawable.ic_baseline_settings_24
                    onDrawerItemClickListener = { _, _, _ ->
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        false
                    }
                }*/
            )

            headerView = View.inflate(this@MainActivity, R.layout.layout_drawer_header, null)
            headerDivider = false
            onDrawerItemLongClickListener = { view, item, index ->
                if (item is NavigationDrawerItem) {
                    prefs.edit {
                        putInt("defaultOpeningFragment", item.resId)
                    }
                    Snackbar.make(binding.root, context.getString(R.string.set_opening_screen), Snackbar.LENGTH_SHORT).apply {
                        this.setBackgroundTint(resources.getColor(R.color.md_green_500))
                        this.setTextColor(Color.WHITE)
                    }.show()
                }
                false
            }
            setSavedInstance(savedInstanceState)
        }

        binding.slider.recyclerView.isVerticalScrollBarEnabled = false

        setupTimes(binding.slider.headerView)
    }

    private fun openMapGenie() {
        "https://mapgenie.io/tarkov/maps/customs".openWithCustomTab(this@MainActivity)
    }

    private fun setupTimes(headerView: View?) {
        val time1 = headerView?.findViewById<TextView>(R.id.header_time_1)
        val time2 = headerView?.findViewById<TextView>(R.id.header_time_2)
        val formatter = SimpleDateFormat("HH:mm:ss")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        GlobalScope.launch {
            while (true) {
                runOnUiThread {
                    time1?.text = formatter.format(Time.realTimeToTarkovTime(true))
                    time2?.text = formatter.format(Time.realTimeToTarkovTime(false))
                }
                delay(1000 / 7)
            }
        }
    }

    private fun setupNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.hideoutMainFragment,
                R.id.fleaMarketListFragment,
                R.id.FirstFragment,
                R.id.WeaponFragment,
                R.id.armorTabFragment,
                R.id.backpackRigTabFragment,
                R.id.keysListFragment,
                R.id.medicalTabFragment,
                R.id.questMainFragment,
                R.id.dealersTabFragment
            ), binding.root
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.root,
            toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close
        )
        binding.root.addDrawerListener(actionBarDrawerToggle)
        binding.slider.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            setupSearch(destination)
            setToolbarElevation(destination)
            supportActionBar?.title = ""
            binding.toolbarTitle.text = destination.label
            setQuestChipVisibility(false)
        }

        //Handle launcher long press shortcut.
        if (intent?.action != null && intent?.action == "shortcut.flea") {
            navController.navigate(R.id.fleaMarketListFragment, null, NavOptions.Builder().setPopUpTo(R.id.fleaMarketListFragment, true).build())
        } else {
            prefs.getInt("defaultOpeningFragment", R.id.FirstFragment).also {
                try {
                    navController.navigate(it, null, NavOptions.Builder().setPopUpTo(it, true).build())
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

    }


    private fun setToolbarElevation(destination: NavDestination) {
        binding.toolbar.elevation = when (destination.id) {
            R.id.keysListFragment,
            R.id.fleaMarketListFragment -> 15f
            else -> 0f
        }
    }

    private fun setupSearch(currentDestination: NavDestination) {
        var list: MutableList<SuggestionModel>? = null

        ammoViewModel.ammoList.observe(this) {
            list = when (currentDestination.id) {
                R.id.FirstFragment -> {
                    ammoViewModel.ammoList.value?.map { ammo ->
                        SuggestionModel("${AmmoHelper.caliberList.find { it.key == ammo.caliber }?.name} ${ammo.name}", ammo)
                    }?.toMutableList()
                }
                R.id.WeaponFragment -> {
                    //TODO FIX THIS. ISSUE #7
                    //weaponViewModel.getAllWeaponSearch()
                    mutableListOf()
                }
                R.id.keysListFragment -> {
                    mutableListOf()
                }
                else -> mutableListOf()
            }

            binding.searchView.setSuggestions(list!!)
        }

        binding.searchView.setAdapter(mSearchAdapter)
        if (list != null) {
            binding.searchView.setSuggestions(list!!)
        }

        binding.searchView.setHint("Search ${currentDestination.label}")

        binding.searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                keysViewModel.searchKey.value = newText
                fleaViewModel.searchKey.value = newText
                return true
            }
        })

        hideSearch = when (currentDestination.id) {
            R.id.medicalTabFragment,
            R.id.questMainFragment,
            R.id.backpackRigTabFragment,
            R.id.hideoutMainFragment,
            R.id.armorTabFragment,
            R.id.dealersTabFragment,
            R.id.WeaponFragment -> true
            else -> false
        }

        invalidateOptionsMenu()
    }

    fun isSearchHidden(boolean: Boolean) {
        hideSearch = boolean
        invalidateOptionsMenu()
    }

    private fun setupSearchAdapter() {
        mSearchAdapter = SlimAdapter.create().register<Ammo>(R.layout.item_ammo_search) { data, injector ->
            injector.text(R.id.ammoSmallName, data.name)
            injector.text(R.id.textView2, data.getSubtitle())
            injector.text(R.id.ammoSmallDamage, data.damage.toString())
            injector.text(R.id.ammoSmallPen, data.penetration.toString())
            injector.text(R.id.ammoSmallCal, AmmoHelper.caliberList.find { it.key == data.caliber }?.name)

            val subtitleTV = injector.findViewById<TextView>(R.id.textView2)

            if (data.getSubtitle().contains("\n")) {
                if (data.prices?.isEmpty() == true) {
                    subtitleTV.maxLines = data.tradeups?.size ?: 0
                } else {
                    subtitleTV.maxLines = data.prices?.size ?: 0
                }
            } else {
                subtitleTV.maxLines = 1
            }

            injector.background(R.id.ammoSmallDMG1, data.getColor(1))
            injector.background(R.id.ammoSmallDMG2, data.getColor(2))
            injector.background(R.id.ammoSmallDMG3, data.getColor(3))
            injector.background(R.id.ammoSmallDMG4, data.getColor(4))
            injector.background(R.id.ammoSmallDMG5, data.getColor(5))
            injector.background(R.id.ammoSmallDMG6, data.getColor(6))

        }.register<Weapon>(R.layout.item_weapon_search) { weapon, i ->
            i.text(R.id.searchItemWeaponName, weapon.name)
            i.text(R.id.searchItemWeaponSubtitle, AmmoHelper.getCaliberByID(weapon.calibre)?.longName)
            i.clicked(R.id.searchItemWeaponRoot) {
                startActivity(Intent(this, WeaponDetailActivity::class.java).apply {
                    putExtra("id", weapon._id)
                })
            }
        }.registerDefault(R.layout.layout_empty) { default, i -> }.attachTo(binding.searchView.getRV())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        searchItem = menu.findItem(R.id.main_search)
        binding.searchView.setMenuItem(searchItem)
        searchItem?.isVisible = !hideSearch
        return true
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = binding.slider.saveInstanceState(outState)
        outState.putString("title", binding.toolbarTitle.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        try {
            super.onRestoreInstanceState(savedInstanceState)
            savedInstanceState.getString("title").let {
                binding.toolbarTitle.text = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        when {
            binding.root.isDrawerOpen(binding.slider) -> {
                binding.root.closeDrawer(binding.slider)
            }
            binding.searchView.isSearchOpen -> {
                binding.searchView.closeSearch()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actionBarDrawerToggle.syncState()
        setupSearch(findNavController(R.id.nav_host_fragment).currentDestination!!)
    }

    fun setQuestChipVisibility(visible: Boolean) {
        if (this::binding.isInitialized)
            binding.questSelectorScrollbar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun getQuestChips(): ChipGroup? {
        if (!this::binding.isInitialized) return null
        return binding.chipGroup2
    }

    fun updateChips(list: ArrayList<String>) {
        if (!this::binding.isInitialized) return
        for (i in list) {
            val chip = binding.chipGroup2[list.indexOf(i)] as Chip
            chip.text = i
        }
    }

    private fun getNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build()
    }
}