package com.austinhodak.thehideout.quests

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.quests.models.QuestOverviewItem
import com.austinhodak.thehideout.viewmodels.QuestsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.idik.lib.slimadapter.SlimAdapter

class QuestsOverviewFragment : Fragment() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: SlimAdapter
    private val sharedViewModel: QuestsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.quests_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById(R.id.questOverviewRV)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())



        val helper = QuestsHelper

        val data = mutableListOf(
            QuestOverviewItem(
                R.drawable.ic_baseline_assignment_turned_in_24,
                R.color.md_green_500,
                "Completed Quests",
                "Kappa",
                0,
                helper.totalQuests()
            ),
            QuestOverviewItem(
                R.drawable.icons8_sniper_96,
                R.color.md_red_500,
                "PMC Eliminations",
                "Includes special kill requirements",
                0,
                helper.totalPMCEliminations()
            ),
            QuestOverviewItem(
                R.drawable.icons8_target_96,
                R.color.md_orange_500,
                "Scav Eliminations",
                "Includes special kill requirements",
                0,
                helper.totalScavEliminations()
            ),
            QuestOverviewItem(
                R.drawable.ic_search_black_24dp,
                R.color.md_light_blue_500,
                "Quest Items",
                "Includes non find-in-raid requirements",
                0,
                helper.totalQuestItems()
            ),
            QuestOverviewItem(
                R.drawable.ic_baseline_check_circle_outline_24,
                R.color.md_light_blue_500,
                "Found in Raid Items",
                "Excludes items that can be bought from Flea or Vendors",
                0,
                helper.totalFIRItems()
            ),
            QuestOverviewItem(
                R.drawable.ic_baseline_swap_horizontal_circle_24,
                R.color.md_light_blue_500,
                "Handover Items",
                "Excludes items that need to be found in raid",
                0,
                helper.totalHandoverItems()
            ),
            QuestOverviewItem(
                R.drawable.icons8_low_importance_96,
                R.color.md_purple_500,
                "Placed Objectives",
                "Markers, cameras, tools, and other items that are placed in a map",
                0,
                helper.totalPlace()
            ),
            QuestOverviewItem(
                R.drawable.icons8_upward_arrow_96,
                R.color.md_purple_500,
                "Pickup Objectives",
                "Items such as docs cases or briefcases that are picked up",
                0,
                helper.totalPickup()
            )
        )

        sharedViewModel.quests.observe(requireActivity()) {
            if (it == null) return@observe

            data[0].updateCount(QuestsHelper.getAllCompletedQuests(it).size)

            if (this::mAdapter.isInitialized)
                mAdapter.updateData(data)
        }

        sharedViewModel.objectives.observe(requireActivity()) {
            if (it == null) return@observe
            data[1].updateCount(QuestsHelper.getTotalPMCEliminations(it))
            data[2].updateCount(QuestsHelper.getTotalScavEliminations(it))
            data[3].updateCount(QuestsHelper.getTotalQuestItemsCompleted(it))
            data[4].updateCount(QuestsHelper.getTotalFIRItemsCompleted(it))
            data[5].updateCount(QuestsHelper.getTotalHandoverItemCompleted(it))
            data[6].updateCount(QuestsHelper.getTotalPlacedCompleted(it))
            data[7].updateCount(QuestsHelper.getTotalPickupCompleted(it))

            if (this::mAdapter.isInitialized)
                mAdapter.updateData(data)
        }

        mAdapter = SlimAdapter.create().attachTo(mRecyclerView).register<QuestOverviewItem>(R.layout.quest_overview_list_item) { item, i ->
            i.text(R.id.questOverviewItemTitle, item.title)
            i.text(R.id.questOverviewItemSubtitle, item.subtitle)
            i.text(R.id.questOverviewItemCount, item.count)
            i.image(R.id.questOverviewItemIcon, item.icon)
            i.background(R.id.questOverviewItemIcon, item.color)

            val progressBar = i.findViewById<ProgressBar>(R.id.questOverviewItemPG)
            progressBar.progress = item.progress
            progressBar.max = item.maxProgress
            progressBar.progressTintList = ColorStateList.valueOf(resources.getColor(item.color))
        }.updateData(data)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuestsOverviewFragment()
    }
}