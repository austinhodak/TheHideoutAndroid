package com.austinhodak.thehideout.clothing.rigs

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Armor
import com.austinhodak.thehideout.viewmodels.models.Rig
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class RigsListFragment : Fragment() {

    private var param1: Int = 0
    private var rigList: List<Rig>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }

        rigList = RigHelper.getRigs(requireContext()).sortedBy { it.name }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        SlimAdapter.create().register<Rig>(R.layout.rig_list_item) { rig, i ->
            i.text(R.id.armorName, rig.name)
            i.text(R.id.armorSubtitle, rig.getSubtitle())
            i.text(R.id.armorSize, rig.internal.toString())
            i.text(R.id.armorHitpoints, rig.getArmored())
            i.text(R.id.armorClass, "${rig.weight}kg")

            val image = i.findViewById<ImageView>(R.id.armorImage)
            Glide.with(this).load("https://eftdb.one/static/item/thumb/${rig.image}").into(image)
        }.attachTo(recyclerView).updateData(rigList)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            RigsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}