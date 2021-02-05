package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentAmmoListBinding
import com.austinhodak.thehideout.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.viewmodels.models.firestore.FSAmmo
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.itemanimators.AlphaInAnimator
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_CALIBER_ID = "param1"

class AmmoListFragment : Fragment() {

    private var caliberID: String = ""
    private var ammoList: List<FSAmmo>?= null
    internal val sharedViewModel: AmmoViewModel by activityViewModels()

    private var _binding: FragmentAmmoListBinding? = null
    private val binding get() = _binding!!

    lateinit var fastAdapter: FastAdapter<FSAmmo>

    val adapter: SlimAdapter by lazy {
        SlimAdapter.create().register<FSAmmo>(R.layout.ammo_list_item) { ammo, i ->
            i.text(R.id.ammo_name, ammo.name)
            i.text(R.id.ammo_subtitle, ammo.getSubtitle())
            i.text(R.id.ammo_dmg, ammo.damage.toString())
            i.text(R.id.ammo_pen, ammo.penetration.toString())

            i.background(R.id.a_1, ammo.getColor(1))
            i.background(R.id.a_2, ammo.getColor(2))
            i.background(R.id.a_3, ammo.getColor(3))
            i.background(R.id.a_4, ammo.getColor(4))
            i.background(R.id.a_5, ammo.getColor(5))
            i.background(R.id.a_6, ammo.getColor(6))

            if (ammo.armor == "------") {
                i.gone(R.id.armor_pen_layout)
            } else {
                i.visible(R.id.armor_pen_layout)
            }

            val image = i.findViewById<ImageView>(R.id.ammo_image)
            Glide.with(context ?: return@register).load("https://www.eftdb.one/static/item/thumb/${ammo.image}").placeholder(R.drawable.icons8_ammo_100).into(image)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            caliberID = it.getString(ARG_CALIBER_ID).toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAmmoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        binding.ammoList.layoutManager = linearLayoutManager
        binding.ammoList.itemAnimator = AlphaInAnimator()

        val itemAdapter = ItemAdapter<FSAmmo>()
        fastAdapter = FastAdapter.with(itemAdapter)

        binding.ammoList.adapter = fastAdapter

        sharedViewModel.ammoList.observe(requireActivity()) { list ->
            val filtered = list.filter { it.caliber == caliberID }.sortedBy { it.name }

            if (filtered != ammoList) {
                ammoList = filtered
            }

            updateList(itemAdapter)
        }

        sharedViewModel.sortBy.observe(requireActivity(), { sort ->
            updateList(itemAdapter)
        })
    }

    //TODO Fix double call on start.
    private fun updateList(itemAdapter: ItemAdapter<FSAmmo>) {
        Log.d("AMMO", "updateList")
        val oldList = ammoList

        when (sharedViewModel.sortBy.value) {
            0 -> ammoList = ammoList?.sortedBy { it.name }
            1 -> ammoList = ammoList?.sortedBy { it.damage }?.reversed()
            2 -> ammoList = ammoList?.sortedBy { it.penetration }?.reversed()
            3 -> ammoList = ammoList?.sortedBy { it.armor }?.reversed()
        }

        itemAdapter.set(ammoList ?: return)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            AmmoListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CALIBER_ID, param1)
                }
            }
    }
}