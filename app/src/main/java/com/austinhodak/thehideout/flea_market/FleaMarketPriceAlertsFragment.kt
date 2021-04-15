package com.austinhodak.thehideout.flea_market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentFleaPriceAlertBinding
import com.austinhodak.thehideout.flea_market.models.PriceAlert
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.logScreen
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.idik.lib.slimadapter.SlimAdapter

class FleaMarketPriceAlertsFragment : Fragment() {

    lateinit var mAdapter: SlimAdapter
    private val viewModel: FleaViewModel by activityViewModels()

    private var _binding: FragmentFleaPriceAlertBinding? = null
    private val binding get() = _binding!!

    private var mShowingDialog: MaterialDialog? = null

    lateinit var fastAdapter: FastAdapter<PriceAlert>
    lateinit var itemAdapter: ItemAdapter<PriceAlert>

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
        val dialogAdapter = SlimAdapter.create().register<PriceAlert>(R.layout.item_dialog_simple) { alert, i ->
            i.text(R.id.itemText, R.string.delete)
            i.image(R.id.itemIcon, R.drawable.ic_baseline_delete_24)

            i.clicked(R.id.itemTop) {
                alert.reference?.removeValue()
                mShowingDialog?.dismiss()
            }
        }

        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter)

        /*mAdapter = SlimAdapter.create().register<PriceAlert>(R.layout.flea_market_alert_item) { alert, i ->
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

        }.attachTo(binding.fleaList)*/

        binding.fleaList.layoutManager = LinearLayoutManager(requireContext())
        binding.fleaList.adapter = fastAdapter
        binding.fleaList.itemAnimator = SlideUpAlphaAnimator().apply {
            addDuration = 150
            removeDuration = 100
        }

        fastAdapter.onLongClickListener = { v, a, item, p ->
            MaterialDialog(requireActivity()).show {
                customListAdapter(dialogAdapter)
                mShowingDialog = this
            }
            dialogAdapter.updateData(mutableListOf(item))
            false
        }

        viewModel.priceAlerts.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                delay(25)
                updateData(it)
            }
            binding.priceAlertsEmptyLayout.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        (activity as MainActivity).isSearchHidden(true)
    }

    fun updateData(list: List<PriceAlert>) {
        for (i in list) {
            i.fleaItem = viewModel.getItemById(i.itemID!!)
        }
        itemAdapter.add(list)
    }
}