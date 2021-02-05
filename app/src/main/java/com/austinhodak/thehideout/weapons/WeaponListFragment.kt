package com.austinhodak.thehideout.weapons

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.log
import com.austinhodak.thehideout.viewmodels.WeaponViewModel
import com.austinhodak.thehideout.viewmodels.models.WeaponModel
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class WeaponListFragment : Fragment() {

    private var param1: String = ""
    private var weaponList: List<WeaponModel>? = null
    private val sharedViewModel: WeaponViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1).toString()
        }

        weaponList = sharedViewModel.list

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

        SlimAdapter.create().register<WeaponModel>(R.layout.weapon_list_item) { weapon, i ->
            i.clicked(R.id.weaponListItemCard) {
                startActivity(Intent(requireContext(), WeaponDetailActivity::class.java).apply {
                    putExtra("id", weapon._id)
                })

                log(FirebaseAnalytics.Event.SELECT_ITEM, weapon._id, weapon.name, "weapon")
            }

            i.text(R.id.weaponName, weapon.name)
            i.text(
                R.id.weaponAmmo,
                AmmoHelper.getCaliberByID(weapon.calibre)?.longName
            )
            i.text(R.id.weaponRange, "${weapon.range}m")
            i.text(R.id.weaponRPM, weapon.rpm.toString())
            i.text(R.id.weaponVRecoil, weapon.recoil.toString())
            i.text(R.id.weaponErgo, weapon.ergonomics.toString())

            Glide.with(this).load("https://www.eftdb.one/static/item/full/${weapon.image}")
                .into(i.findViewById(R.id.weaponImage))
        }.attachTo(recyclerView)
            .updateData(weaponList!!.filter { it.`class` == param1 }.sortedBy { it.name })
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
}