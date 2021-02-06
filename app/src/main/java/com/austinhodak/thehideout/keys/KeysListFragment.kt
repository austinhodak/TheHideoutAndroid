package com.austinhodak.thehideout.keys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.austinhodak.thehideout.databinding.FragmentKeysListBinding
import com.austinhodak.thehideout.viewmodels.KeysViewModel
import com.austinhodak.thehideout.viewmodels.models.Key
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"

class KeysListFragment : Fragment() {

    private var listener: ValueEventListener? = null
    private var filteredKeyList: List<Key>? = null
    private val sharedViewModel: KeysViewModel by activityViewModels()
    private var filterIndices = intArrayOf(0, 1, 2, 3, 4, 5)

    private lateinit var fastAdapter: FastAdapter<Key>
    private lateinit var itemAdapter: ItemAdapter<Key>

    private var _binding: FragmentKeysListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentKeysListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayoutManager = GridLayoutManager(context, 1)

        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter)

        binding.keyList.layoutManager = gridLayoutManager
        binding.keyList.itemAnimator = SlideUpAlphaAnimator()

        binding.keyList.adapter = fastAdapter

        itemAdapter.itemFilter.filterPredicate = { key: Key, string: CharSequence? ->
            key.name.contains(string.toString(), true) || key.map.contains(string.toString(), true) || key.getDetailsList().contains(string.toString(), true)
        }

        sharedViewModel.keyList.observe(viewLifecycleOwner) {
            //Have to do this or else it breaks :/
            val list = it.map { item ->
                Key(
                    item.icon,
                    item.name,
                    item.link,
                    item.map,
                    item.location,
                    item.door,
                    item.details,
                    item._id,
                    item.have
                )
            }
            filteredKeyList = list

            //Needed for initial animation.
            GlobalScope.launch(Dispatchers.Main) {
                delay(25)
                itemAdapter.set(list)
            }

            updateListener()
        }

        sharedViewModel.searchKey.observe(requireActivity(), { string ->
            itemAdapter.filter(string)
        })

        fastAdapter.onLongClickListener = { view, adapter, item, position ->
            item.toggleHaveStatus()
            false
        }

        /* view.findViewById<FloatingActionButton>(R.id.keysFilterFAB).setOnClickListener {
             MaterialDialog(requireActivity()).show {
                 listItemsMultiChoice(R.array.keys_filter_list, initialSelection = filterIndices) { dialog, indices, items ->
                     filterIndices = indices
                     filteredKeyList = filteredKeyList!!.filter { items.contains(it.map) }
                     mAdapter.submitList(filteredKeyList)
                 }
                 title(text = "Show Only")
                 positiveButton(text = "FILTER")
             }
         }*/
    }

    private fun updateListener() {
        if (listener != null) Firebase.database.getReference("users/${Firebase.auth.currentUser?.uid}/keys/have/").removeEventListener(listener!!)
        Firebase.database.getReference("users/${Firebase.auth.currentUser?.uid}/keys/have/").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { snap ->
                    filteredKeyList?.find { it._id == snap.key }?.have = snap.value as Boolean
                }

                fastAdapter.notifyAdapterDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = KeysListFragment()
    }
}