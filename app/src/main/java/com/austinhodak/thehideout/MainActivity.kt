package com.austinhodak.thehideout

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
        setupActionBarWithNavController(findNavController(R.id.nav_host_fragment))

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.root, binding.toolbar, com.mikepenz.materialdrawer.R.string.material_drawer_open, com.mikepenz.materialdrawer.R.string.material_drawer_close)
        actionBarDrawerToggle.drawerArrowDrawable.color = resources.getColor(android.R.color.white)
        binding.root.addDrawerListener(actionBarDrawerToggle)

        val benderFont = resources.getFont(R.font.bender)

        binding.slider.apply {
            addItems(
                NavigationDrawerItem(R.id.FirstFragment, PrimaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Ammunition"); iconRes = R.drawable.icons8_ammo_100; }),
                ExpandableDrawerItem().apply {
                    typeface = benderFont;
                    nameText = "Armor & Clothing"; iconRes = R.drawable.icons8_coat_100; isSelectable = false; isIconTinted = true
                    subItems = mutableListOf(
                        SecondaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Armor Vests"); iconRes = R.drawable.icons8_bulletproof_vest_100; },
                        SecondaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Chest Rigs"); iconRes = R.drawable.icons8_vest_100; },
                        SecondaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Helmets"); iconRes = R.drawable.icons8_helmet_96; },
                        SecondaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Helmet Addons"); iconRes = R.drawable.icons8_helmet_96; },
                    )
                },
                NavigationDrawerItem(R.id.SecondFragment, PrimaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Containers"); iconRes = R.drawable.icons8_storage_box_96; }),
                PrimaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Keys & Intel"); iconRes = R.drawable.icons8_key_100; },
                PrimaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Medical"); iconRes = R.drawable.icons8_syringe_100; },
                PrimaryDrawerItem().apply { typeface = benderFont; isIconTinted = true; name = StringHolder("Weapons"); iconRes = R.drawable.icons8_assault_rifle_100; },
                //SectionDrawerItem().apply { nameText = "Flea Market" },

            )
            headerView = View.inflate(this@MainActivity, R.layout.main_drawer_header, null)
            headerDivider = false
        }

        binding.slider.setupWithNavController(findNavController(R.id.nav_host_fragment))

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

    override fun onResume() {
        super.onResume()
        actionBarDrawerToggle.syncState()
    }
}