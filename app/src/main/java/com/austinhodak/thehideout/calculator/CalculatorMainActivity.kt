package com.austinhodak.thehideout.calculator

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.Body
import com.austinhodak.thehideout.calculator.models.Part
import com.austinhodak.thehideout.calculator.pickers.CalculatorPickerActivity
import com.austinhodak.thehideout.clothing.armor.ArmorHelper
import com.austinhodak.thehideout.clothing.models.Armor
import com.austinhodak.thehideout.databinding.ActivityCalculatorMainBinding
import com.austinhodak.thehideout.databinding.BottomSheetCalculatorMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CalculatorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculatorMainBinding
    private lateinit var bottomBinding: BottomSheetCalculatorMainBinding
    private var body = Body()

    private lateinit var selectedAmmo: Ammo
    private var selectedHelmet: Armor? = null
    private var selectedChestArmor: Armor?  = null

    private lateinit var ammoViewModel: AmmoViewModel

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            if (result.data?.hasExtra("ammoID") == true) {
                selectedAmmo = ammoViewModel.ammoList.value?.find { it.prices?.firstOrNull { it._id == result.data?.getStringExtra("ammoID") } != null }!!
                updateBottomSheet()
            }
            if (result.data?.hasExtra("helmetID") == true) {
                selectedHelmet = ArmorHelper.getArmors(this).find { it._id == result.data?.getStringExtra("helmetID") }
                updateBottomSheet()
            }
            if (result.data?.hasExtra("chestID") == true) {
                selectedChestArmor = ArmorHelper.getArmors(this).find { it._id == result.data?.getStringExtra("chestID") }
                updateBottomSheet()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            bottomBinding = it.bottomSheet
        }

        setupToolbar()

        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)

        val bs = BottomSheetBehavior.from(findViewById<ConstraintLayout>(R.id.bottomSheet))
        bs.skipCollapsed = true

        binding.floatingActionButton.setOnClickListener {
            if (bs.state == BottomSheetBehavior.STATE_HIDDEN || bs.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bs.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bs.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        body.linkToHealthBar(Part.HEAD, binding.healthHead)
        body.linkToHealthBar(Part.THORAX, binding.healthThorax)
        body.linkToHealthBar(Part.STOMACH, binding.healthStomach)
        body.linkToHealthBar(Part.LEFTARM, binding.healthLArm)
        body.linkToHealthBar(Part.RIGHTARM, binding.healthRArm)
        body.linkToHealthBar(Part.LEFTLEG, binding.healthLLeg)
        body.linkToHealthBar(Part.RIGHTLEG, binding.healthRLeg)

        ammoViewModel.ammoList.observe(this) {
            selectedAmmo = it.find { it.prices?.firstOrNull { it._id == "5f4a52549f319f4528ac3646" } != null }!!
            updateBottomSheet()
            updateDurabilities()
        }

        selectedHelmet = ArmorHelper.getArmors(this).find { it._id == "5f4a52549f319f4528ac3760" }
        selectedChestArmor = ArmorHelper.getArmors(this).find { it._id == "5f4a52549f319f4528ac3758" }

        selectedHelmet = null
        selectedChestArmor = null
        updateDurabilities()
        updateBottomSheet()

        binding.healthHead.setOnClickListener {
            body.shoot(Part.HEAD, selectedAmmo.getCAmmo(), selectedHelmet?.getArmor())
        }

        binding.healthThorax.setOnClickListener {
            body.shoot(Part.THORAX, selectedAmmo.getCAmmo(), selectedChestArmor?.getArmor())
        }

        binding.healthStomach.setOnClickListener {
            body.shoot(Part.STOMACH, selectedAmmo.getCAmmo(), selectedChestArmor?.getArmor())
        }

        binding.healthLArm.setOnClickListener {
            body.shoot(Part.LEFTARM, selectedAmmo.getCAmmo())
        }

        binding.healthRArm.setOnClickListener {
            body.shoot(Part.RIGHTARM, selectedAmmo.getCAmmo())
        }

        binding.healthLLeg.setOnClickListener {
            body.shoot(Part.LEFTLEG, selectedAmmo.getCAmmo())
        }

        binding.healthRLeg.setOnClickListener {
            body.shoot(Part.RIGHTLEG, selectedAmmo.getCAmmo())
        }

        body.bindTotalTextView(binding.healthTotal)
        body.bindCurrentTextView(binding.healthCurrentTV)

        body.onShootListener = {
            updateDurabilities()
        }

        binding.bottomSheet.calcAmmoCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.AMMO)
        }
        binding.bottomSheet.calcHelmetCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.HELMET)
        }
        binding.bottomSheet.calcChestCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.CHEST)
        }
    }

    private fun launchPicker(itemType: CalculatorPickerActivity.ItemType) {
        val intent = Intent(this, CalculatorPickerActivity::class.java)
        intent.putExtra("itemType", itemType)
        resultLauncher.launch(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun updateDurabilities() {
        binding.calcArmorDurabliltyCard.visibility = if (selectedHelmet == null && selectedChestArmor == null) View.GONE else View.VISIBLE

        var text = ""
        text += "${selectedHelmet?.getArmor()?.durability ?: 0}/${selectedHelmet?.getArmor()?.maxDurability ?: 0}"
        text += "\n${selectedChestArmor?.getArmor()?.durability ?: 0}/${selectedChestArmor?.getArmor()?.maxDurability ?: 0}"
        binding.calcDurabilitiesTV.text = text

        binding.calcDurabilitiesArmorNamesTV.text = "${selectedHelmet?.name ?: "Helmet"}: \n${selectedChestArmor?.name ?: "Chest"}: "
    }

    private fun updateBottomSheet() {
        if (selectedHelmet == null) {
            bottomBinding.calcHelmetName.text = getString(R.string.helmet_none)
            bottomBinding.calcHelmetSubtitle.text = getString(R.string.helmet_select)
        } else {
            bottomBinding.calcHelmetName.text = "${selectedHelmet?.name} • Class ${selectedHelmet?.level}"
            bottomBinding.calcHelmetSubtitle.text = selectedHelmet?.zones?.joinToString(separator = ", ")
        }

        if (selectedChestArmor == null) {
            bottomBinding.calcChestTitle.text = getString(R.string.armor_chest_none)
            bottomBinding.calcChestSubtitle.text = getString(R.string.armor_chest_select)
        } else {
            bottomBinding.calcChestTitle.text = "${selectedChestArmor?.name} • Class ${selectedChestArmor?.level}"
            bottomBinding.calcChestSubtitle.text = selectedChestArmor?.zones?.joinToString(separator = ", ")
        }

        if (this::selectedAmmo.isInitialized) {
            val caliber = AmmoHelper.getCaliberByID(selectedAmmo.caliber)
            bottomBinding.calcAmmoTitle.text = "${selectedAmmo.name} • ${caliber?.longName}"
            bottomBinding.calcAmmoSubtitle.text = "Damage: ${selectedAmmo.damage} • Armor Damage: ${selectedAmmo.armor_damage} • Penetration: ${selectedAmmo.penetration}"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_damage_calculator, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reset -> {
                body.reset()
                selectedHelmet?.resetDurability()
                selectedChestArmor?.resetDurability()
                updateDurabilities()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}