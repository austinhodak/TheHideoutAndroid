package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.austinhodak.tarkovapi.utils.AmmoCalibers
import com.austinhodak.tarkovapi.utils.getCaliberName
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.databinding.FragmentAmmoTabsBinding
import com.austinhodak.thehideout.utils.logScreen
import com.google.android.material.tabs.TabLayoutMediator

@ExperimentalMaterialApi
class AmmoTabsFragment : Fragment() {

    private lateinit var calibers: List<String>
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
        calibers = AmmoCalibers()
        setupTabs()
    }

    private fun setupTabs() {
        binding.ammoViewpager.adapter = CollectionAdapter(this, calibers.size)

        for (i in calibers) {
            binding.ammoTabs.addTab(binding.ammoTabs.newTab().setText(i))
        }

        TabLayoutMediator(binding.ammoTabs, binding.ammoViewpager) { tab, position ->
            tab.text = getCaliberName(calibers[position])
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_ammo_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        sharedViewModel.setSort(
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
            return AmmoListFragment.newInstance(calibers[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("AmmunitionFragment")
    }
}