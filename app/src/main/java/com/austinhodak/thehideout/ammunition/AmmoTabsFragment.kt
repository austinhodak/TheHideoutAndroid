package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentAmmoTabsBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.firestore.FSCaliber
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AmmoTabsFragment : Fragment() {

    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var tabs: TabLayout
    private lateinit var calibers: List<FSCaliber>
    private var sortBy: Int = 0
    private val sharedViewModel: AmmoViewModel by activityViewModels()

    private var _binding: FragmentAmmoTabsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAmmoTabsBinding.inflate(inflater, container, false)
        val view = binding.root

        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.caliberList.observe(viewLifecycleOwner) {
            calibers = it
            setupTabs()
        }
    }

    private fun setupTabs() {
        collectionAdapter = CollectionAdapter(this, calibers.size)
        val viewpager = binding.ammoViewpager
        viewpager.adapter = collectionAdapter
        tabs = binding.ammoTabs
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
            return AmmoListFragment.newInstance(calibers[position]._id!!, sortBy)
        }
    }
}