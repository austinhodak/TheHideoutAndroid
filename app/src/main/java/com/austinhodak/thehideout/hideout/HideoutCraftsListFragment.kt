@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.austinhodak.thehideout.hideout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.hideout.models.HideoutCraft
import com.austinhodak.thehideout.hideout.models.Input
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.austinhodak.thehideout.viewmodels.HideoutViewModel
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

class HideoutCraftsListFragment : Fragment() {

    private lateinit var mAdapter: SlimAdapter
    private lateinit var mRecyclerView: RecyclerView
    private val viewModel: HideoutViewModel by activityViewModels()
    private val fleaViewModel: FleaViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hideout_module_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        setupAdapter()
    }

    private fun setupRecyclerView(view: View) {
        mRecyclerView = view.findViewById(R.id.moduleList)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        mAdapter = SlimAdapter.create().register<HideoutCraft>(R.layout.hideout_craft_item) { craft, i ->
            val requirementRV = i.findViewById<RecyclerView>(R.id.craftInputRV)
            requirementRV.layoutManager = LinearLayoutManager(requireContext())

            var totalCost = 0
            SlimAdapter.create().attachTo(requirementRV).register<Input>(R.layout.hideout_crafting_item_requirement) { inputItem, rI ->
                val fleaItem = fleaViewModel.getItemById(inputItem.id)
                val inputIcon = rI.findViewById<ImageView>(R.id.craftInputIcon)
                Glide.with(this).load(fleaItem.getItemIcon()).into(inputIcon)
                totalCost += (fleaItem.price?.times(inputItem.qty)) ?: 0

                rI.text(R.id.craftInputName, "x${inputItem.qty} ${fleaItem.name}")
                rI.text(R.id.craftInputPrice, "${fleaItem.price?.times(inputItem.qty)?.getPrice("₽")}")
            }.updateData(craft.input)

            val fleaItemTop = fleaViewModel.getItemById(craft.output.first().id)
            val icon = i.findViewById<ImageView>(R.id.craftIcon)
            Glide.with(this).load(fleaItemTop.getItemIcon()).into(icon)

            i.text(R.id.craftName, fleaItemTop.name)
            i.text(R.id.craftOutputName, "${fleaItemTop.shortName} x${craft.output[0].qty}")

            i.text(R.id.craftOutputPrice, (fleaItemTop.price?.times(craft.output[0].qty)?.getPrice("₽")))

            i.text(R.id.craftOutputCost, totalCost.getPrice("₽"))

            i.text(R.id.craftOutputProfit, (fleaItemTop.price!! * craft.output[0].qty - totalCost).getPrice("₽"))

            val VO = fleaItemTop.basePrice!!.toDouble()
            val VR = fleaItemTop.price.toDouble()
            val Ti = 0.05
            val Tr = 0.05
            val Q = 1
            val PO = log10((VO / VR))
            val PR = log10((VR / VO))

            val PO4 = if (VO > VR) {
                Math.pow(4.0, PO.pow(1.08))
            } else {
                Math.pow(4.0, PO)
            }

            val PR4 = if (VR > VO) {
                Math.pow(4.0, PR.pow(1.08))
            } else {
                Math.pow(4.0, PR)
            }

            val tax = (VO * Ti * PO4 * Q + VR * Tr * PR4 * Q) * craft.output[0].qty

            i.text(R.id.craftFleaFea, tax.roundToInt().getPrice("₽"))

            i.text(R.id.craftTotalProfit, (fleaItemTop.price * craft.output[0].qty - totalCost - tax).roundToInt().getPrice("₽"))
            i.text(R.id.craftProfitHour, ((fleaItemTop.price * craft.output[0].qty - totalCost - tax) / craft.time).roundToInt().getPrice("₽"))

        }.attachTo(mRecyclerView).updateData(viewModel.craftsList.value)
    }
}