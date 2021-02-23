package com.austinhodak.thehideout.calculator.pickers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.clothing.armor.ArmorHelper
import com.austinhodak.thehideout.databinding.ActivityCalculatorPickerBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.firestore.FSAmmo
import com.austinhodak.thehideout.weapons.WeaponDetailActivity
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalculatorPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorPickerBinding

    private lateinit var adapter: FastAdapter<*>
    private lateinit var ammoAdapter: ItemAdapter<FSAmmo>
    private lateinit var helmetAdapter: ItemAdapter<FSAmmo>
    private lateinit var chestAdapter: ItemAdapter<FSAmmo>

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
        adapter = FastAdapter.with(listOf(ammoAdapter, helmetAdapter, chestAdapter))

        binding.calculatorPickerRV.layoutManager = LinearLayoutManager(this)
        binding.calculatorPickerRV.adapter = adapter

        adapter.onClickListener = { view, adapter, item, pos ->
            val intent = Intent()
            when(item) {
                is FSAmmo -> {
                    intent.putExtra("ammoID", item.prices?.first()?._id)
                }
            }
            setResult(RESULT_OK, intent)
            finish()
            false
        }
    }

    private fun loadData(itemType: ItemType) {
        this.itemType = itemType
        when (itemType) {
            ItemType.AMMO -> {
                ammoAdapter.add(ammoViewModel.ammoList.value?.sortedBy { it.name } ?: emptyList())
            }
            ItemType.HELMET -> {

            }
            ItemType.CHEST -> {

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
            }
        }
    }

    enum class ItemType {
        AMMO,
        HELMET,
        CHEST
    }
}