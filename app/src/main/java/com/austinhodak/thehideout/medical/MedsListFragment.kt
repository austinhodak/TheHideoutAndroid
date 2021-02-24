package com.austinhodak.thehideout.medical

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.medical.models.Med
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class MedsListFragment : Fragment() {

    private var param1: Int = 0
    private var medList: List<Med>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }

        medList = MedicalHelper.getMeds(requireContext()).sortedBy { it.name }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        SlimAdapter.create().register<Med>(R.layout.item_med) { med, i ->
            i.text(R.id.medName, med.name)
            i.text(R.id.medSubtitle, med.type)

            val image = i.findViewById<ImageView>(R.id.medImage)
            Glide.with(this).load(med.icon).into(image)
        }.attachTo(recyclerView).updateData(medList)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            MedsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}