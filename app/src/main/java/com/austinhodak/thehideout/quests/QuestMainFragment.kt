package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.logScreen
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class QuestMainFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("QuestsFragment")
        QuestsHelper.getQuests(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.quests_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNav(view.findViewById(R.id.questBottomNavBar))

        view.findViewById<FloatingActionButton>(R.id.questStartRaidFAB).setOnClickListener {
            Toast.makeText(activity, "This will be implemented Soon™", Toast.LENGTH_SHORT).show()
        }

        (requireActivity() as MainActivity).updateChips(arrayListOf("All", "Available", "Locked", "Completed"))
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