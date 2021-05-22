package com.austinhodak.thehideout.flea_market.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentFleaItemDetailBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.models.PriceAlertSmall
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.utils.getPrice
import com.google.android.material.textfield.TextInputEditText
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.text.DecimalFormat

private const val ARG_PARAM1 = "id"

class FleaItemDetailFragment : Fragment() {

    private var _binding: FragmentFleaItemDetailBinding? = null
    private val binding get() = _binding!!

    private val fleaViewModel: FleaViewModel by activityViewModels()
    private var fleaItem: FleaItem? = null
    private var itemID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemID = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFleaItemDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fleaViewModel.fleaItems.observe(viewLifecycleOwner) { list ->
            fleaItem = list.find { it.uid == itemID }
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
                    fleaItem?.calculateTax(s.toString().replace("[^0-9]".toRegex(), "").toLong()) {
                        binding.fleaDetailListingFeeTV.text = it.getPrice("₽")
                        binding.fleaDetailProfitTV.text = (s.toString().replace("[^0-9]".toRegex(), "").toLong() - it).getPrice("₽")
                    }
                }

                binding.fleaDetailSalePriceET.removeTextChangedListener(this)

                try {
                    var givenstring = s.toString()
                    if (givenstring.contains(",")) {
                        givenstring = givenstring.replace("[^0-9]".toRegex(), "")
                    }
                    val longval: Long = givenstring.toLong()
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

        //binding.fleaDetailSalePriceET.setText(fleaItem?.price.toString())

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
        binding.fleaDetailPriceAlertRV.layoutManager = LinearLayoutManager(requireContext())

        val itemAdapter = ItemAdapter<PriceAlertSmall>()
        val fastAdapter = FastAdapter.with(itemAdapter)
        binding.fleaDetailPriceAlertRV.adapter = fastAdapter

        fleaViewModel.priceAlerts.observe(viewLifecycleOwner) { list ->
            val alerts = list.filter { it.itemID == fleaItem?.uid }.map {
                PriceAlertSmall(it.price, it.uid, it.itemID, it.`when`, it.token, it.reference)
            }
            itemAdapter.set(alerts)
        }

        binding.fleaDetailPriceAlertFAB.setOnClickListener {
            val alertDialog = MaterialDialog(requireContext()).show {
                title(text = getString(R.string.price_alert_add))
                customView(R.layout.dialog_add_price_alert)
                positiveButton(text = getString(R.string.add)) { dialog ->
                    val alertAddView = dialog.getCustomView()
                    val spinner = alertAddView.findViewById<AppCompatSpinner>(R.id.addAlertSpinner)
                    val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)

                    if (editText.text.toString().isEmpty()) {
                        editText.error = getString(R.string.error_empty)
                    } else {
                        editText.error = null
                        fleaViewModel.addPriceAlert(spinner, editText, dialog, fleaItem!!)
                    }
                }
                negativeButton(text = getString(R.string.cancel)) {
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            FleaItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}