package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class QuestMainFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    private fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnNavigationItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.questOverview -> QuestsOverviewFragment.newInstance()
                R.id.questTraders -> QuestsTradersTabFragment()
                else -> QuestsOverviewFragment.newInstance()
            }

            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.questsFragment, fragment).commit()
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