package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.quests.models.Maps
import com.google.android.material.tabs.TabLayout

class QuestsMapTabFragment : Fragment() {

    private lateinit var tabs: TabLayout
    private lateinit var maps: Array<Maps>
    private var sortBy: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        maps = Maps.values()
        return inflater.inflate(R.layout.fragment_quest_traders_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewpager = view.findViewById<ViewPager>(R.id.ammo_viewpager)
        tabs = view.findViewById(R.id.ammo_tabs)

        for (i in maps) {
            tabs.addTab(tabs.newTab().setText(i.id))
        }

        childFragmentManager.beginTransaction().replace(R.id.quest_frame, QuestsMapListFragment.newInstance(maps[0])).commit()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                childFragmentManager.beginTransaction().replace(R.id.quest_frame, QuestsMapListFragment.newInstance(maps[tab?.position ?: 0])).commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })


    }
}