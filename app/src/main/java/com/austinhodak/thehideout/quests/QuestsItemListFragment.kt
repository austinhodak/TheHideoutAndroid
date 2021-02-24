package com.austinhodak.thehideout.quests

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Maps
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.quests.viewmodels.QuestsViewModel
import com.google.android.material.card.MaterialCardView
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class QuestsItemListFragment : Fragment() {

    private lateinit var adapter: SlimAdapter
    private lateinit var selectedMap: Maps
    private var questList: UserFB.UserFBQuests = UserFB.UserFBQuests()
    private val viewModel: QuestsViewModel by activityViewModels()
    private var objectivesList: UserFB.UserFBQuestObjectives = UserFB.UserFBQuestObjectives()
    private var chipSelected = R.id.chip_active

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedMap = it.getSerializable(ARG_PARAM1) as Maps
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trader_quest_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.trader_rv)
        recyclerView.layoutManager = linearLayoutManager

        adapter = SlimAdapter.create().register<Objective>(R.layout.item_quest_items) { item, i ->
            i.text(R.id.itemName, item.objective.target)
            i.text(R.id.itemSubtitle, "${item.quest.giver} (${item.quest.title})")

            i.text(R.id.itemCount, "${item.objective.getCount(objectivesList)}/${item.objective.number}")

            val card = i.findViewById<MaterialCardView>(R.id.itemCard)

            if (item.objective.isCompleted(objectivesList)) {
                card.strokeColor = Color.parseColor("#994CAF50")
            } else {
                card.strokeColor = resources.getColor(R.color.md_grey_850)
            }

            i.clicked(R.id.itemLeftIcon) {
                item.objective.decrement(objectivesList)
            }

            i.clicked(R.id.itemRightIcon) {
                item.objective.increment(objectivesList)
            }

        }.attachTo(recyclerView)

        viewModel.completedObjectives.observe(requireActivity(), { obj ->
            objectivesList = obj ?: UserFB.UserFBQuestObjectives()
            adapter.notifyDataSetChanged()
        })

        showAllItems()

        /*val chips = (requireActivity() as MainActivity).getQuestChips()
        chipSelected = chips.checkedChipId
        chipSelected()
        chips.setOnCheckedChangeListener { group, checkedId ->
            chipSelected = checkedId
            chipSelected()
        }*/
    }

    /*private fun chipSelected() {
        when (chipSelected) {
            R.id.chip_all -> {
                showAllQuests()
            }
            R.id.chip_locked -> {
                adapter.updateData(QuestsHelper.getLockedQuests(map = selectedMap, quests = questList))
            }
            R.id.chip_active -> {
                adapter.updateData(QuestsHelper.getActiveQuests(map = selectedMap, quests = questList))
            }
            R.id.chip_completed -> {
                adapter.updateData(QuestsHelper.getCompletedQuests(map = selectedMap, quests = questList))
            }
            else -> adapter.updateData(QuestsHelper.getQuests(activity).filter { it.giver == "Fence"})
        }

        if (adapter.itemCount == 0) {
            view?.findViewById<TextView>(R.id.empty)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.empty)?.visibility = View.GONE
        }
    }*/

    private fun showAllItems() {
        val list: MutableList<Any> = ArrayList()
        for (quest in QuestsHelper.getQuests(requireContext())) {
            for (obj in quest.objectives) {
                if (obj.type == "collect" || obj.type == "find") {
                    list.add(Objective(
                        quest,
                        obj
                    ))
                }
            }
        }
        adapter.updateData(list)
        //adapter.updateData(QuestsHelper.getQuests(activity).filter { it.getLocation().contains(selectedMap.id, true) })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Maps) =
            QuestsItemListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }

    data class Objective (
        var quest: Quest,
        var objective: Quest.QuestObjectives
    )
}