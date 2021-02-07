package com.austinhodak.thehideout.quests

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.QuestsFragmentBinding
import com.austinhodak.thehideout.logScreen
import com.austinhodak.thehideout.quests.inraid.QuestInRaidActivity
import com.austinhodak.thehideout.quests.inraid.models.Map
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class QuestMainFragment : Fragment() {

    private var _binding: QuestsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("QuestsFragment")
        QuestsHelper.getQuests(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = QuestsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNav(view.findViewById(R.id.questBottomNavBar))
        val fab = binding.questStartRaidFAB

        val bs = BottomSheetBehavior.from(view.findViewById<ConstraintLayout>(R.id.bottomSheet))
        bs.skipCollapsed = true
        bs.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    fab.show()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        fab.setOnClickListener {
            if (bs.state == BottomSheetBehavior.STATE_HIDDEN || bs.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bs.state = BottomSheetBehavior.STATE_EXPANDED
                fab.hide()
            } else {
                bs.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        (requireActivity() as MainActivity).updateChips(arrayListOf("All", "Available", "Locked", "Completed"))

        val mapRV = binding.bottomSheet.startRaidMapRV
        mapRV.layoutManager = LinearLayoutManager(context)
        val itemAdapter = ItemAdapter<Map>()
        val fastAdapter = FastAdapter.with(itemAdapter)
        for (map in com.austinhodak.thehideout.utils.Map.values()) {
            itemAdapter.add(Map(map))
        }
        mapRV.adapter = fastAdapter
        fastAdapter.onClickListener = { v, a, map, int ->
            startActivity(Intent(requireContext(), QuestInRaidActivity::class.java).apply {
                putExtra("map", map.map)
            })
            false
        }
    }

    private fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnNavigationItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.questOverview -> QuestsOverviewFragment.newInstance()
                R.id.questTraders -> QuestsTradersTabFragment()
                R.id.questMaps -> QuestsMapTabFragment()
                R.id.questItems -> QuestsItemListFragment()
                else -> QuestsOverviewFragment.newInstance()
            }

            (requireActivity() as MainActivity).setQuestChipVisibility(when (it.itemId) {
                R.id.questTraders -> true
                R.id.questMaps -> true
                else -> false
            })

            childFragmentManager.beginTransaction().replace(R.id.questsFragment, fragment).commit()
            true
        }
        bottomNav.selectedItemId = R.id.questOverview
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuestMainFragment()
    }
}