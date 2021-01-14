package com.austinhodak.thehideout.hideout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HideoutMainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.hideout_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNav(view.findViewById(R.id.hideoutBottomNavBar))
    }

    private fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnNavigationItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.menuModules -> HideoutModuleListFragment()
                R.id.menuCrafts -> HideoutModuleListFragment()
                else -> HideoutModuleListFragment()
            }

            (requireActivity() as MainActivity).setQuestChipVisibility(when (it.itemId) {
                R.id.menuModules -> true
                R.id.menuCrafts -> false
                else -> false
            })

            childFragmentManager.beginTransaction().replace(R.id.hideoutFragment, fragment).commit()
            true
        }
        bottomNav.selectedItemId = R.id.menuModules
    }
}