package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.RecyclerAdapter
import com.austinhodak.thehideout.RecyclerItem
import com.austinhodak.thehideout.databinding.FragmentAllAmmoListBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.AmmoModel

class AmmoAllListFragment : Fragment() {

    private var _binding: FragmentAllAmmoListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AmmoViewModel by activityViewModels()

    private val mAdapter by lazy {
        RecyclerAdapter(RecyclerItem.diffCallback<AmmoModel>(), R.layout.ammo_list_item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllAmmoListBinding.inflate(inflater, container, false)
        binding.ammoList.layoutManager = LinearLayoutManager(requireContext())
        binding.ammoList.adapter = mAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allAmmoList.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)
        }
    }
}