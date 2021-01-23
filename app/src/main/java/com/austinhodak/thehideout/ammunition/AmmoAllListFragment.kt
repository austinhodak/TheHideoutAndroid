package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentAllAmmoListBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import net.idik.lib.slimadapter.SlimAdapter

class AmmoAllListFragment : Fragment() {

    private var _binding: FragmentAllAmmoListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AmmoViewModel by activityViewModels()

    /*private val mAdapter by lazy {
        RecyclerAdapter(RecyclerItem.diffCallback<CaliberModel>(), R.layout.ammo_category_item)
    }*/



    private val mAdapter by lazy {
        SlimAdapter.create().register<CaliberModel>(R.layout.ammo_category_item) { caliber, i ->
            val mAmmoAdapter = SlimAdapter.create().register<AmmoModel>(R.layout.ammo_list_item_small_search) { ammo, i ->

            }
            val recyclerView = i.findViewById<RecyclerView>(R.id.ammoCategoryRV)
            recyclerView.adapter = mAmmoAdapter
            mAmmoAdapter.updateData(caliber.ammo)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllAmmoListBinding.inflate(inflater, container, false)
        binding.ammoList.layoutManager = LinearLayoutManager(requireContext())
        binding.ammoList.adapter = mAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data.observe(viewLifecycleOwner) {
            mAdapter.updateData(it)
        }
    }
}