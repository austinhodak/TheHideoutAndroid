package com.austinhodak.thehideout.flea_market.detail

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentFleaItemBartersBinding
import com.austinhodak.thehideout.flea_market.models.Barter
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.getPrice
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.idik.lib.slimadapter.SlimAdapter
import java.lang.reflect.Type

private const val ARG_PARAM1 = "id"

class FleaItemBartersFragment : Fragment() {

    private var _binding: FragmentFleaItemBartersBinding? = null
    private val binding get() = _binding!!

    private val fleaViewModel: FleaViewModel by activityViewModels()
    private var fleaItem: FleaItem? = null
    private var itemID: String? = null
    private var fleaItems: MutableList<FleaItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemID = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFleaItemBartersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.itemBarterRV.layoutManager = LinearLayoutManager(requireContext())
        val adapter = SlimAdapter.create().attachTo(binding.itemBarterRV).register<Barter>(R.layout.item_list_barter) { barter, i ->
            val rewardFleaItem = fleaItems.find { it.bsgId == barter.rewardItems.first().item.id }
            Glide.with(this).load(barter.rewardItem().item.iconLink).into(i.findViewById(R.id.craftIcon))

            i.text(R.id.craftName, barter.rewardItem().item.name)
            i.text(R.id.craftTime, barter.source)

            i.text(R.id.craftOutputName, "${rewardFleaItem?.shortName}:")
            i.text(R.id.craftOutputPrice, rewardFleaItem?.getCurrentPrice())

            var totalCost: Long = barter.requiredItems.sumOf { b ->
                val requiredFleaItem = fleaItems.find { it.bsgId == b.item.id }
                requiredFleaItem?.totalNumber(b.count) ?: 0
            }

            i.text(R.id.craftOutputCost, "-${totalCost.getPrice("₽")}")
            i.text(R.id.craftTotalProfit, (rewardFleaItem?.price?.minus(totalCost))?.getPrice("₽"))

            i.findViewById<RecyclerView>(R.id.barterInputRV).layoutManager = LinearLayoutManager(requireContext())
            SlimAdapter.create().attachTo(i.findViewById(R.id.barterInputRV)).register<Barter.RequiredItem>(R.layout.item_hideout_crafting_requirement) { requiredItem, i2 ->
                val requiredFleaItem = fleaItems.find { it.bsgId == requiredItem.item.id }
                totalCost += requiredFleaItem?.totalNumber(requiredItem.count)?.toInt() ?: 0

                Glide.with(this).load(requiredItem.item.iconLink).into(i2.findViewById(R.id.craftInputIcon))

                i2.text(R.id.craftInputName, "x${requiredItem.count} ${requiredFleaItem?.shortName}")
                i2.text(R.id.craftInputPrice, "${requiredFleaItem?.total(requiredItem.count)}")

                val craftInputName = i2.findViewById<TextView>(R.id.craftInputName)
                if (itemID == requiredFleaItem?.uid) {
                    craftInputName.setTypeface(craftInputName.typeface, Typeface.BOLD)
                } else {
                    craftInputName.setTypeface(craftInputName.typeface, Typeface.NORMAL)
                }

                i2.clicked(R.id.barterRequireItem) {
                    startActivity(Intent(requireContext(), FleaItemDetailActivity::class.java).apply {
                        putExtra("id", requiredFleaItem?.uid)
                    })
                }
            }.updateData(barter.requiredItems)

            val card = i.findViewById<MaterialCardView>(R.id.barterCard)
            if (itemID == rewardFleaItem?.uid) {
                card.strokeWidth = 1
                card.strokeColor = resources.getColor(R.color.md_grey_700)

                card.setOnClickListener(null)
            } else {
                card.strokeWidth = 0

                i.clicked(R.id.barterCard) {
                    startActivity(Intent(requireContext(), FleaItemDetailActivity::class.java).apply {
                        putExtra("id", rewardFleaItem?.uid)
                    })
                }
            }
        }

        fleaViewModel.fleaItems.observe(viewLifecycleOwner) { items ->
            fleaItems = items.toMutableList()
            val bartersNeeded = getBarters().filter { it.isNeededForAny(items.find { it.uid == itemID }?.bsgId ?: "") }.sortedBy { b ->
                items.find { it.bsgId == b.rewardItem().item.id }?.uid != itemID
            }
            adapter.updateData(bartersNeeded)
        }
    }

    private fun getBarters(): List<Barter> {
        val groupListType: Type = object : TypeToken<ArrayList<Barter?>?>() {}.type
        return Gson().fromJson(resources.openRawResource(R.raw.barters).bufferedReader().use { it.readText() }, groupListType)
    }
}