package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentAmmoTabsBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.google.android.material.tabs.TabLayoutMediator

class AmmoTabsFragment : Fragment() {

    private lateinit var calibers: List<AmmoHelper.Caliber>
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
        calibers = AmmoHelper.caliberList
        setupTabs()
    }

    private fun setupTabs() {
        binding.ammoViewpager.adapter = CollectionAdapter(this, calibers.size)

        for (i in calibers) {
            binding.ammoTabs.addTab(binding.ammoTabs.newTab().setText(i.name))
        }

        TabLayoutMediator(binding.ammoTabs, binding.ammoViewpager) { tab, position ->
            tab.text = calibers[position].name
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.ammo_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        sharedViewModel.setSortBy(
            when (item.itemId) {
                R.id.sort_name -> 0
                R.id.sort_damage -> 1
                R.id.sort_pen -> 2
                R.id.sort_armor -> 3
                else -> return false
            }
        )
        return super.onOptionsItemSelected(item)
    }

    private inner class CollectionAdapter(fragment: Fragment, val itemsCount: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = itemsCount

        override fun createFragment(position: Int): Fragment {
            return AmmoListFragment.newInstance(calibers[position].key)
        }
    }
}