package com.austinhodak.thehideout.flea_market.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentFleaItemBartersBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.quests.models.QuestNew
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.idik.lib.slimadapter.SlimAdapter
import java.lang.reflect.Type

private const val ARG_PARAM1 = "id"

class FleaItemQuestsFragment : Fragment() {

    private var _binding: FragmentFleaItemBartersBinding? = null
    private val binding get() = _binding!!

    private val fleaViewModel: FleaViewModel by activityViewModels()
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
        val adapter = SlimAdapter.create().attachTo(binding.itemBarterRV).register<QuestNew>(R.layout.item_flea_quest) { quest, i ->
            val itemID = fleaItems.find { it.uid == itemID }?.bsgId ?: ""
            val item2 = quest.objectives.find { it.targetItem?.id == itemID }

            i.text(R.id.itemQuestName, quest.title)
            i.text(R.id.itemQuestSubtitle, "Need ${item2?.number}")

            if (item2?.type == "find") {
                i.visible(R.id.itemQuestFIRIcon)
            } else {
                i.gone(R.id.itemQuestFIRIcon)
            }

            i.image(R.id.itemQuestIcon, quest.getTraderIcon())

            i.clicked(R.id.questDetailTopCard) {
                Toast.makeText(requireContext(), "Coming soon.", Toast.LENGTH_SHORT).show()
            }
        }

        fleaViewModel.fleaItems.observe(viewLifecycleOwner) { items ->
            fleaItems = items.toMutableList()
            val questsNeeded = getQuests().filter {
                it.needsItem(items.find { it.uid == itemID }?.bsgId ?: "")
            }
            adapter.updateData(questsNeeded)
        }
    }

    private fun getQuests(): List<QuestNew> {
        val groupListType: Type = object : TypeToken<ArrayList<QuestNew?>?>() {}.type
        return Gson().fromJson(resources.openRawResource(R.raw.quests_new).bufferedReader().use { it.readText() }, groupListType)
    }
}