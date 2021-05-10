package com.austinhodak.thehideout.calculator.pickers

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.models.Ammo
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.calculator.models.Character
import com.austinhodak.thehideout.clothing.armor.ArmorHelper
import com.austinhodak.thehideout.clothing.models.Armor
import com.austinhodak.thehideout.databinding.ActivityCalculatorPickerBinding
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.SuggestionModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import net.idik.lib.slimadapter.SlimAdapter

@AndroidEntryPoint
class CalculatorPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorPickerBinding
    private var searchItem: MenuItem? = null
    private lateinit var adapter: FastAdapter<*>
    private lateinit var ammoAdapter: ItemAdapter<Ammo>
    private lateinit var helmetAdapter: ItemAdapter<Armor>
    private lateinit var chestAdapter: ItemAdapter<Armor>
    private lateinit var armorAdapter: ItemAdapter<Armor>
    private lateinit var characterAdapter: ItemAdapter<Character>

    private lateinit var mSearchAdapter: SlimAdapter

    private lateinit var itemType: ItemType

    private val ammoViewModel: AmmoViewModel by viewModels()
    private var armorHelper = ArmorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorPickerBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        setupToolbar()
        setupAdapter()
        handleIntent()
    }

    private fun setupAdapter() {
        ammoAdapter = ItemAdapter()
        helmetAdapter = ItemAdapter()
        chestAdapter = ItemAdapter()
        characterAdapter = ItemAdapter()
        armorAdapter = ItemAdapter()
        adapter = FastAdapter.with(listOf(ammoAdapter, helmetAdapter, chestAdapter, characterAdapter, armorAdapter))

        binding.calculatorPickerRV.layoutManager = LinearLayoutManager(this)
        binding.calculatorPickerRV.adapter = adapter

        adapter.onClickListener = { view, adapter, item, pos ->
            //Toast.makeText(this, "Selected: $pos", Toast.LENGTH_SHORT).show()
            val intent = Intent()
            when(item) {
                is Ammo -> {
                    intent.putExtra("ammoID", item._id)
                }
                is Armor -> {
                    if (item.`class` == "Helmet") {
                        intent.putExtra("helmetID", item._id)
                        intent.putExtra("armorID", item._id)
                    } else {
                        intent.putExtra("chestID", item._id)
                        intent.putExtra("armorID", item._id)
                    }
                }
                is Character -> {
                    intent.putExtra("character", item.name)
                }
            }
            setResult(RESULT_OK, intent)
            finish()
            false
        }

        mSearchAdapter = SlimAdapter.create()
        binding.searchView.setAdapter(mSearchAdapter)

        val list: MutableList<SuggestionModel> = ArrayList()
        binding.searchView.setSuggestions(list)

        binding.searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                ammoAdapter.filter(newText)
                ammoAdapter.itemFilter.filterPredicate = { ammo: Ammo, constraint: CharSequence? ->
                    ammo.name?.contains(constraint ?: "", true) ?: false
                }

                helmetAdapter.filter(newText)
                helmetAdapter.itemFilter.filterPredicate = { item: Armor, constraint: CharSequence? ->
                    item.name.contains(constraint ?: "", true)
                }

                chestAdapter.filter(newText)
                chestAdapter.itemFilter.filterPredicate = { item: Armor, constraint: CharSequence? ->
                    item.name.contains(constraint ?: "", true)
                }

                armorAdapter.filter(newText)
                armorAdapter.itemFilter.filterPredicate = { item: Armor, constraint: CharSequence? ->
                    item.name.contains(constraint ?: "", true)
                }

                characterAdapter.filter(newText)
                characterAdapter.itemFilter.filterPredicate = { item: Character, constraint: CharSequence? ->
                    item.name.contains(constraint ?: "", true)
                }
                return true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        searchItem = menu.findItem(R.id.main_search)
        binding.searchView.setMenuItem(searchItem)
        return true
    }

    private fun loadData(itemType: ItemType) {
        this.itemType = itemType
        when (itemType) {
            ItemType.AMMO -> {
                ammoAdapter.add(ammoViewModel.ammoList.value?.sortedBy { it.name } ?: emptyList())
            }
            ItemType.HELMET -> {
                helmetAdapter.setNewList(armorHelper.getArmors(this).filter { it.`class` == "Helmet" }.sortedWith(compareBy({ it.level }, { it.name })).map {
                    Armor(
                        it.description,
                        it.weight,
                        it.level,
                        it.hitpoints,
                        it.movement,
                        it.turn,
                        it.ergonomics,
                        it.zones,
                        it._id,
                        it.name,
                        it.image,
                        it.material,
                        it.`class`,
                        it.ricochet,
                        it.grid,
                        it.prices,
                        it.tradeups,
                        it.fields,
                        it.internal,
                        it.blunt,
                        it.resistance,
                        it.destructibility,
                        it.cArmor
                    )
                })
            }
            ItemType.CHEST -> {
                chestAdapter.add(armorHelper.getArmors(this).filter { it.`class` == "Chest Rig" || it.`class` == "Body Armor" }.sortedWith(compareBy({ it.level }, { it.name })).map {
                    Armor(
                        it.description,
                        it.weight,
                        it.level,
                        it.hitpoints,
                        it.movement,
                        it.turn,
                        it.ergonomics,
                        it.zones,
                        it._id,
                        it.name,
                        it.image,
                        it.material,
                        it.`class`,
                        it.ricochet,
                        it.grid,
                        it.prices,
                        it.tradeups,
                        it.fields,
                        it.internal,
                        it.blunt,
                        it.resistance,
                        it.destructibility,
                        it.cArmor
                    )
                })
            }
            ItemType.CHARACTER -> {
                characterAdapter.add(CalculatorHelper.getCharacters(this).map {
                    Character(
                        it.name,
                        it.health,
                        it.image,
                        it.c_type,
                        it.spawn_chance
                    )
                })
            }
            ItemType.ARMOR -> {
                armorAdapter.add(armorHelper.getArmors(this).filterNot { it.hitpoints <= 0 }.sortedWith(compareBy({ it.level }, { it.name })).map {
                    Armor(
                        it.description,
                        it.weight,
                        it.level,
                        it.hitpoints,
                        it.movement,
                        it.turn,
                        it.ergonomics,
                        it.zones,
                        it._id,
                        it.name,
                        it.image,
                        it.material,
                        it.`class`,
                        it.ricochet,
                        it.grid,
                        it.prices,
                        it.tradeups,
                        it.fields,
                        it.internal,
                        it.blunt,
                        it.resistance,
                        it.destructibility,
                        it.cArmor
                    )
                })
            }
        }
    }

    private fun handleIntent() {
        if (intent == null) return
        if (intent.hasExtra("itemType")) {
            loadData(intent.getSerializableExtra("itemType") as ItemType)
            setupToolbar()
        } else {
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
        binding.toolbar.setNavigationOnClickListener { super.onBackPressed() }

        if (this::itemType.isInitialized) {
            binding.toolbar.title = when (itemType) {
                ItemType.AMMO -> "Select Ammo"
                ItemType.HELMET -> "Select Helmet"
                ItemType.CHEST -> "Select Chest Armor"
                ItemType.CHARACTER -> "Select Character"
                ItemType.ARMOR -> "Select Armor"
            }
        }
    }

    enum class ItemType {
        AMMO,
        HELMET,
        CHEST,
        CHARACTER,
        ARMOR
    }
}