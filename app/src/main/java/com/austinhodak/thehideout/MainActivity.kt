package com.austinhodak.thehideout

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.austinhodak.thehideout.databinding.ActivityMainBinding
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.setupWithNavController
import com.mikepenz.materialdrawer.widget.AccountHeaderView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var headerView: AccountHeaderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        setupDrawer(savedInstanceState)
        setupNavigation()
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
                ExpandableDrawerItem().apply {
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
                },
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Containers"); iconRes = R.drawable.icons8_storage_box_96;
                },
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Keys & Intel"); iconRes = R.drawable.icons8_key_100;
                },
                PrimaryDrawerItem().apply {
                    typeface = benderFont; isIconTinted = true; name =
                    StringHolder("Medical"); iconRes = R.drawable.icons8_syringe_100;
                },
                NavigationDrawerItem(
                    R.id.WeaponFragment,
                    PrimaryDrawerItem().apply {
                        typeface = benderFont; isIconTinted = true; name =
                        StringHolder("Weapons"); iconRes = R.drawable.icons8_assault_rifle_100;
                    },
                    null,
                    null
                ),
                //SectionDrawerItem().apply { nameText = "Flea Market" },

            )
            headerView = View.inflate(this@MainActivity, R.layout.main_drawer_header, null)
            headerDivider = false
            setSavedInstance(savedInstanceState)
        }
    }

    private fun setupNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.FirstFragment, R.id.WeaponFragment), binding.root)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, toolbar, com.mikepenz.materialdrawer.R.string.material_drawer_open, com.mikepenz.materialdrawer.R.string.material_drawer_close)
        binding.root.addDrawerListener(actionBarDrawerToggle)
        binding.slider.setupWithNavController(navController)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
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
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        actionBarDrawerToggle.syncState()

    }
}