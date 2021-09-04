package com.austinhodak.thehideout.flea_market

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.FragmentFleaFavoritesListBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.utils.log
import com.austinhodak.thehideout.utils.logScreen
import com.austinhodak.thehideout.utils.userRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.idik.lib.slimadapter.SlimAdapter
import java.text.DecimalFormat

@AndroidEntryPoint
class FleaMarketFavoritesFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private var currentSearchKey: String = ""
    private var mList: MutableList<FleaItem>? = null
    lateinit var mAdapter: SlimAdapter
    private val viewModel: FleaViewModel by activityViewModels()
    private var sortBy = 1

    lateinit var fastAdapter: FastAdapter<FleaItem>
    lateinit var itemAdapter: ItemAdapter<FleaItem>

    var favoriteItems: MutableMap<String, Boolean> = HashMap()

    private var _binding: FragmentFleaFavoritesListBinding? = null
    private val binding get() = _binding!!

    lateinit var prefs: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFleaFavoritesListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        setupAdapter()
        setupRecyclerView(view)
        progressBar = view.findViewById(R.id.fleaPG)
        progressBar.visibility = View.GONE

        (activity as MainActivity).isSearchHidden(false)


        userRef("flea").child("favorites").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteItems.clear()
                snapshot.children.forEach {
                    favoriteItems[it.key.toString()] = it.value as Boolean
                }
                binding.fleaFavoritesEmptyLayout.visibility = if (favoriteItems.isEmpty()) View.VISIBLE else View.GONE
                updateData(currentSearchKey)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        logScreen("FleaMarketListFragment")
    }

    private fun setupAdapter() {
        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter)

        val mDialogAdapter = SlimAdapter.create().register<String>(R.layout.item_dialog_simple) { s, i ->

        }.register<Wiki>(R.layout.item_dialog_simple) { wiki, i ->
            i.text(R.id.itemText, getString(R.string.wiki_goto))
            i.image(R.id.itemIcon, R.drawable.icons8_website_96)

            i.clicked(R.id.itemTop) {
                log(FirebaseAnalytics.Event.SELECT_ITEM, wiki.url, wiki.item.name ?: "", "wiki")
                wiki.dialog.dismiss()
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse(wiki.url))
            }
        }.register<FleaItem>(R.layout.item_dialog_subtitle) { item, i ->
            val top = i.findViewById<ConstraintLayout>(R.id.itemTop)
            top.isClickable = false

            i.text(R.id.itemText, item.traderName)
            i.text(R.id.itemRightText, item.getCurrentTraderPrice())
            i.image(R.id.itemIcon, R.drawable.ic_baseline_groups_24)
        }.register<PriceAlert>(R.layout.item_dialog_simple) { price, i ->
            i.text(R.id.itemText, R.string.price_alert_add)
            i.image(R.id.itemIcon, R.drawable.ic_baseline_add_alert_24)

            i.clicked(R.id.itemTop) {
                log("alert_add_click", price.item.uid ?: "", price.item.name ?: "", "flea_item")

                price.dialog.dismiss()

                val alertDialog = MaterialDialog(requireActivity()).show {
                    title(res = R.string.price_alert_add)
                    customView(R.layout.dialog_add_price_alert)
                    positiveButton(res = R.string.add) { dialog ->
                        val alertAddView = dialog.getCustomView()
                        val spinner = alertAddView.findViewById<AppCompatSpinner>(R.id.addAlertSpinner)
                        val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)

                        if (editText.text.toString().isEmpty()) {
                            editText.error = getString(R.string.error_empty)
                        } else {
                            editText.error = null
                            viewModel.addPriceAlert(spinner, editText, dialog, price.item)
                        }
                    }
                    negativeButton(res = R.string.cancel) {
                        dismiss()
                    }
                    noAutoDismiss()
                }
                val alertAddView = alertDialog.getCustomView()
                val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        editText.removeTextChangedListener(this)

                        try {
                            var givenstring = s.toString()
                            val longval: Long
                            if (givenstring.contains(",")) {
                                givenstring = givenstring.replace(",".toRegex(), "")
                            }
                            longval = givenstring.toLong()
                            val formatter = DecimalFormat("#,###,###")
                            val formattedString: String = formatter.format(longval)
                            editText.setText(formattedString)
                            editText.setSelection(editText.text?.length ?: 0)
                            // to place the cursor at the end of text
                        } catch (nfe: NumberFormatException) {
                            nfe.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        editText.addTextChangedListener(this)
                    }
                })
            }
        }

        fastAdapter.onLongClickListener = { view, adapter, item, pos ->
            val dialog = MaterialDialog(requireActivity()).show {
                customListAdapter(mDialogAdapter)
            }
            mDialogAdapter.updateData(mutableListOf(PriceAlert(item, dialog), Wiki(item.wikiLink!!, dialog, item), item))
            false
        }

        fastAdapter.onClickListener = { view, adapter, item, pos ->
            log(FirebaseAnalytics.Event.SELECT_ITEM, item.uid ?: "", item.name ?: "", "flea_item")

            false
        }

        viewModel.fleaItems.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                delay(25)
                updateData(mList = it.toMutableList(), searchKey = currentSearchKey)
            }
        }

        viewModel.searchKey.observe(viewLifecycleOwner) {
            currentSearchKey = it
            itemAdapter.filter(it)
        }

        itemAdapter.itemFilter.filterPredicate = { item: FleaItem, constraint: CharSequence? ->
            item.name?.contains(constraint.toString(), ignoreCase = true) == true
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun updateData(searchKey: String = "", mList: MutableList<FleaItem>? = null) {
        if (mList != null) this.mList = mList
        var nList = mList

        when (sortBy) {
            0 -> nList = this.mList?.filter { it.name!!.contains(searchKey, true) }?.sortedBy { it.name }?.toMutableList()
            1 -> nList = this.mList?.filter { it.name!!.contains(searchKey, true) }?.sortedByDescending { it.price }?.toMutableList()
            2 -> nList = this.mList?.filter { it.name!!.contains(searchKey, true) }?.sortedByDescending { (it.price!! / it.slots!!) }?.toMutableList()
        }

        itemAdapter.clear()
        if (nList != mList)
        itemAdapter.set(nList?.filterNot { it.icon!!.isEmpty() && it.img!!.isEmpty() }?.filter { favoriteItems.getOrDefault(it.uid, false) } ?: emptyList())
        progressBar.visibility = View.GONE
    }

    private fun setupRecyclerView(view: View) {
        val mRecyclerView = view.findViewById<RecyclerView>(R.id.flea_list)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = SlideUpAlphaAnimator().apply {
            addDuration = 150
            removeDuration = 100
        }
        mRecyclerView.adapter = fastAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_flea_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flea_sort -> {
                MaterialDialog(requireActivity()).show {
                    title(text = getString(R.string.sort_by))
                    listItemsSingleChoice(R.array.flea_sort, initialSelection = sortBy) { _, index, text ->
                        sortBy = index
                        updateData()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    data class Wiki(
        var url: String,
        val dialog: MaterialDialog,
        var item: FleaItem
    )

    data class Favorite(
        var item: FleaItem
    )

    data class PriceAlert(
        var item: FleaItem,
        var dialog: MaterialDialog
    )
}