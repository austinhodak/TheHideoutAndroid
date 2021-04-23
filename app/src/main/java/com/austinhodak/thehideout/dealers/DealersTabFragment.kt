package com.austinhodak.thehideout.dealers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentDealersTabBinding
import com.austinhodak.thehideout.quests.models.Traders
import com.google.android.material.tabs.TabLayout

class DealersTabFragment : Fragment() {

    private lateinit var tabs: TabLayout
    private lateinit var traders: Array<Traders>

    private var _binding: FragmentDealersTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        traders = Traders.values()
        _binding = FragmentDealersTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewpager = view.findViewById<ViewPager>(R.id.ammo_viewpager)
        tabs = view.findViewById(R.id.ammo_tabs)

        for (i in traders) {
            tabs.addTab(tabs.newTab().setText(i.id))
        }

    }
}