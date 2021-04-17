package com.austinhodak.thehideout.medical

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.utils.logScreen
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MedicalTabFragment : Fragment() {

    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var tabs: TabLayout
    private lateinit var classes: Array<String>
    private var sortBy: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        classes = resources.getStringArray(R.array.medical_tabs)
        return inflater.inflate(R.layout.fragment_backpack_tabs, container, false)
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
            return when (position) {
                0 -> MedsListFragment.newInstance(position, sortBy)
                else -> StimsListFragment.newInstance(position, sortBy)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("MedicalFragment")
    }
}