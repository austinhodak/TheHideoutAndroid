package com.austinhodak.thehideout.weapons

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.inflate
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.WeaponViewModel
import com.austinhodak.thehideout.viewmodels.models.WeaponModel
import com.bumptech.glide.Glide

private const val ARG_PARAM1 = "param1"

class WeaponListFragment : Fragment() {

    private var param1: String = ""
    private var weaponList: List<WeaponModel>?= null
    private var adapter: RecyclerAdapter? = null
    internal val sharedViewModel: WeaponViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1).toString()
        }

        /*when (sharedViewModel.sortBy.value) {
            0 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.name }
            1 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.damage }?.reversed()
            2 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.penetration }?.reversed()
            3 -> ammoList = AmmoHelper.getAmmoListByID(requireActivity(), param1)?.sortedBy { it.armor }?.reversed()
        }*/

        weaponList = sharedViewModel.list
        adapter = RecyclerAdapter(weaponList!!.filter { it.`class` == param1 }.sortedBy { it.name }, requireContext())

        /*sharedViewModel.sortBy.observe(requireActivity(), { sort ->
            Log.d("SORT", "SORT ${sort}" )
            when (sort) {
                0 -> adapter?.updateData(ammoList?.sortedBy { it.name })
                1 -> adapter?.updateData(ammoList?.sortedBy { it.damage }?.reversed())
                2 -> adapter?.updateData(ammoList?.sortedBy { it.penetration }?.reversed())
                3 -> adapter?.updateData(ammoList?.sortedBy { it.armor }?.reversed())
            }
            //adapter?.notifyDataSetChanged()
        })*/
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
            WeaponListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    class RecyclerAdapter(private var weaponList: List<WeaponModel>, var context: Context): RecyclerView.Adapter<RecyclerAdapter.WeaponHolder>() {
        fun updateData(ammoList: List<WeaponModel>?) {
            this.weaponList = ammoList!!
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeaponHolder {
            val view = parent.inflate(R.layout.weapon_list_item, false)
            return WeaponHolder(view, context)
        }

        override fun onBindViewHolder(holder: WeaponHolder, position: Int) {
            val itemAmmo = weaponList[position]
            holder.bindAmmo(itemAmmo)
        }

        override fun getItemCount(): Int = weaponList.size

        class WeaponHolder(v: View, var ctx: Context) : RecyclerView.ViewHolder(v), View.OnClickListener {
            val weaponName = v.findViewById<TextView>(R.id.weaponName)
            val weaponAmmo = v.findViewById<TextView>(R.id.weaponAmmo)

            val weaponRange = v.findViewById<TextView>(R.id.weaponRange)
            val weaponRPM = v.findViewById<TextView>(R.id.weaponRPM)
            val weaponVRecoil = v.findViewById<TextView>(R.id.weaponVRecoil)
            val weaponHRecoil = v.findViewById<TextView>(R.id.weaponHRecoil)
            val weaponErgo = v.findViewById<TextView>(R.id.weaponErgo)

            val weaponImage = v.findViewById<ImageView>(R.id.weaponImage)

            /*val ammoName = v.findViewById<TextView>(R.id.ammo_name)
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
            val a6 = v.findViewById<View>(R.id.a_6)*/

            override fun onClick(v: View?) {

            }

            fun bindAmmo(weapon: WeaponModel) {
                weaponName.text = "${weapon.name}"
                weaponAmmo.text = "${AmmoHelper.getCaliberByID(ctx, weapon.calibre)?.name}"

                weaponRange.text = "${weapon.range}m"
                weaponRPM.text = weapon.rpm.toString()
                weaponVRecoil.text = weapon.recoil.vertical.toString()
                weaponHRecoil.text = weapon.recoil.horizontal.toString()
                weaponErgo.text = weapon.accuracy.toString()

                Glide.with(ctx).load("https://www.eftdb.one/static/item/full/${weapon.image}").into(weaponImage)

               /* ammoName.text = ammo.name
                ammoDamage.text = ammo.damage.toString()
                ammoPen.text = ammo.penetration.toString()
                ammoSubtitle.text = ammo.getSubtitle()

                a1.setBackgroundResource(ammo.getColor(1))
                a2.setBackgroundResource(ammo.getColor(2))
                a3.setBackgroundResource(ammo.getColor(3))
                a4.setBackgroundResource(ammo.getColor(4))
                a5.setBackgroundResource(ammo.getColor(5))
                a6.setBackgroundResource(ammo.getColor(6))


                if (ammo.armor == "------") {
                    armorPenLayout.visibility = View.GONE
                } else {
                    armorPenLayout.visibility = View.VISIBLE
                }*/
            }
        }
    }
}