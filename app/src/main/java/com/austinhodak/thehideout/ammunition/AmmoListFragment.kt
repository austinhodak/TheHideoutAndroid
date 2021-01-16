package com.austinhodak.thehideout.ammunition

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.inflate
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.AmmoModel

private const val ARG_PARAM1 = "param1"

class AmmoListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String = ""
    private var ammoList: List<AmmoModel>?= null
    private var adapter: RecyclerAdapter? = null
    internal val sharedViewModel: AmmoViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1).toString()
        }

        when (sharedViewModel.sortBy.value) {
            0 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.name }
            1 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.damage }?.reversed()
            2 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.penetration }?.reversed()
            3 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.armor }?.reversed()
        }

        adapter = RecyclerAdapter(ammoList!!, requireContext())

        sharedViewModel.sortBy.observe(requireActivity(), { sort ->
            when (sort) {
                0 -> adapter?.updateData(ammoList?.sortedBy { it.name })
                1 -> adapter?.updateData(ammoList?.sortedBy { it.damage }?.reversed())
                2 -> adapter?.updateData(ammoList?.sortedBy { it.penetration }?.reversed())
                3 -> adapter?.updateData(ammoList?.sortedBy { it.armor }?.reversed())
            }
            //adapter?.notifyDataSetChanged()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ammo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: Int) =
            AmmoListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    class RecyclerAdapter(private var ammoList: List<AmmoModel>, var context: Context): RecyclerView.Adapter<RecyclerAdapter.AmmoHolder>() {
        fun updateData(ammoList: List<AmmoModel>?) {
            this.ammoList = ammoList!!
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmmoHolder {
            val view = parent.inflate(R.layout.ammo_list_item, false)
            return AmmoHolder(view, context)
        }

        override fun onBindViewHolder(holder: AmmoHolder, position: Int) {
            val itemAmmo = ammoList[position]
            holder.bindAmmo(itemAmmo)
        }

        override fun getItemCount(): Int = ammoList.size

        class AmmoHolder(v: View, ctx: Context) : RecyclerView.ViewHolder(v), View.OnClickListener {
            val ammoName = v.findViewById<TextView>(R.id.ammo_name)
            val ammoSubtitle = v.findViewById<TextView>(R.id.ammo_subtitle)
            val ammoDamage = v.findViewById<TextView>(R.id.ammo_dmg)
            val ammoPen = v.findViewById<TextView>(R.id.ammo_pen)
            val ammoImage = v.findViewById<ImageView>(R.id.ammo_image)
            val armorPenLayout = v.findViewById<ConstraintLayout>(R.id.armor_pen_layout)

            val context = ctx

            val a1 = v.findViewById<View>(R.id.a_1)
            val a2 = v.findViewById<View>(R.id.a_2)
            val a3 = v.findViewById<View>(R.id.a_3)
            val a4 = v.findViewById<View>(R.id.a_4)
            val a5 = v.findViewById<View>(R.id.a_5)
            val a6 = v.findViewById<View>(R.id.a_6)

            override fun onClick(v: View?) {

            }

            fun bindAmmo(ammo: AmmoModel) {
                ammoName.text = ammo.name
                ammoDamage.text = ammo.damage.toString()
                ammoPen.text = ammo.penetration.toString()
                ammoSubtitle.text = ammo.getSubtitle()

                a1.setBackgroundResource(ammo.getColor(1))
                a2.setBackgroundResource(ammo.getColor(2))
                a3.setBackgroundResource(ammo.getColor(3))
                a4.setBackgroundResource(ammo.getColor(4))
                a5.setBackgroundResource(ammo.getColor(5))
                a6.setBackgroundResource(ammo.getColor(6))

                //Glide.with(context).load("https://www.eftdb.one/static/item/thumb/${ammo.image}").placeholder(R.drawable.icons8_ammo_100).into(ammoImage)

                if (ammo.armor == "------") {
                    armorPenLayout.visibility = View.GONE
                } else {
                    armorPenLayout.visibility = View.VISIBLE
                }
            }
        }
    }
}