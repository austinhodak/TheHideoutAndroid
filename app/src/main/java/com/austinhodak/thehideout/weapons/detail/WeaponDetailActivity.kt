package com.austinhodak.thehideout.weapons.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.models.AmmoOld
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.databinding.ActivityWeaponDetailBinding
import com.austinhodak.thehideout.weapons.viewmodels.WeaponViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.idik.lib.slimadapter.SlimAdapter

@AndroidEntryPoint
class WeaponDetailActivity : AppCompatActivity() {
    lateinit var viewModel: WeaponViewModel
    lateinit var ammoViewModel: AmmoViewModel
    private var sortByIndex = 5
    lateinit var ammoAdapter: SlimAdapter
    lateinit var loadoutAdapter: SlimAdapter
    lateinit var ammoList: List<AmmoOld>
    lateinit var binding: ActivityWeaponDetailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWeaponDetailBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        viewModel = ViewModelProvider(this).get(WeaponViewModel::class.java)
        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)
        //bsgViewModel = ViewModelProvider(this).get(BSGViewModel::class.java)
        setupToolbar()
        loadWeapon()
    }

    private fun loadWeapon() {

    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.weaponDetailToolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        findViewById<Toolbar>(R.id.weaponDetailToolbar).setNavigationOnClickListener { super.onBackPressed() }
    }

   /* override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_weapon_detail, menu)
        menu.findItem(R.id.weapon_wiki).isVisible = false
        if (weapon.fleaItem?.wikiLink == null) {
            menu.findItem(R.id.weapon_wiki).isVisible = false
        }
        return true
    }*/
}