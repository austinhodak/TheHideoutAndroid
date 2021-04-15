package com.austinhodak.thehideout.weapons

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.databinding.FragmentWeaponListBinding
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.weapons.models.Weapon
import com.austinhodak.thehideout.weapons.viewmodels.WeaponViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.itemanimators.AlphaInAnimator

private const val ARG_PARAM1 = "param1"

class WeaponListFragment : Fragment() {

    private var weaponClass: String = ""
    private val model: WeaponViewModel by activityViewModels()
    private val fleaViewModel: FleaViewModel by activityViewModels()

    private var _binding: FragmentWeaponListBinding? = null
    private val binding get() = _binding!!

    private lateinit var fastAdapter: FastAdapter<Weapon>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            weaponClass = it.getString(ARG_PARAM1).toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeaponListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        binding.weaponList.layoutManager = linearLayoutManager
        binding.weaponList.itemAnimator = AlphaInAnimator()

        val itemAdapter = ItemAdapter<Weapon>()
        fastAdapter = FastAdapter.with(itemAdapter)
        binding.weaponList.adapter = fastAdapter

        //TODO Add on weapon click.
        fastAdapter.onClickListener = { view, adapter, weapon, pos ->
            startActivity(Intent(requireContext(), WeaponDetailActivity::class.java).apply {
                putExtra("id", weapon._id)
            })
            false
        }

        model.weaponsList.observe(viewLifecycleOwner) { list ->
            Handler().postDelayed({
                val newList = list.map {
                    Weapon(
                        it.grid,
                        it.firemodes,
                        it.recoil,
                        it.description,
                        it.weight,
                        it._id,
                        it.name,
                        it.`class`,
                        it.image,
                        it.rpm,
                        it.range,
                        it.ergonomics,
                        it.accuracy,
                        it.velocity,
                        it.fields,
                        it.calibre,
                        it.__v,
                        it.builds,
                        it.wiki
                    )
                }
                itemAdapter.set(newList.filter { it.`class` == weaponClass }.sortedBy { it.name })
            }, 50)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            WeaponListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}