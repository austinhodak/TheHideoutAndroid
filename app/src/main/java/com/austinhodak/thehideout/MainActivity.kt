package com.austinhodak.thehideout

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.databinding.ActivityMainBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.austinhodak.thehideout.viewmodels.KeysViewModel
import com.austinhodak.thehideout.viewmodels.models.Weapon
import com.austinhodak.thehideout.viewmodels.models.firestore.FSAmmo
import com.austinhodak.thehideout.weapons.WeaponDetailActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.SuggestionModel
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import net.idik.lib.slimadapter.SlimAdapter


class MainActivity : AppCompatActivity() {

    private var searchItem: MenuItem? = null
    private var hideSearch = false
    private lateinit var ammoViewModel: AmmoViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var mSearchAdapter: SlimAdapter
    private lateinit var keysViewModel: KeysViewModel
    private lateinit var fleaViewModel: FleaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TheHideout_NoActionBar)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)
        keysViewModel = ViewModelProvider(this).get(KeysViewModel::class.java)
        fleaViewModel = ViewModelProvider(this).get(FleaViewModel::class.java)

        setupDrawer(savedInstanceState)
        setupSearchAdapter()
        setupNavigation()

        /*onlyOnce("introIGN") {

        }*/
    }

    private fun setupDrawer(savedInstanceState: Bundle?) {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender)
        val discord = PrimaryDrawerItem().apply {
            typeface = benderFont
            nameText = "Discord"
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
            nameText = "Twitch"
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
            nameText = "Twitter"
            iconRes = R.drawable.icons8_twitter_squared_96
            isIconTinted = true
            isSelectable = false
            onDrawerItemClickListener = { _, _, _ ->
                "https://twitter.com/austin6561".openWithCustomTab(this@MainActivity)
                false
            }
        }

        binding.slider.apply {
            addItems(
                NavigationDrawerItem(
                    R.id.FirstFragment,
                    PrimaryDrawerItem().apply {
                        typeface = benderFont; isIconTinted = true; name =
                        StringHolder("Ammunition"); iconRes = R.drawable.icons8_ammo_100
                    },
                    null,
                    getNavOptions()
                ),
                NavigationDrawerItem(R.id.armorTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Armor"); iconRes = R.drawable.icons8_bulletproof_vest_100
                }, null, null),
                NavigationDrawerItem(R.id.backpackRigTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Backpacks & Rigs"); iconRes = R.drawable.icons8_rucksack_96
                }),
                NavigationDrawerItem(R.id.keysListFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Keys"); iconRes = R.drawable.icons8_key_100
                }, options = getNavOptions()),
                NavigationDrawerItem(R.id.medicalTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Medical"); iconRes = R.drawable.icons8_syringe_100
                }, options = getNavOptions()),
                NavigationDrawerItem(
                    R.id.WeaponFragment,
                    PrimaryDrawerItem().apply {
                        typeface = benderFont; isIconTinted = true
                        name = StringHolder("Weapons"); iconRes = R.drawable.icons8_assault_rifle_100
                    },
                    null,
                    null
                ),
                DividerDrawerItem(),
                /*PrimaryDrawerItem().apply {
                    typeface = benderFont
                    isIconTinted = true
                    name = StringHolder("Damage Calculator")
                    iconRes = R.drawable.icons8_ammo_100
                    isSelectable = false
                    onDrawerItemClickListener = { _, _, _ ->
                        startActivity(Intent(this@MainActivity, CalculatorMainActivity::class.java))
                        false
                    }
                },*/
                NavigationDrawerItem(R.id.fleaMarketListFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true
                    name = StringHolder("Flea Market"); iconRes = R.drawable.ic_baseline_shopping_cart_24
                }),
                NavigationDrawerItem(R.id.hideoutMainFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true
                    name = StringHolder("Hideout"); iconRes = R.drawable.hideout_shadow_1
                }),
                NavigationDrawerItem(R.id.questMainFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true
                    name = StringHolder("Quests"); iconRes = R.drawable.ic_baseline_assignment_24
                }),
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true
                    name = StringHolder("Traders"); iconRes = R.drawable.ic_baseline_groups_24
                    isEnabled = false
                },
                SectionDrawerItem().apply {
                    nameText = "Join us on"
                },
                discord,
                twitch,
                twitter,
                DividerDrawerItem(),
                SecondaryDrawerItem().apply {
                    isEnabled = false
                    typeface = benderFont
                    isIconTinted = true
                    nameText = BuildConfig.VERSION_NAME
                    iconRes = R.drawable.ic_baseline_info_24
                }
                /*PrimaryDrawerItem().apply {
                    identifier = 10; typeface = benderFont; isIconTinted = true;
                    name = StringHolder("Settings"); iconRes = R.drawable.ic_baseline_settings_24;
                },*/
            )

            headerView = View.inflate(this@MainActivity, R.layout.main_drawer_header, null)
            headerDivider = true
            setSavedInstance(savedInstanceState)
        }

        binding.slider.recyclerView.isVerticalScrollBarEnabled = false
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
                R.id.questMainFragment
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

        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            setupSearch(destination)
            setToolbarElevation(destination)
            supportActionBar?.title = ""
            binding.toolbarTitle.text = destination.label
            setQuestChipVisibility(false)
        }
    }

    private fun setToolbarElevation(destination: NavDestination) {
        binding.toolbar.elevation = when (destination.id) {
            R.id.FirstFragment,
            R.id.WeaponFragment,
            R.id.armorTabFragment,
            R.id.medicalTabFragment,
            R.id.backpackRigTabFragment -> 0f

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
            R.id.WeaponFragment -> true
            else -> false
        }

        invalidateOptionsMenu()

        val a = arrayOf(10, 30)
        val b = listOf(10,20)
        val cl = b

    }

    fun isSearchHidden(boolean: Boolean) {
        hideSearch = boolean
        invalidateOptionsMenu()
    }

    private fun setupSearchAdapter() {
        mSearchAdapter = SlimAdapter.create().register<FSAmmo>(R.layout.ammo_list_item_small_search) { data, injector ->
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

        }.register<Weapon>(R.layout.search_item_weapon) { weapon, i ->
            i.text(R.id.searchItemWeaponName, weapon.name)
            i.text(R.id.searchItemWeaponSubtitle, AmmoHelper.getCaliberByID(weapon.calibre)?.longName)
            i.clicked(R.id.searchItemWeaponRoot) {
                startActivity(Intent(this, WeaponDetailActivity::class.java).apply {
                    putExtra("id", weapon._id)
                })
            }
        }.registerDefault(R.layout.empty_layout) { default, i -> }.attachTo(binding.searchView.getRV())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        searchItem = menu.findItem(R.id.main_search)
        binding.searchView.setMenuItem(searchItem)
        searchItem?.isVisible = !hideSearch
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = binding.slider.saveInstanceState(outState)
        outState.putString("title", binding.toolbarTitle.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("title").let {
            binding.toolbarTitle.text = it
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

    fun getQuestChips(): ChipGroup {
        return binding.chipGroup2
    }

    fun updateChips(list: ArrayList<String>) {
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