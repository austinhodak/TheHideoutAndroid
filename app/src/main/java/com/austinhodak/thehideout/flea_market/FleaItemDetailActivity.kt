package com.austinhodak.thehideout.flea_market

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ActivityFleaItemDetailBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.austinhodak.thehideout.viewmodels.models.PriceAlertSmall
import com.google.android.material.textfield.TextInputEditText
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.text.DecimalFormat


class FleaItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFleaItemDetailBinding
    private var fleaItem: FleaItem? = null
    private lateinit var fleaViewModel: FleaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFleaItemDetailBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        setupToolbar()

        fleaViewModel = ViewModelProvider(this).get(FleaViewModel::class.java)
        fleaViewModel.fleaItems.observe(this) { list ->
            if (intent.getStringExtra("id").isNullOrEmpty()) return@observe
            fleaItem = list.find { it.uid == intent.getStringExtra("id")!! }
            updateItem(fleaItem)
        }
    }

    private fun updateItem(fleaItem: FleaItem?) {
        binding.item = fleaItem
        binding.fleaDetailSalePriceET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.fleaDetailListingFeeTV.text = "0₽"
                    binding.fleaDetailProfitTV.text = "0₽"
                } else {
                    fleaItem?.calculateTax(s.toString().replace(",", "").toLong()) {
                        binding.fleaDetailListingFeeTV.text = it.getPrice("₽")
                        binding.fleaDetailProfitTV.text = (s.toString().replace(",", "").toLong() - it).getPrice("₽")
                    }
                }

                binding.fleaDetailSalePriceET.removeTextChangedListener(this)

                try {
                    var givenstring = s.toString()
                    val longval: Long
                    if (givenstring.contains(",")) {
                        givenstring = givenstring.replace(",".toRegex(), "")
                    }
                    longval = givenstring.toLong()
                    val formatter = DecimalFormat("#,###,###")
                    val formattedString: String = formatter.format(longval)
                    binding.fleaDetailSalePriceET.setText(formattedString)
                    binding.fleaDetailSalePriceET.setSelection(binding.fleaDetailSalePriceET.text?.length ?: 0)
                    // to place the cursor at the end of text
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                binding.fleaDetailSalePriceET.addTextChangedListener(this)
            }
        })

        when {
            fleaItem?.diff24h!! > 0.0 -> {
                binding.fleaDetail24HTV.setTextColor(resources.getColor(R.color.md_green_500))
                binding.fleaDetail24HIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                binding.fleaDetail24HIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
            }
            fleaItem.diff24h < 0.0 -> {
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
            fleaItem.diff7days!! > 0.0 -> {
                binding.fleaDetail7DTV.setTextColor(resources.getColor(R.color.md_green_500))
                binding.fleaDetail7DIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                binding.fleaDetail7DIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
            }
            fleaItem.diff7days < 0.0 -> {
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

        setupPriceAlertList()
    }

    private fun setupPriceAlertList() {
        binding.fleaDetailPriceAlertRV.layoutManager = LinearLayoutManager(this)

        val itemAdapter = ItemAdapter<PriceAlertSmall>()
        val fastAdapter = FastAdapter.with(itemAdapter)
        binding.fleaDetailPriceAlertRV.adapter = fastAdapter

        fleaViewModel.priceAlerts.observe(this) { list ->
            val alerts = list.filter { it.itemID == fleaItem?.uid }.map {
                PriceAlertSmall(it.price, it.uid, it.itemID, it.`when`, it.token, it.reference)
            }
            itemAdapter.set(alerts)
        }

        binding.fleaDetailPriceAlertFAB.setOnClickListener {
            val alertDialog = MaterialDialog(this).show {
                title(text = "Add Price Alert")
                customView(R.layout.dialog_add_price_alert)
                positiveButton(text = "ADD") { dialog ->
                    val alertAddView = dialog.getCustomView()
                    val spinner = alertAddView.findViewById<AppCompatSpinner>(R.id.addAlertSpinner)
                    val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)

                    if (editText.text.toString().isEmpty()) {
                        editText.error = "Cannot be empty."
                    } else {
                        editText.error = null
                        fleaViewModel.addPriceAlert(spinner, editText, dialog, fleaItem!!)
                    }
                }
                negativeButton(text = "CANCEL") {
                    dismiss()
                }
                noAutoDismiss()
            }
            val alertAddView = alertDialog.getCustomView()
            val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    editText.removeTextChangedListener(this)

                    try {
                        var givenstring = s.toString()
                        val longval: Long
                        if (givenstring.contains(",")) {
                            givenstring = givenstring.replace(",".toRegex(), "")
                        }
                        longval = givenstring.toLong()
                        val formatter = DecimalFormat("#,###,###")
                        val formattedString: String = formatter.format(longval)
                        editText.setText(formattedString)
                        editText.setSelection(editText.text?.length ?: 0)
                        // to place the cursor at the end of text
                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    editText.addTextChangedListener(this)
                }
            })
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.weaponDetailToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.weaponDetailToolbar.setNavigationOnClickListener { super.onBackPressed() }
    }
}