package com.austinhodak.thehideout.flea_market

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.bumptech.glide.Glide
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import net.idik.lib.slimadapter.SlimAdapter

class FleaMarketListFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private var currentSearchKey: String = ""
    private var mList: MutableList<FleaItem>? = null
    lateinit var mAdapter: SlimAdapter
    private val viewModel: FleaViewModel by activityViewModels()
    private var sortBy = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flea_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView(view)
        progressBar = view.findViewById(R.id.fleaPG)
        progressBar.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun setupAdapter() {
        val mDialogAdaper = SlimAdapter.create().register<String>(R.layout.dialog_list_item_1) { s, i ->

        }.register<Wiki>(R.layout.dialog_list_item_1) { wiki, i ->
            i.text(R.id.itemText, "Go to Wiki Page")
            i.image(R.id.itemIcon, R.drawable.icons8_website_96)

            i.clicked(R.id.itemTop) {
                wiki.dialog.dismiss()
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse(wiki.url))
            }
        }.register<FleaItem>(R.layout.dialog_list_item_2) { item, i ->
            val top = i.findViewById<ConstraintLayout>(R.id.itemTop)
            top.isClickable = false

            i.text(R.id.itemText, item.traderName)
            i.text(R.id.itemRightText, item.getCurrentTraderPrice())
            i.image(R.id.itemIcon, R.drawable.ic_baseline_groups_24)
        }

        mAdapter = SlimAdapter.create().register<FleaItem>(R.layout.flea_item_1) { item, i ->
            val icon = i.findViewById<ImageView>(R.id.fleaItemIcon)
            Glide.with(this).load(item.icon).into(icon)

            i.text(R.id.fleaItemName, item.name)
            i.text(R.id.fleaItemSubtitle, item.getUpdatedTime())
            i.text(R.id.fleaItemPrice, item.getCurrentPrice())
            i.text(R.id.fleaItemPriceSlot, item.getPricePerSlot())

            i.text(R.id.fleaItemChange, "${item.diff24h}%")

            val changeIcon = i.findViewById<ImageView>(R.id.fleaItemChangeIcon)
            when {
                item.diff24h!! > 0.0 -> {
                    i.textColor(R.id.fleaItemChange, resources.getColor(R.color.md_green_500))
                    i.image(R.id.fleaItemChangeIcon, R.drawable.ic_baseline_arrow_drop_up_24)
                    changeIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
                }
                item.diff24h < 0.0 -> {
                    i.textColor(R.id.fleaItemChange,  resources.getColor(R.color.md_red_500))
                    i.image(R.id.fleaItemChangeIcon, R.drawable.ic_baseline_arrow_drop_down_24)
                    changeIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
                }
                else -> {
                    i.textColor(R.id.fleaItemChange,  resources.getColor(R.color.primaryText60))
                    i.image(R.id.fleaItemChangeIcon, R.drawable.icons8_horizontal_line_96)
                    changeIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryText60))
                }
            }

            i.longClicked(R.id.itemCard) {
                val dialog = MaterialDialog(requireActivity()).show {
                    customListAdapter(mDialogAdaper)
                }
                mDialogAdaper.updateData(mutableListOf(Wiki(item.wikiLink!!, dialog), item))
                false
            }
        }

        viewModel.fleaItems.observe(viewLifecycleOwner) {
            updateData(mList = it, searchKey = currentSearchKey)
        }

        viewModel.searchKey.observe(viewLifecycleOwner) {
            currentSearchKey = it
            updateData(searchKey = it)
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun updateData(searchKey: String = "", mList: MutableList<FleaItem>? = null) {
        if (mList != null) this.mList = mList
        var nList = mList

        when (sortBy) {
            0 -> nList = this.mList?.filter { it.name!!.contains(searchKey, true) }?.sortedBy { it.name }?.toMutableList()
            1 -> nList = this.mList?.filter { it.name!!.contains(searchKey, true) }?.sortedByDescending { it.price }?.toMutableList()
            2 -> nList = this.mList?.filter { it.name!!.contains(searchKey, true) }?.sortedByDescending { (it.price!!/it.slots!!) }?.toMutableList()
        }

        mAdapter.updateData(nList?.filterNot { it.icon!!.isEmpty() })
        progressBar.visibility = View.GONE
    }

    private fun setupRecyclerView(view: View) {
        val mRecyclerView = view.findViewById<RecyclerView>(R.id.flea_list)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.attachTo(mRecyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.flea_market_main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flea_sort -> {
                MaterialDialog(requireActivity()).show {
                    listItemsSingleChoice(R.array.flea_sort, initialSelection = sortBy) { _, index, text ->
                        sortBy = index
                        updateData()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    data class Wiki (
        var url: String,
        val dialog: MaterialDialog
    )

    data class Favorite (
        var item: FleaItem
    )
}