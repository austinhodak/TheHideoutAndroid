package com.austinhodak.thehideout.flea_market

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.utils.logScreen
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.google.android.material.bottomnavigation.BottomNavigationView

class FleaMarketNavHome : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_flea_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNav(view.findViewById(R.id.hideoutBottomNavBar))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_flea, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flea_tm -> {
                "https://tarkov-market.com/".openWithCustomTab(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnNavigationItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.fleaItems -> FleaMarketListFragment()
                R.id.fleaPriceAlerts -> FleaMarketPriceAlertsFragment()
                R.id.fleaFavorites -> FleaMarketFavoritesFragment()
                else -> FleaMarketListFragment()
            }

            childFragmentManager.beginTransaction().replace(R.id.hideoutFragment, fragment).commit()
            true
        }
        bottomNav.selectedItemId = R.id.fleaItems
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logScreen("AmmunitionFragment")
    }
}