package com.austinhodak.thehideout.medical

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Backpack
import com.austinhodak.thehideout.viewmodels.models.Stim
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class StimsListFragment : Fragment() {

    private var param1: Int = 0
    private var stimList: List<Stim>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }

        stimList = MedicalHelper.getStims(requireContext()).sortedBy { it.name }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        SlimAdapter.create().register<Stim>(R.layout.stim_list_item) { stim, i ->
            i.text(R.id.stimName, stim.name)
            i.text(R.id.stimSubtitle, stim.type)

            val image = i.findViewById<ImageView>(R.id.stimImage)
            Glide.with(this).load(stim.icon).into(image)
        }.attachTo(recyclerView).updateData(stimList)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            StimsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}