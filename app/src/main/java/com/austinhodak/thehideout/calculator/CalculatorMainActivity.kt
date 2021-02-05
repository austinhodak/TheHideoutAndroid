package com.austinhodak.thehideout.calculator

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.Body
import com.austinhodak.thehideout.calculator.models.Part
import com.austinhodak.thehideout.clothing.armor.ArmorHelper
import com.austinhodak.thehideout.databinding.ActivityCalculatorMainNewBinding
import com.austinhodak.thehideout.databinding.CalculatorBottomSheetBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.Armor
import com.austinhodak.thehideout.viewmodels.models.firestore.FSAmmo
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CalculatorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculatorMainNewBinding
    private lateinit var bottomBinding: CalculatorBottomSheetBinding
    private var body = Body()

    private lateinit var selectedAmmo: FSAmmo
    private var selectedHelmet: Armor? = null
    private var selectedChestArmor: Armor?  = null

    private lateinit var ammoViewModel: AmmoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorMainNewBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            bottomBinding = it.bottomSheet
        }

        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)

        setSupportActionBar(binding.toolbar)

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
            selectedAmmo = it.find { it._id == "5f4a52549f319f4528ac3635" }!!
            updateBottomSheet()
            updateDurabilities()
        }

        selectedHelmet = ArmorHelper.getArmors(this).find { it._id == "5f4a52549f319f4528ac3760" }
        selectedChestArmor = ArmorHelper.getArmors(this).find { it._id == "5f4a52549f319f4528ac3750" }
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
    }

    private fun updateDurabilities() {
        binding.calcArmorDurabliltyCard.visibility = if (selectedHelmet == null && selectedChestArmor == null) View.GONE else View.VISIBLE

        var text = ""
        text += "${selectedHelmet?.getArmor()?.durability ?: 0}/${selectedHelmet?.getArmor()?.maxDurability ?: 0}"
        text += "\n${selectedChestArmor?.getArmor()?.durability ?: 0}/${selectedChestArmor?.getArmor()?.maxDurability ?: 0}"
        binding.calcDurabilitiesTV.text = text
    }

    private fun updateBottomSheet() {
        if (selectedHelmet == null) {
            bottomBinding.calcHelmetName.text = "No Helmet"
            bottomBinding.calcHelmetSubtitle.text = "Select a Helmet."
        } else {
            bottomBinding.calcHelmetName.text = selectedHelmet?.name
            bottomBinding.calcHelmetSubtitle.text = selectedHelmet?.zones?.joinToString(separator = ", ")
        }

        if (selectedChestArmor == null) {
            bottomBinding.calcChestTitle.text = "No Chest Armor"
            bottomBinding.calcChestSubtitle.text = "Select Chest Armor."
        } else {
            bottomBinding.calcChestTitle.text = selectedChestArmor?.name
            bottomBinding.calcChestSubtitle.text = selectedChestArmor?.zones?.joinToString(separator = ", ")
        }

        if (this::selectedAmmo.isInitialized) {
            bottomBinding.calcAmmoTitle.text = selectedAmmo.name
           /* ammoViewModel.data.observe(this) {
                bottomBinding.calcAmmoSubtitle.text = it?.find { it.ammo.find { it._id == selectedAmmo._id } != null }?.long_name
            }*/
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.damage_calculator_main, menu)
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