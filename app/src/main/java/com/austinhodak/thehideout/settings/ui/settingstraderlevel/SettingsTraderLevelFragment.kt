package com.austinhodak.thehideout.settings.ui.settingstraderlevel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentTraderLevelSettingsBinding
import com.austinhodak.thehideout.quests.models.Traders
import com.austinhodak.thehideout.utils.getTraderIcon
import net.idik.lib.slimadapter.SlimAdapter

class SettingsTraderLevelFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsTraderLevelFragment()
    }

    private var _binding: FragmentTraderLevelSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTraderLevelSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.traderLevelRv.layoutManager = LinearLayoutManager(requireContext())
        SlimAdapter.create().updateData(Traders.values().toMutableList().filterNot { it == Traders.FENCE }).register<Traders>(R.layout.item_trader_level_settings) { trader, i ->
            i.image(R.id.imageView, getTraderIcon(trader))
            i.text(R.id.textView4, trader.id)
        }.attachTo(binding.traderLevelRv)
    }
}