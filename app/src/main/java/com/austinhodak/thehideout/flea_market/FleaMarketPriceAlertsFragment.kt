package com.austinhodak.thehideout.flea_market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentFleaPriceAlertBinding
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.logScreen
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.austinhodak.thehideout.viewmodels.models.PriceAlert
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

class FleaMarketPriceAlertsFragment : Fragment() {

    lateinit var mAdapter: SlimAdapter
    private val viewModel: FleaViewModel by activityViewModels()

    private var _binding: FragmentFleaPriceAlertBinding? = null
    private val binding get() = _binding!!

    private var mShowingDialog: MaterialDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFleaPriceAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("FleaMarketPriceAlertsFragment")
    }

    private fun setupAdapter() {
        val dialogAdapter = SlimAdapter.create().register<PriceAlert>(R.layout.dialog_list_item_1) { alert, i ->
            i.text(R.id.itemText, "Delete")
            i.image(R.id.itemIcon, R.drawable.ic_baseline_delete_24)

            i.clicked(R.id.itemTop) {
                alert.reference?.removeValue()
                mShowingDialog?.dismiss()
            }
        }

        mAdapter = SlimAdapter.create().register<PriceAlert>(R.layout.flea_market_alert_item) { alert, i ->
            val item = viewModel.getItemById(alert.itemID!!)

            val icon = i.findViewById<ImageView>(R.id.fleaItemIcon)
            Glide.with(this).load(item?.getItemIcon()).into(icon)

            i.text(R.id.fleaItemName, item?.name)
            i.text(R.id.fleaItemSubtitle, alert.getWhenText())
            i.text(R.id.fleaItemPrice, alert.price?.getPrice("₽"))
            i.text(R.id.fleaItemPriceSlot, "Current: ${item?.getCurrentPrice()}")

            i.longClicked(R.id.itemCard) {
                MaterialDialog(requireActivity()).show {
                    customListAdapter(dialogAdapter)
                    mShowingDialog = this
                }
                dialogAdapter.updateData(mutableListOf(alert))
                false
            }

        }.attachTo(binding.fleaList)
        binding.fleaList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.priceAlerts.observe(viewLifecycleOwner) {
            mAdapter.updateData(it)
            binding.priceAlertsEmptyLayout.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        (activity as MainActivity).isSearchHidden(true)
    }

}