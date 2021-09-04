package com.austinhodak.thehideout.flea_market

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.FleaItem
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.databinding.FragmentFleaListBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.utils.logScreen
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import net.idik.lib.slimadapter.SlimAdapter
import timber.log.Timber

@AndroidEntryPoint
class FleaMarketListFragment : Fragment() {

    private var currentSearchKey: String = ""
    private var mList: MutableList<FleaItem>? = null
    lateinit var mAdapter: SlimAdapter

    private val viewModel: FleaViewModel by activityViewModels()
    private var sortBy = 1

    lateinit var fastAdapter: FastAdapter<FleaItem>
    lateinit var itemAdapter: ItemAdapter<FleaItem>

    lateinit var prefs: SharedPreferences

    private var _binding: FragmentFleaListBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("CoroutineCreationDuringComposition")
    @ExperimentalMaterialApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HideoutTheme {
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(scope) {
                        viewModel.getAllItems()
                    }

                    val data = viewModel.fleaItemsNew.observeAsState()

                    val searchKey = viewModel.searchKey.observeAsState()
                    val sortBy = viewModel.sortBy.observeAsState()

                    val list = when (sortBy.value) {
                        0 -> data.value?.sortedBy { it.Name }
                        1 -> data.value?.sortedByDescending { it.getPrice() }
                        2 -> data.value?.sortedByDescending { it.getPricePerSlot() }
                        3 -> data.value?.sortedByDescending { it.pricing?.changeLast48h }
                        4 -> data.value?.sortedBy { it.pricing?.changeLast48h }
                        else -> data.value?.sortedBy { it.getPrice() }
                    }?.filter { if (searchKey.value.isNullOrEmpty()) true else it.Name?.contains(searchKey.value.toString(), true) == true }

                    Timber.d(searchKey.value.toString())

                    LazyColumn(
                        modifier = Modifier,
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(items = list ?: emptyList()) { item ->
                            FleaItem(item = item) {

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        logScreen("FleaMarketListFragment")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_flea_main, menu)
    }

    @SuppressLint("CheckResult")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flea_sort -> {
                MaterialDialog(requireActivity()).show {
                    title(res = R.string.sort_by)
                    listItemsSingleChoice(R.array.flea_sort, initialSelection = viewModel.sortBy.value ?: 1) { _, index, _ ->
                        viewModel.setSort(index)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}