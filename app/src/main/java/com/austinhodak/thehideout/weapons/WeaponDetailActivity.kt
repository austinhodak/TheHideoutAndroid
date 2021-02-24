package com.austinhodak.thehideout.weapons

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.models.Ammo
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.databinding.ActivityWeaponDetailBinding
import com.austinhodak.thehideout.weapons.models.Weapon
import com.austinhodak.thehideout.weapons.viewmodels.WeaponViewModel
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

class WeaponDetailActivity : AppCompatActivity() {
    lateinit var viewModel: WeaponViewModel
    lateinit var ammoViewModel: AmmoViewModel
    lateinit var weapon: Weapon
    private var sortByIndex = 5
    lateinit var ammoAdapter: SlimAdapter
    lateinit var loadoutAdapter: SlimAdapter
    lateinit var ammoList: List<Ammo>
    lateinit var binding: ActivityWeaponDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weapon_detail)
        viewModel = ViewModelProvider(this).get(WeaponViewModel::class.java)
        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)
        setupToolbar()
        loadWeapon()
        setupAdapters()
    }

    private fun loadWeapon() {
        weapon = viewModel.weaponsList.value?.find { it._id == intent.getStringExtra("id") ?: "" } ?: return
        //weapon = viewModel.getWeaponByID(intent.getStringExtra("id") ?: "")
        binding.weapon = weapon

        ammoViewModel.ammoList.observe(this) { list ->
            ammoList = list.filter { it.caliber == weapon.calibre }.sortedBy { it.armor }.reversed()
            ammoLoaded()
        }

        Glide.with(this).load("https://www.eftdb.one/static/item/full/${weapon.image}")
            .into(findViewById(R.id.weaponImage2))
    }

    private fun ammoLoaded() {
        ammoAdapter.updateData(ammoList)
    }

    private fun setupAdapters() {
        val ammoRV = findViewById<RecyclerView>(R.id.weaponDetailAmmoList)
        ammoRV.layoutManager = LinearLayoutManager(this)

        val loadoutRV = findViewById<RecyclerView>(R.id.weaponLoadoutRV)
        loadoutRV.layoutManager = LinearLayoutManager(this)

        ammoAdapter = SlimAdapter.create()
            .register<Ammo>(R.layout.item_ammo_small) { data, injector ->
                injector.text(R.id.ammoSmallName, data.name)
                injector.text(R.id.textView2, data.getSubtitle())
                injector.text(R.id.ammoSmallDamage, data.damage.toString())
                injector.text(R.id.ammoSmallPen, data.penetration.toString())
                injector.text(R.id.ammoSmallRecoil, data.recoil.toString())
                injector.text(R.id.ammoSmallACC, data.getAccuracyString())

                val subtitleTV = injector.findViewById<TextView>(R.id.textView2)

                if (data.getSubtitle().contains("\n")) {
                    if (data.prices?.isEmpty() == true) {
                        subtitleTV.maxLines = data.tradeups?.size ?: 1
                    } else {
                        subtitleTV.maxLines = data.prices?.size ?: 1
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

                when {
                    data.recoil == 0.0 -> {
                        injector.textColor(
                            R.id.ammoSmallRecoil,
                            resources.getColor(R.color.primaryText87)
                        )
                    }
                    data.recoil!! > 0 -> {
                        injector.textColor(
                            R.id.ammoSmallRecoil,
                            resources.getColor(R.color.md_red_A200)
                        )
                    }
                    data.recoil!! < 0 -> {
                        injector.textColor(
                            R.id.ammoSmallRecoil,
                            resources.getColor(R.color.md_green_A200)
                        )
                    }
                }

                when {
                    data.accuracy == 0.0 -> {
                        injector.textColor(
                            R.id.ammoSmallACC,
                            resources.getColor(R.color.primaryText87)
                        )
                    }
                    data.accuracy!! > 0 -> {
                        injector.textColor(
                            R.id.ammoSmallACC,
                            resources.getColor(R.color.md_green_A200)
                        )
                    }
                    data.accuracy!! < 0 -> {
                        injector.textColor(
                            R.id.ammoSmallACC,
                            resources.getColor(R.color.md_red_A200)
                        )
                    }
                }

                injector.longClicked(R.id.ammoSmallTop) {
                    MaterialDialog(this).show {
                        listItemsSingleChoice(
                            R.array.ammo_sort,
                            initialSelection = sortByIndex
                        ) { dialog, index, text ->
                            sortByIndex = index
                            sortUpdated()
                        }
                        title(text = "Sort Ammo By")
                    }
                    true
                }
            }.attachTo(ammoRV)

        loadoutAdapter = SlimAdapter.create().attachTo(loadoutRV)
            .register<Weapon.WeaponBuilds>(R.layout.item_weapon_loadout) { build, i ->
                Glide.with(this).load("https://www.eftdb.one/static/item/thumb/${build.image}")
                    .into(i.findViewById(R.id.weaponLoadoutImage))
                i.text(R.id.weaponLoadoutName, build.name)
                i.text(R.id.weaponLoadoutSubtitle, build.toString())
            }.updateData(weapon.builds)
    }

    private fun sortUpdated() {
        when (sortByIndex) {
            0 -> ammoAdapter.updateData(ammoList.sortedBy { it.name })
            1 -> ammoAdapter.updateData(ammoList.sortedByDescending { it.damage })
            2 -> ammoAdapter.updateData(ammoList.sortedByDescending { it.penetration })
            3 -> ammoAdapter.updateData(ammoList.sortedBy { it.recoil })
            4 -> ammoAdapter.updateData(ammoList.sortedByDescending { it.accuracy })
            5 -> ammoAdapter.updateData(ammoList.sortedByDescending { it.armor })
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.weaponDetailToolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        findViewById<Toolbar>(R.id.weaponDetailToolbar).setNavigationOnClickListener { super.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_weapon_detail, menu)
        if (weapon.wiki == null) {
            menu.findItem(R.id.weapon_wiki).isVisible = false
        }
        return true
    }
}