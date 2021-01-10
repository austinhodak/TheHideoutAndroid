package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.quests.models.Traders
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class QuestsTradersTabFragment : Fragment() {

    //private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var tabs: TabLayout
    private lateinit var traders: Array<Traders>
    private var sortBy: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        traders = Traders.values()
        return inflater.inflate(R.layout.fragment_quest_traders_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //collectionAdapter = CollectionAdapter(this, traders.size)
        val viewpager = view.findViewById<ViewPager>(R.id.ammo_viewpager)
        //viewpager.adapter = collectionAdapter

        //viewpager.adapter = PagerAdapter(childFragmentManager)
        tabs = view.findViewById(R.id.ammo_tabs)
        //viewpager.offscreenPageLimit = 2

        for (i in traders) {
            tabs.addTab(tabs.newTab().setText(i.id))
        }

        childFragmentManager.beginTransaction().replace(R.id.quest_frame, QuestsTradersListFragment.newInstance(traders[0])).commit()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                childFragmentManager.beginTransaction().replace(R.id.quest_frame, QuestsTradersListFragment.newInstance(traders[tab?.position ?: 0])).commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        //tabs.setupWithViewPager(viewpager)

        /*TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = traders[position].id
        }.attach()*/
    }

    /*inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = traders.size

        override fun getItem(position: Int): Fragment {
            return QuestsTradersListFragment.newInstance(traders[position])
        }

        override fun getPageTitle(position: Int): CharSequence {
            return traders[position].id
        }
    }*/

    /*private inner class CollectionAdapter(fragment: Fragment, val itemsCount: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = itemsCount

        override fun createFragment(position: Int): Fragment {
            return QuestsTradersListFragment.newInstance(traders[position])
        }
    }*/
}