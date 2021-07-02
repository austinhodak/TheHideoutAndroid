package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.austinhodak.tarkovapi.room.TarkovDatabase
import com.austinhodak.thehideout.ammunition.models.Ammo
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.compose.components.AmmoDetailCard
import com.austinhodak.thehideout.compose.theme.TheHideoutTheme
import com.austinhodak.thehideout.databinding.FragmentAmmoListBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

private const val ARG_CALIBER_ID = "param1"

class AmmoListFragment : Fragment() {

    private var caliberID: String = ""
    private var ammoList: List<Ammo>?= null
    internal val sharedViewModel: AmmoViewModel by activityViewModels()

    private var _binding: FragmentAmmoListBinding? = null
    private val binding get() = _binding!!

    lateinit var fastAdapter: FastAdapter<Ammo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            caliberID = it.getString(ARG_CALIBER_ID).toString()
        }
    }



    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TheHideoutTheme {
                    val scope = rememberCoroutineScope()
                    //val data = sharedViewModel.ammo.observeAsState()
                    val data = TarkovDatabase.getDatabase(context, scope).WeaponDao().getAllAmmo().observeAsState()
                    //MainView()
                    LazyColumn(
                        Modifier.padding(vertical = 4.dp)
                    ) {
                        items(items = data.value?.filter { it.Caliber == caliberID } ?: emptyList()) { item ->
                            //Timber.d(item.name)
                            AnimatedVisibility(visible = true) {
                                AmmoDetailCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*val linearLayoutManager = LinearLayoutManager(requireActivity())
        binding.ammoList.layoutManager = linearLayoutManager
        binding.ammoList.itemAnimator = AlphaInAnimator()

        val itemAdapter = ItemAdapter<Ammo>()
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

        fastAdapter.onClickListener = { view, adapter, ammo, pos ->
            startActivity(Intent(requireContext(), AmmoDetailActivity::class.java).apply {
                putExtra("id", ammo._id)
            })
            false
        }*/
    }

    //TODO Fix double call on start.
    private fun updateList(itemAdapter: ItemAdapter<Ammo>) {
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