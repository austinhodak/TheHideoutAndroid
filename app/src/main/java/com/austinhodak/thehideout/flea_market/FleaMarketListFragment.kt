package com.austinhodak.thehideout.flea_market

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.thehideout.MainActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.uid
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import net.idik.lib.slimadapter.SlimAdapter
import java.text.DecimalFormat

class FleaMarketListFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private var currentSearchKey: String = ""
    private var mList: MutableList<FleaItem>? = null
    lateinit var mAdapter: SlimAdapter
    private val viewModel: FleaViewModel by activityViewModels()
    private var sortBy = 1

    lateinit var fastAdapter: FastAdapter<FleaItem>
    lateinit var itemAdapter: ItemAdapter<FleaItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flea_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView(view)
        progressBar = view.findViewById(R.id.fleaPG)
        progressBar.visibility = View.GONE

        (activity as MainActivity).isSearchHidden(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun setupAdapter() {

        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter)



        val mDialogAdapter = SlimAdapter.create().register<String>(R.layout.dialog_list_item_1) { s, i ->

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
        }.register<PriceAlert>(R.layout.dialog_list_item_1) { price, i ->
            i.text(R.id.itemText, "Add Price Alert")
            i.image(R.id.itemIcon, R.drawable.ic_baseline_add_alert_24)

            i.clicked(R.id.itemTop) {
                price.dialog.dismiss()

                val alertDialog = MaterialDialog(requireActivity()).show {
                    title(text = "Add Price Alert")
                    customView(R.layout.dialog_add_price_alert)
                    positiveButton(text = "ADD") { dialog ->
                        val alertAddView = dialog.getCustomView()
                        val spinner = alertAddView.findViewById<AppCompatSpinner>(R.id.addAlertSpinner)
                        val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)

                        if (editText.text.toString().isEmpty()) {
                            editText.error = "Cannot be empty."
                        } else {
                            editText.error = null
                            addPriceAlert(spinner, editText, dialog, price.item)
                        }
                    }
                    negativeButton(text = "CANCEL") {
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
            mDialogAdapter.updateData(mutableListOf(PriceAlert(item, dialog), Wiki(item.wikiLink!!, dialog), item))
            false
        }

        viewModel.fleaItems.observe(viewLifecycleOwner) {
            Handler().postDelayed({
                updateData(mList = it.toMutableList(), searchKey = currentSearchKey)
            }, 50)
        }

        viewModel.searchKey.observe(viewLifecycleOwner) {
            currentSearchKey = it
            itemAdapter.filter(it)
            //updateData(searchKey = it)
        }

        itemAdapter.itemFilter.filterPredicate = { item: FleaItem, constraint: CharSequence? ->
            item.name?.contains(constraint.toString(), ignoreCase = true) == true
        }

    }

    private fun addPriceAlert(spinner: AppCompatSpinner?, editText: TextInputEditText?, dialog: MaterialDialog, item: FleaItem) {
        val selected = when (spinner?.selectedItemPosition) {
            0 -> "below"
            else -> "above"
        }
        val price = editText?.text.toString().replace(",", "").toInt()


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {

                return@OnCompleteListener
            }

            val token = task.result
            val push = Firebase.database.getReference("priceAlerts").push().key
            Firebase.database.getReference("priceAlerts").child(push!!).setValue(mutableMapOf(
                "itemID" to item.uid,
                "price" to price,
                "token" to token,
                "uid" to uid(),
                "when" to selected
            )).addOnCompleteListener {
                dialog.dismiss()
            }
        })
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
        itemAdapter.set(nList?.filterNot { it.icon!!.isEmpty() && it.img!!.isEmpty() } ?: emptyList())
        progressBar.visibility = View.GONE
    }

    private fun setupRecyclerView(view: View) {
        val mRecyclerView = view.findViewById<RecyclerView>(R.id.flea_list)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = SlideUpAlphaAnimator().apply {
            addDuration = 200
            removeDuration = 100
        }
        mRecyclerView.adapter = fastAdapter
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

    data class Wiki(
        var url: String,
        val dialog: MaterialDialog
    )

    data class Favorite(
        var item: FleaItem
    )

    data class PriceAlert(
        var item: FleaItem,
        var dialog: MaterialDialog
    )
}