package com.austinhodak.thehideout.clothing.backpacks

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Armor
import com.austinhodak.thehideout.viewmodels.models.Backpack
import com.austinhodak.thehideout.viewmodels.models.Rig
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class BackpackListFragment : Fragment() {

    private var param1: Int = 0
    private var rigList: List<Backpack>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }

        rigList = BackpackHelper.getBackpacks(requireContext()).sortedBy { it.name }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        SlimAdapter.create().register<Backpack>(R.layout.backpack_list_item) { bp, i ->
            i.text(R.id.backpackName, bp.name)
            i.text(R.id.backpackSubtitle, bp.getSubtitle())
            i.text(R.id.backpackWeight, "${bp.weight}kg")
            i.text(R.id.backpackSize, bp.internal.toString())

            val image = i.findViewById<ImageView>(R.id.backpackImage)
            Glide.with(this).load("https://eftdb.one/static/item/thumb/${bp.image}").into(image)
        }.attachTo(recyclerView).updateData(rigList)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            BackpackListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}