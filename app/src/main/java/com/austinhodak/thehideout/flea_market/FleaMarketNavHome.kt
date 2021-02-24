package com.austinhodak.thehideout.flea_market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.logScreen
import com.google.android.material.bottomnavigation.BottomNavigationView

class FleaMarketNavHome : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flea_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNav(view.findViewById(R.id.hideoutBottomNavBar))
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