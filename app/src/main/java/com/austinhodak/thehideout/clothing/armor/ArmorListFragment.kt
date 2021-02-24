package com.austinhodak.thehideout.clothing.armor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.clothing.models.Armor
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class ArmorListFragment : Fragment() {

    private var param1: Int = 0
    private var armorList: List<Armor>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }

        armorList = when (param1) {
            0 -> ArmorHelper.getArmors(requireContext()).filter { it.`class` == "Chest Rig" }
            1 -> ArmorHelper.getArmors(requireContext()).filter { it.`class` == "Body Armor" }
            2 -> ArmorHelper.getArmors(requireContext()).filter { it.`class` == "Helmet" }
            3 -> ArmorHelper.getArmors(requireContext()).filter { it.`class` == "Attachment" }
            else -> ArmorHelper.getArmors(requireContext()).filter { it.`class` == "" }
        }.sortedBy { it.name }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        SlimAdapter.create().register<Armor>(R.layout.item_armor) { armor, i ->
            i.text(R.id.armorName, armor.name)
            i.text(R.id.armorSubtitle, armor.getSubtitle())
            i.text(R.id.armorClass, armor.level.toString())
            i.text(R.id.armorSize, armor.internal.toString())
            i.text(R.id.armorHitpoints, armor.hitpoints.toString())

            var image = i.findViewById<ImageView>(R.id.armorImage)
            Glide.with(this).load("https://eftdb.one/static/item/thumb/${armor.image}").into(image)
        }.attachTo(recyclerView).updateData(armorList)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            ArmorListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}