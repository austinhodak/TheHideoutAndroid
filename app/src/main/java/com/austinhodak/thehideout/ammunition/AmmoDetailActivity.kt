package com.austinhodak.thehideout.ammunition

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.models.Ammo
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.calculator.pickers.CalculatorPickerActivity
import com.austinhodak.thehideout.clothing.armor.ArmorHelper
import com.austinhodak.thehideout.clothing.models.Armor
import com.austinhodak.thehideout.databinding.ActivityAmmoDetailBinding
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetailActivity
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.log
import com.google.firebase.analytics.FirebaseAnalytics
import net.idik.lib.slimadapter.SlimAdapter
import timber.log.Timber

class AmmoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmmoDetailBinding
    private lateinit var fleaViewModel: FleaViewModel
    private lateinit var ammoViewModel: AmmoViewModel
    private var ammo: Ammo? = null

    private var selectedArmor: Armor?  = null

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            if (result.data?.hasExtra("armorID") == true) {
                selectedArmor = ArmorHelper.getArmors(this).find { it._id == result.data?.getStringExtra("armorID") }
                armorSelected()
                updatePenChance()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmmoDetailBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        ammoViewModel = ViewModelProvider(this).get(AmmoViewModel::class.java)
        fleaViewModel = ViewModelProvider(this).get(FleaViewModel::class.java)
        ammoViewModel.ammoList.observe(this) {
            if (intent.hasExtra("id")) {
                ammo = it.find { it._id == intent.getStringExtra("id") }
                updateData()
            }
        }

        setupToolbar()

        binding.ammoDetailArmorCard.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Armor Chart Legend")
                customView(R.layout.dialog_ammo_armor_chart)
                positiveButton(text = "Okay")
            }
        }

        binding.ammoDetailCalcFAB.setOnClickListener {
            startActivity(Intent(this, CalculatorMainActivity::class.java).apply {
                putExtra("ammoID", ammo?._id)
            })
        }

        binding.ammoDetailScollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY + 12 && binding.ammoDetailCalcFAB.isShown) {
                binding.ammoDetailCalcFAB.hide()
            }

            if (scrollY < oldScrollY - 12 && !binding.ammoDetailCalcFAB.isShown) {
                binding.ammoDetailCalcFAB.show()
            }
        }

        selectedArmor = ArmorHelper.getArmors(this).find { it.name == "Galvion Caiman" }
        armorSelected()
    }

    private fun armorSelected() {
        binding.calcChestTitle.text = "${selectedArmor?.name} • Class ${selectedArmor?.level}"
        binding.calcChestSubtitle.text = selectedArmor?.zones?.joinToString(separator = ", ")
        binding.textView24.text = selectedArmor?.hitpoints.toString()

        binding.ammoDetailArmorPenSeekbar.valueTo = selectedArmor?.hitpoints?.toFloat() ?: 0.0f
        binding.ammoDetailArmorPenSeekbar.value = selectedArmor?.hitpoints?.toFloat() ?: 0.0f
    }

    private fun updatePenChance(durability: Float? = null) {
        val chance = CalculatorHelper.penChance(
            ammo?.getCAmmo()!!,
            selectedArmor?.getCArmor(durability?.toDouble())!!
        )

        Timber.d(chance.toString())

        binding.textView23.text = "${String.format("%.2f", chance)}%"
    }

    private fun updateData() {
        getCaliberData()
        binding.ammo = ammo

        if (ammo?.prices?.isNotEmpty() == true) {
            SlimAdapter.create().register<Ammo.AmmoPriceModel>(R.layout.item_ammo_detail_trader_price) { price, i ->
                i.text(R.id.ammoDetailTraderPriceTitle, price.getTraderString())
                i.text(R.id.ammoDetailTraderPriceTV, price.getPrice())
            }.attachTo(binding.ammoDetailTraderPriceRV).updateData(ammo?.prices)
            binding.ammoDetailTraderPriceRV.layoutManager = LinearLayoutManager(this)
        } else {
            binding.ammoDetailSoldByCard.visibility = View.GONE
        }

        getFleaItemData()

        binding.ammoDetailArmorPenSeekbar.addOnChangeListener { slider, value, fromUser ->
            binding.ammoDetailPenChanceCurrentDurability.text = String.format("%.2f", value)
            updatePenChance(value)
        }

        updatePenChance()

        binding.calcChestCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.ARMOR)
        }
    }

    private fun getCaliberData() {
        binding.caliber = ammo?.caliber?.let { AmmoHelper.getCaliberByID(it) }
    }

    private fun getFleaItemData() {
        fleaViewModel.fleaItems.observe(this) {
            binding.fleaItem = ammo?.getFleaMarketItem(it).also { item ->
                if (item == null) return@also

                binding.ammoDetailFleaCard.setOnClickListener {
                    log(FirebaseAnalytics.Event.SELECT_ITEM, item.uid ?: "", item.name ?: "", "flea_item")
                    startActivity(Intent(this, FleaItemDetailActivity::class.java).apply {
                        putExtra("id", item.uid)
                    })
                }

                when {
                    item.diff24h!! > 0.0 -> {
                        binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.md_green_500))
                        binding.fleaDetail24HIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                        binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
                    }
                    item.diff24h < 0.0 -> {
                        binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.md_red_500))
                        binding.fleaDetail24HIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                        binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
                    }
                    else -> {
                        binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.primaryText60))
                        binding.fleaDetail24HIcon.setImageResource(R.drawable.icons8_horizontal_line_96)
                        binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText60))
                    }
                }

                when {
                    item.diff7days!! > 0.0 -> {
                        binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.md_green_500))
                        binding.fleaDetail7DIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                        binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
                    }
                    item.diff7days < 0.0 -> {
                        binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.md_red_500))
                        binding.fleaDetail7DIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                        binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
                    }
                    else -> {
                        binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.primaryText60))
                        binding.fleaDetail7DIcon.setImageResource(R.drawable.icons8_horizontal_line_96)
                        binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText60))
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.ammoDetailToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.ammoDetailToolbar.setNavigationOnClickListener { super.onBackPressed() }
    }

    private fun launchPicker(itemType: CalculatorPickerActivity.ItemType) {
        val intent = Intent(this, CalculatorPickerActivity::class.java)
        intent.putExtra("itemType", itemType)
        resultLauncher.launch(intent)
    }
}