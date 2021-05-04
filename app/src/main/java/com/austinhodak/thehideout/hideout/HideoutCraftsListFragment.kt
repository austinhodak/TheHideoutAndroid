
package com.austinhodak.thehideout.hideout

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.hideout.models.HideoutCraft
import com.austinhodak.thehideout.hideout.models.Input
import com.austinhodak.thehideout.hideout.viewmodels.HideoutViewModel
import com.austinhodak.thehideout.utils.getPrice
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter
import kotlin.math.abs
import kotlin.math.roundToInt

class HideoutCraftsListFragment : Fragment() {

    private var mList: List<HideoutCraft>? = null
    private lateinit var mAdapter: SlimAdapter
    private lateinit var mRecyclerView: RecyclerView
    private val viewModel: HideoutViewModel by activityViewModels()
    private val fleaViewModel: FleaViewModel by activityViewModels()
    private var sortBy = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_hideout_module_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        setupAdapter()

        (activity as MainActivity).isSearchHidden(false)
    }

    private fun setupRecyclerView(view: View) {
        mRecyclerView = view.findViewById(R.id.moduleList)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        val greenTextColor = resources.getColor(R.color.md_green_400)
        val redTextColor = resources.getColor(R.color.md_red_400)

        mAdapter = SlimAdapter.create().register<HideoutCraft>(R.layout.item_hideout_craft) { craft, i ->

            //Requirements
            val requirementRV = i.findViewById<RecyclerView>(R.id.craftInputRV)
            requirementRV.layoutManager = LinearLayoutManager(requireContext())

            SlimAdapter.create().attachTo(requirementRV).register<Input>(R.layout.item_hideout_crafting_requirement) { inputItem, rI ->
                val fleaItem = inputItem.fleaItem
                val inputIcon = rI.findViewById<ImageView>(R.id.craftInputIcon)
                Glide.with(this).load(fleaItem.getItemIcon()).into(inputIcon)

                rI.text(R.id.craftInputName, "x${inputItem.qty.toInt()} ${fleaItem.name}")
                rI.text(R.id.craftInputPrice, "${fleaItem.price?.times(inputItem.qty)?.roundToInt()?.getPrice("₽")}")
            }.updateData(craft.input)
            //Requirements

            val fleaItem = craft.output.first().fleaItem

            viewModel.getHideoutByID(craft.facility) {
                i.text(R.id.hideoutCraftItemModule, "${it.module.toUpperCase()} LEVEL ${it.level}")
            }

            //Craft Image
            Glide.with(this).load(fleaItem.getItemIcon()).into(i.findViewById(R.id.craftIcon))

            //Craft Name
            i.text(R.id.craftName, fleaItem.name)

            //Time to craft
            i.text(R.id.craftTime, craft.getTimeToCraft())

            //Craft Output Name + Qty
            i.text(R.id.craftOutputName, craft.getOutputName())

            //Craft Output Price
            i.text(R.id.craftOutputPrice, craft.getOutputPrice())

            //Craft Total Cost of Input Items
            val totalCostToCraft = craft.getTotalCostToCraft()

            //Total Cost Text
            i.text(R.id.craftOutputCost, totalCostToCraft.getPrice("₽"))

            //(Price of item * qty) - cost of items
            val profit = craft.getProfit()

            i.text(R.id.craftOutputProfit, profit.getPrice("₽"))

            i.textColor(R.id.craftOutputProfit, if (profit <= 0) redTextColor else greenTextColor)

            i.text(R.id.craftTotalProfit, craft.getTotalProfit().getPrice("₽"))
            i.text(R.id.craftProfitHour, craft.getProfitPerHour().getPrice("₽"))

            i.textColor(R.id.craftTotalProfit, if (craft.getTotalProfit() <= 0) redTextColor else greenTextColor)
            i.textColor(R.id.craftProfitHour, if (craft.getProfitPerHour() <= 0) redTextColor else greenTextColor)

            fleaItem.calculateTax {
                val tax = it * craft.output[0].qty
                i.text(R.id.craftFleaFea, (-abs(tax)).getPrice("₽"))
            }
        }.attachTo(mRecyclerView)

        updateData(mList = viewModel.craftsList.value?.map { craft ->
            craft.output.first().fleaItem = fleaViewModel.getItemById(craft.output.first().id) ?: return
            craft.input.map {
                it.fleaItem = fleaViewModel.getItemById(it.id) ?: return
            }
            craft
        })

        fleaViewModel.searchKey.observe(viewLifecycleOwner) { string ->
            updateData(searchKey = string)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_flea_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flea_sort -> {
                MaterialDialog(requireActivity()).show {
                    listItemsSingleChoice(R.array.crafts_sort, initialSelection = sortBy) { _, index, text ->
                        sortBy = index
                        updateData()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateData(searchKey: String = "", mList: List<HideoutCraft>? = null) {
        if (mList != null) this.mList = mList
        var nList = mList

        val filtered = this.mList?.filter { it.getOutputItem().name?.contains(searchKey, true) == true || it.getOutputItem().shortName?.contains(searchKey, true) == true }

        when (sortBy) {
            0 -> nList = filtered?.sortedBy { it.getOutputItem().name }?.toMutableList()
            1 -> nList = filtered?.sortedBy { it.getTotalProfit() }?.toMutableList()
            2 -> nList = filtered?.sortedByDescending { it.getTotalProfit() }?.toMutableList()
            3 -> nList = filtered?.sortedBy { it.getProfitPerHour() }?.toMutableList()
            4 -> nList = filtered?.sortedByDescending { it.getProfitPerHour() }?.toMutableList()
        }

        mAdapter.updateData(nList)
    }
}