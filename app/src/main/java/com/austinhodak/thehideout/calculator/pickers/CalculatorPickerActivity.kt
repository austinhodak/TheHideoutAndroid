package com.austinhodak.thehideout.calculator.pickers

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.austinhodak.thehideout.clothing.armor.ArmorHelper
import com.austinhodak.thehideout.databinding.ActivityCalculatorPickerBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.firestore.FSAmmo
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

    private val ammoViewModel: AmmoViewModel by viewModels()
    @Inject lateinit var armorHelper: ArmorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorPickerBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        setupAdapter()
        handleIntent()
    }

    private fun setupAdapter() {
        ammoAdapter = ItemAdapter()
        helmetAdapter = ItemAdapter()
        chestAdapter = ItemAdapter()
        adapter = FastAdapter.with(listOf(ammoAdapter, helmetAdapter, chestAdapter))
        binding.calculatorPickerRV.adapter = adapter
    }

    private fun loadData(itemType: ItemType) {
        when (itemType) {
            ItemType.AMMO -> {

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
        } else {
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    enum class ItemType {
        AMMO,
        HELMET,
        CHEST
    }
}