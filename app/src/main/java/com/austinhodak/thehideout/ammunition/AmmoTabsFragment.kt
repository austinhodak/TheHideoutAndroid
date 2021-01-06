package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.austinhodak.thehideout.viewmodels.AmmoSharedViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AmmoTabsFragment : Fragment() {

    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var tabs: TabLayout
    private lateinit var calibers: List<CaliberModel>
    private var sortBy: Int = 0
    internal val sharedViewModel: AmmoSharedViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        calibers = AmmoHelper.getCalibers(requireContext())


        return inflater.inflate(R.layout.fragment_ammo_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectionAdapter = CollectionAdapter(this, calibers.size)
        val viewpager = view.findViewById<ViewPager2>(R.id.ammo_viewpager)
        viewpager.adapter = collectionAdapter
        tabs = view.findViewById(R.id.ammo_tabs)
        viewpager.offscreenPageLimit = 3
        for (i in calibers) {
            tabs.addTab(tabs.newTab().setText(i.name))
        }

        TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = calibers[position].name
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ammo_list_menu, menu)
    }

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
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class CollectionAdapter(fragment: Fragment, val itemsCount: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = itemsCount

        override fun createFragment(position: Int): Fragment {
            return AmmoListFragment.newInstance(calibers[position]._id, sortBy)
        }
    }
}