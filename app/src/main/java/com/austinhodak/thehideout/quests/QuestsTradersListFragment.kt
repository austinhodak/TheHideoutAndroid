package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.quests.models.Traders
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class QuestsTradersListFragment : Fragment() {

    private lateinit var selectedTrader: Traders
    private var questList: List<Quest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedTrader = it.getSerializable(ARG_PARAM1) as Traders
        }

        questList = QuestsHelper.getQuests(requireContext()).filter { it.giver == selectedTrader.id }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        SlimAdapter.create().register<Quest>(R.layout.quest_list_item_1) { quest, i ->
            val taskRV = i.findViewById<RecyclerView>(R.id.questTaskListRV)
            taskRV.layoutManager = LinearLayoutManager(requireContext())
            SlimAdapter.create().register<Quest.QuestObjectives>(R.layout.quest_task_item_1) { objective, i ->
                i.text(R.id.questTaskText, objective.toString())
                i.image(R.id.questTaskIcon, objective.getIcon())
            }.attachTo(taskRV).updateData(quest.objectives)

            i.text(R.id.questItemTitle, quest.title)
            i.text(R.id.questItemSubtitle, quest.objectives.joinToString(", ") { it.location })
            i.text(R.id.questItemTertiary, "Level: ${quest.require.level}")

        }.attachTo(recyclerView).updateData(questList)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Traders) =
            QuestsTradersListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }
}