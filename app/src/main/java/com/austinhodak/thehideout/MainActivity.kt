package com.austinhodak.thehideout

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.calculator.models.CArmor
import com.austinhodak.thehideout.databinding.ActivityMainBinding
import com.austinhodak.thehideout.viewmodels.KeysViewModel
import com.austinhodak.thehideout.viewmodels.WeaponViewModel
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.WeaponModel
import com.austinhodak.thehideout.weapons.WeaponDetailActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.SuggestionModel
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import net.idik.lib.slimadapter.SlimAdapter
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private var searchItem: MenuItem? = null
    private var hideSearch = false
    private lateinit var weaponViewModel: WeaponViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var headerView: AccountHeaderView
    private lateinit var mSearchAdapter: SlimAdapter
    private lateinit var keysViewModel: KeysViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        weaponViewModel = ViewModelProvider(this).get(WeaponViewModel::class.java)
        keysViewModel = ViewModelProvider(this).get(KeysViewModel::class.java)

        setupDrawer(savedInstanceState)
        setupSearchAdapter()
        setupNavigation()

        Firebase.database.setPersistenceEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        Firebase.auth.signInAnonymously()
    }

    private fun setupDrawer(savedInstanceState: Bundle?) {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender)
        binding.slider.apply {
            addItems(
                NavigationDrawerItem(
                    R.id.FirstFragment,
                    PrimaryDrawerItem().apply {
                        typeface = benderFont; isIconTinted = true; name =
                        StringHolder("Ammunition"); iconRes = R.drawable.icons8_ammo_100;
                    },
                    null,
                    null
                ),
                /*ExpandableDrawerItem().apply {
                    typeface = benderFont;
                    nameText = "Armor & Clothing"; iconRes =
                    R.drawable.icons8_coat_100; isSelectable = false; isIconTinted = true
                    subItems = mutableListOf(
                        SecondaryDrawerItem().apply {
                            typeface = benderFont; isIconTinted = true; name =
                            StringHolder("Armor Vests"); iconRes =
                            R.drawable.icons8_bulletproof_vest_100;
                        },
                        SecondaryDrawerItem().apply {
                            typeface = benderFont; isIconTinted = true; name =
                            StringHolder("Chest Rigs"); iconRes = R.drawable.icons8_vest_100;
                        },
                        SecondaryDrawerItem().apply {
                            typeface = benderFont; isIconTinted = true; name =
                            StringHolder("Helmets"); iconRes = R.drawable.icons8_helmet_96;
                        },
                        SecondaryDrawerItem().apply {
                            typeface = benderFont; isIconTinted = true; name =
                            StringHolder("Helmet Addons"); iconRes = R.drawable.icons8_helmet_96;
                        },
                    )
                },*/
                NavigationDrawerItem(R.id.armorTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Armor"); iconRes = R.drawable.icons8_bulletproof_vest_100;
                }, null, null),
                NavigationDrawerItem(R.id.backpackRigTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Backpacks & Rigs"); iconRes = R.drawable.icons8_rucksack_96;
                }),
                /*PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Containers"); iconRes = R.drawable.icons8_storage_box_96;
                },*/
                NavigationDrawerItem(R.id.keysListFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Keys"); iconRes = R.drawable.icons8_key_100;
                }),
                NavigationDrawerItem(R.id.medicalTabFragment, PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Medical"); iconRes = R.drawable.icons8_syringe_100;
                }),
                NavigationDrawerItem(
                    R.id.WeaponFragment,
                    PrimaryDrawerItem().apply {
                        typeface = benderFont; isIconTinted = true; name =
                        StringHolder("Weapons"); iconRes = R.drawable.icons8_assault_rifle_100;
                    },
                    null,
                    null
                ),
                DividerDrawerItem(),
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Hideout"); iconRes = R.drawable.hideout_shadow_1;
                },
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Traders"); iconRes = R.drawable.ic_baseline_groups_24;
                },
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Quests"); iconRes = R.drawable.ic_baseline_assignment_24;
                },
                DividerDrawerItem(),
                PrimaryDrawerItem().apply {
                    identifier = 10; typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Settings"); iconRes = R.drawable.ic_baseline_settings_24;
                },
            )

            headerView = View.inflate(this@MainActivity, R.layout.main_drawer_header, null)
            headerDivider = false
            setSavedInstance(savedInstanceState)
        }

       //startActivity(Intent(this@MainActivity, CalculatorMainActivity::class.java))
    }

    private fun setupNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.FirstFragment, R.id.WeaponFragment, R.id.armorTabFragment, R.id.backpackRigTabFragment, R.id.keysListFragment, R.id.medicalTabFragment), binding.root)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, toolbar, com.mikepenz.materialdrawer.R.string.material_drawer_open, com.mikepenz.materialdrawer.R.string.material_drawer_close)
        binding.root.addDrawerListener(actionBarDrawerToggle)
        binding.slider.setupWithNavController(navController)

        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            setupSearch(destination)
            setToolbarElevation(destination)
        }
    }

    private fun setToolbarElevation(destination: NavDestination) {
        when (destination.id) {
            R.id.FirstFragment,
            R.id.WeaponFragment,
            R.id.armorTabFragment,
            R.id.backpackRigTabFragment -> {
                binding.toolbar.elevation = 0f
            }
            R.id.keysListFragment -> {
                binding.toolbar.elevation = 15f
            }
            R.id.medicalTabFragment -> {
                binding.toolbar.elevation = 0f
            }
            else -> {
                binding.toolbar.elevation = 0f
            }
        }
    }

    private fun setupSearch(currentDestination: NavDestination) {
        Log.d("SEARCH", "CURRENT DESTINATION ${currentDestination.label}")
        val list = when (currentDestination.id) {
            R.id.FirstFragment -> {
                AmmoHelper.getAllAmmoList(this)
            }
            R.id.WeaponFragment -> {
                weaponViewModel.getAllWeaponSearch()
            }
            R.id.keysListFragment -> {
                mutableListOf()
            }
            else -> mutableListOf()
        }

        binding.searchView.setAdapter(mSearchAdapter)
        binding.searchView.setSuggestions(list)

        binding.searchView.setHint("Search ${currentDestination.label}")

        binding.searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                keysViewModel.searchKey.value = newText
                return true
            }
        })

        hideSearch = when (currentDestination.id) {
            R.id.medicalTabFragment -> true
            else -> false
        }

        invalidateOptionsMenu()
    }

    private fun setupSearchAdapter() {
        mSearchAdapter = SlimAdapter.create().register<AmmoModel>(R.layout.ammo_list_item_small_search) { data, injector ->
            injector.text(R.id.ammoSmallName, data.name)
            injector.text(R.id.textView2, data.getSubtitle())
            injector.text(R.id.ammoSmallDamage, data.damage.toString())
            injector.text(R.id.ammoSmallPen, data.penetration.toString())
            injector.text(R.id.ammoSmallCal, data.caliber)

            val subtitleTV = injector.findViewById<TextView>(R.id.textView2)

            if (data.getSubtitle().contains("\n")) {
                if (data.prices.isEmpty()) {
                    subtitleTV.maxLines = data.tradeups.size
                } else {
                    subtitleTV.maxLines = data.prices.size
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

        }.register<WeaponModel>(R.layout.search_item_weapon) { weapon, i ->
            i.text(R.id.searchItemWeaponName, weapon.name)
            i.text(R.id.searchItemWeaponSubtitle, AmmoHelper.getCaliberByID(this, weapon.calibre)?.long_name)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = binding.slider.saveInstanceState(outState)
        outState.putString("title", binding.toolbar.title.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("title").let {
            supportActionBar?.title = it
        }
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (binding.root.isDrawerOpen(binding.slider)) {
            binding.root.closeDrawer(binding.slider)
        } else if (binding.searchView.isSearchOpen) {
            binding.searchView.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        actionBarDrawerToggle.syncState()
        setupSearch(findNavController(R.id.nav_host_fragment).currentDestination!!)
    }
}