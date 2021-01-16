package com.austinhodak.thehideout.weapons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.WeaponViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WeaponsTabFragment : Fragment() {

    private lateinit var collectionAdapter: CollectionAdapter
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
        //setHasOptionsMenu(true)

        //weaponViewModel.loadWeapons()
        // Inflate the layout for this fragment
        classes = resources.getStringArray(R.array.weapon_classes_ids)
        classesNames = resources.getStringArray(R.array.weapon_classes_names)

        return inflater.inflate(R.layout.fragment_weapon_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectionAdapter = CollectionAdapter(this, classes.size)
        val viewpager = view.findViewById<ViewPager2>(R.id.ammo_viewpager)
        viewpager.adapter = collectionAdapter
        tabs = view.findViewById(R.id.ammo_tabs)
        viewpager.offscreenPageLimit = 3
        for (i in classes) {
            tabs.addTab(tabs.newTab().setText(classesNames[classes.indexOf(i)]))
        }

        TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = classesNames[position]
        }.attach()
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.weapon_list_menu, menu)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            return WeaponListFragment.newInstance(classes[position], sortBy)
        }
    }
}