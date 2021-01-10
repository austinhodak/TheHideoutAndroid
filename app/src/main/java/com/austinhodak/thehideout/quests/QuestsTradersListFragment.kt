package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.quests.models.Traders
import com.austinhodak.thehideout.viewmodels.QuestsViewModel
import com.austinhodak.thehideout.views.QuestObjective
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class QuestsTradersListFragment : Fragment() {


    private lateinit var adapter: SlimAdapter
    private lateinit var selectedTrader: Traders
    private var questList: UserFB.UserFBQuests = UserFB.UserFBQuests()
    private val sharedViewModel: QuestsViewModel by activityViewModels()
    private var objectivesList: UserFB.UserFBQuestObjectives? = null
    private var chipSelected = R.id.chip_active

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedTrader = it.getSerializable(ARG_PARAM1) as Traders
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

        adapter = SlimAdapter.create().register<Quest>(R.layout.quest_list_item_1) { quest, i ->
            val taskRV = i.findViewById<RecyclerView>(R.id.questTaskListRV)
            taskRV.layoutManager = LinearLayoutManager(requireActivity())

            SlimAdapter.create().register<Quest.QuestObjectives>(R.layout.quest_task_item_1) { objective, i ->
                val objectiveView = i.findViewById<QuestObjective>(R.id.questOb)
                objectiveView.setObjective(objective, objectivesList)
            }.attachTo(taskRV).updateData(quest.objectives)

            i.text(R.id.questItemTitle, quest.title)
            i.text(R.id.questItemSubtitle, quest.getLocation())
            i.text(R.id.questItemTertiary, "Level: ${quest.require.level}")

        }.attachTo(recyclerView)

        sharedViewModel.quests.observe(requireActivity(), { quests ->
            questList = quests
            chipSelected()
        })

        sharedViewModel.objectives.observe(requireActivity(), { obj ->
            objectivesList = obj
            chipSelected()
        })

        val chips = (requireActivity() as MainActivity).getQuestChips()
        chipSelected = chips.checkedChipId
        chipSelected()
        chips.setOnCheckedChangeListener { group, checkedId ->
            chipSelected = checkedId
            chipSelected()
        }
    }

    private fun chipSelected() {
        when (chipSelected) {
            R.id.chip_all -> {
                showAllQuests()
            }
            R.id.chip_locked -> {
                adapter.updateData(QuestsHelper.getLockedQuests(selectedTrader, questList))
            }
            R.id.chip_active -> {
                adapter.updateData(QuestsHelper.getActiveQuests(selectedTrader, questList))
            }
            R.id.chip_completed -> {
                adapter.updateData(QuestsHelper.getCompletedQuests(selectedTrader, questList))
            }
            else -> adapter.updateData(QuestsHelper.getQuests(activity).filter { it.giver == "Fence"})
        }

        if (adapter.itemCount == 0) {
            view?.findViewById<TextView>(R.id.empty)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.empty)?.visibility = View.GONE
        }
    }

    private fun showAllQuests() {
        adapter.updateData(QuestsHelper.getQuests(activity).filter { it.giver == selectedTrader.id })
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

    open class QuestItem : AbstractItem<QuestItem.ViewHolder>() {
        override val type: Int
            get() = R.id.fast_adapter_id

        override val layoutRes: Int
            get() = R.layout.quest_list_item_1

        override fun getViewHolder(v: View): QuestItem.ViewHolder {
            return ViewHolder(v)
        }

        class ViewHolder(view: View) : FastAdapter.ViewHolder<QuestItem>(view) {
            override fun bindView(item: QuestItem, payloads: List<Any>) {

            }

            override fun unbindView(item: QuestItem) {

            }

        }
    }
}