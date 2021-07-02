package com.austinhodak.thehideout.clothing.backpacks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.apollographql.apollo.coroutines.toFlow
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.networking.TarkovApi
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.thehideout.clothing.ClothingDetailActivityC
import com.austinhodak.thehideout.clothing.models.Backpack
import com.austinhodak.thehideout.compose.theme.TheHideoutTheme
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import com.austinhodak.thehideout.utils.asCurrency
import com.google.accompanist.glide.rememberGlidePainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

private const val ARG_PARAM1 = "param1"

@ExperimentalAnimationApi
class BackpackListFragment : Fragment() {

    private val viewModel: FleaVM by activityViewModels()

    private var param1: Int = 0
    private var rigList: List<Backpack>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                TheHideoutTheme() {
                    MainView()
                }
            }
        }
    }

    @Composable
    private fun MainView() {
        BackpackList()
    }

    @ExperimentalCoroutinesApi
    @Composable
    private fun BackpackList() {
        Timber.d("Start load.")

        val list = TarkovApi().getTarkovClient(requireContext()).query(ItemsByTypeQuery(if (param1 == 0) ItemType.BACKPACK else ItemType.BACKPACK)).toFlow().collectAsState(initial = null)

        LazyColumn(
            Modifier.padding(vertical = 4.dp)
        ) {
            items(items = list.value?.data?.itemsByType?.sortedBy { it?.fragments?.itemFragment?.shortName } ?: emptyList()) { backpack ->
                BackpackCard(backpack)
            }
        }
    }

    @Composable
    private fun BackpackCard(
        backpack: ItemsByTypeQuery.ItemsByType?
    ) {
        Card(
            Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clickable {
                    startActivity(Intent(requireActivity(), ClothingDetailActivityC::class.java).apply {
                        putExtra("id", backpack?.fragments?.itemFragment?.id)
                    })
                }
        ) {
            Column {
                Row(
                    Modifier.padding(16.dp)
                ) {
                    Image(
                        rememberGlidePainter(request = backpack?.fragments?.itemFragment?.iconLink),
                        contentDescription = null,
                        modifier = Modifier
                            .width(38.dp)
                            .height(38.dp)
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = backpack?.fragments?.itemFragment?.shortName ?: "",
                            style = MaterialTheme.typography.h6,
                            fontSize = 18.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "Last Price: ${backpack?.fragments?.itemFragment?.avg24hPrice?.asCurrency()}",
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    private fun MainViewPreview() {
        MainView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        val adapter = SlimAdapter.create().register<Backpack>(R.layout.item_backpack) { bp, i ->
            i.text(R.id.backpackName, bp.name)
            i.text(R.id.backpackSubtitle, bp.getSubtitle())
            i.text(R.id.backpackWeight, "${bp.weight}kg")
            i.text(R.id.backpackSize, bp.internal.toString())

            val image = i.findViewById<ImageView>(R.id.backpackImage)
            Glide.with(this).load(bp.getImageURL()).into(image)

            i.clicked(R.id.weaponListItemCard) {
                startActivity(Intent(requireActivity(), ClothingDetailActivityC::class.java).apply {
                    putExtra("id", bp._id)
                })
            }
        }.register<ItemsByTypeQuery.ItemsByType>(R.layout.item_backpack) { bp, i ->
            i.text(R.id.backpackName, bp.name)
            //i.text(R.id.backpackSubtitle, bp.getSubtitle())
            //i.text(R.id.backpackWeight, "${bp.weight}kg")
            //i.text(R.id.backpack
            // Size, bp.internal.toString())

            val image = i.findViewById<ImageView>(R.id.backpackImage)
            Glide.with(this).load(bp.gridImageLink).into(image)

            i.clicked(R.id.weaponListItemCard) {
                startActivity(Intent(requireActivity(), ClothingDetailActivityC::class.java).apply {
                    putExtra("id", bp.id)
                })
            }
        }.attachTo(recyclerView)



        viewModel.itemsList.observe(viewLifecycleOwner) { items ->
            val list = items.value?.data?.itemsByType?.filter { it?.types?.contains(ItemType.BACKPACK) == true }
            adapter.updateData(list)
        }*/
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