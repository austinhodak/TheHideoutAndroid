package com.austinhodak.thehideout.ammunition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.compose.components.AmmoDetailCard
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val ARG_CALIBER_ID = "param1"

@ExperimentalMaterialApi
@AndroidEntryPoint
@Deprecated("")
class AmmoListFragment : Fragment() {

    private var caliberID: String = ""
    @Inject
    lateinit var tarkovRepo: TarkovRepo
    private val ammoViewModel: AmmoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            caliberID = it.getString(ARG_CALIBER_ID).toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HideoutTheme {
                    val scope = rememberCoroutineScope()
                    val data by tarkovRepo.getAllAmmo.collectAsState(initial = emptyList())
                    val sort by ammoViewModel.sort.observeAsState()

                    if (data.isNullOrEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    } else {
                        LazyColumn(
                            Modifier.padding(vertical = 4.dp)
                        ) {
                            var items = data.filter { it.Caliber == caliberID }
                            items = when (sort) {
                                0 -> items.sortedBy { it.shortName }
                                1 -> items.sortedBy { it.pricing?.lastLowPrice }
                                2 -> items.sortedByDescending { it.pricing?.lastLowPrice }
                                3 -> items.sortedByDescending { it.ballistics?.damage }
                                4 -> items.sortedByDescending { it.ballistics?.penetrationPower }
                                else -> items.sortedBy { it.shortName }
                            }
                            items(items = items) { ammo ->
                                AmmoDetailCard(
                                    ammo
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated("")
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