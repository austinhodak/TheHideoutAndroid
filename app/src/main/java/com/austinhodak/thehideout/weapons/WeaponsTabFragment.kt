package com.austinhodak.thehideout.weapons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.utils.logScreen
import com.austinhodak.thehideout.weapons.viewmodels.WeaponViewModel
import com.google.android.material.tabs.TabLayout

class WeaponsTabFragment : Fragment() {

    private lateinit var tabs: TabLayout
    private lateinit var classes: Array<String>
    private lateinit var classesNames: Array<String>
    private var sortBy: Int = 0
    internal val sharedViewModel: AmmoViewModel by activityViewModels()
    val weaponViewModel: WeaponViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        classes = resources.getStringArray(R.array.weapon_classes_ids)
        classesNames = resources.getStringArray(R.array.weapon_classes_names)

        return inflater.inflate(R.layout.fragment_weapon_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*
        collectionAdapter = CollectionAdapter(this, bsgViewModel.weaponClasses().size)

        val viewpager = view.findViewById<ViewPager2>(R.id.ammo_viewpager)

        viewpager.adapter = collectionAdapter

        tabs = view.findViewById(R.id.ammo_tabs)

        viewpager.offscreenPageLimit = 3

        for (i in bsgViewModel.weaponClasses()) {
            tabs.addTab(tabs.newTab().setText(i.name))
        }

        TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = bsgViewModel.weaponClasses().elementAt(position).name
        }.attach()*/
    }
    
    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_name -> {
                sharedViewModel.setSortBy(0)
            }
            R.id.sort_damage -> {
                sharedViewModel.setSortBy(1)
            }
            R.id.sort_pen -> {
                sharedViewModel.setSortBy(2)
            }
            R.id.sort_armor -> {
                sharedViewModel.setSortBy(3)
            }
            R.id.weapon_sort -> {
                MaterialDialog(requireContext()).show {
                    listItemsSingleChoice(R.array.weapon_sort, initialSelection = sortBy) { dialog, index, text ->
                        sortBy = index
                    }
                    title(text = "Sort By")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class CollectionAdapter(fragment: Fragment, val itemsCount: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = itemsCount

        override fun createFragment(position: Int): Fragment {
            return WeaponListFragment.newInstance(bsgViewModel.weaponClasses().elementAt(position))
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("WeaponsFragment")
    }
}