package com.austinhodak.thehideout.clothing.armor

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.AmmoSharedViewModel
import com.austinhodak.thehideout.viewmodels.WeaponViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ArmorTabFragment : Fragment() {

    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var tabs: TabLayout
    private lateinit var classes: Array<String>
    private var sortBy: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        classes = resources.getStringArray(R.array.armor_tabs)
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
            tabs.addTab(tabs.newTab().setText(i))
        }
        TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = classes[position]
        }.attach()
    }

    private inner class CollectionAdapter(fragment: Fragment, val itemsCount: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = itemsCount

        override fun createFragment(position: Int): Fragment {
            return ArmorListFragment.newInstance(position, sortBy)
        }
    }
}