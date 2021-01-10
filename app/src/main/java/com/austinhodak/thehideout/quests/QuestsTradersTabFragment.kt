package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.quests.models.Traders
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class QuestsTradersTabFragment : Fragment() {

    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var tabs: TabLayout
    private lateinit var traders: Array<Traders>
    private var sortBy: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        traders = Traders.values()
        return inflater.inflate(R.layout.fragment_quest_traders_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectionAdapter = CollectionAdapter(this, traders.size)
        val viewpager = view.findViewById<ViewPager2>(R.id.ammo_viewpager)
        viewpager.adapter = collectionAdapter
        tabs = view.findViewById(R.id.ammo_tabs)
        viewpager.offscreenPageLimit = 3

        for (i in traders) {
            tabs.addTab(tabs.newTab().setText(i.id))
        }
        TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = traders[position].id
        }.attach()
    }

    private inner class CollectionAdapter(fragment: Fragment, val itemsCount: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = itemsCount

        override fun createFragment(position: Int): Fragment {
            return QuestsTradersListFragment.newInstance(traders[position])
        }
    }
}