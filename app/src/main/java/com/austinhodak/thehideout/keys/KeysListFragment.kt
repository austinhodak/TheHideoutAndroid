package com.austinhodak.thehideout.keys

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.KeysViewModel
import com.austinhodak.thehideout.viewmodels.models.Key
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import net.idik.lib.slimadapter.SlimAdapter

private const val ARG_PARAM1 = "param1"

class KeysListFragment : Fragment() {

    private lateinit var adapter: SlimAdapter
    private var param1: String = ""
    private var keyList: List<Key>? = null
    private var filteredKeyList: List<Key>? = null
    private val sharedViewModel: KeysViewModel by activityViewModels()
    private var filterIndices = intArrayOf(0, 1, 2, 3, 4, 5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1).toString()
        }

        //keyList = KeysHelper.getKeys(requireContext(), true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_keys_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView = view.findViewById<RecyclerView>(R.id.ammo_list)
        recyclerView.layoutManager = linearLayoutManager

        sharedViewModel.keys.observe(viewLifecycleOwner) {
            filteredKeyList = it
            adapter.updateData(it)
        }

        adapter = SlimAdapter.create().register<Key>(R.layout.key_list_item) { key, i ->
            var need = false

            i.text(R.id.keyName, key.name)
            i.text(R.id.keySubtitle, key.map)

            val useless = i.findViewById<ImageView>(R.id.keyUseless)
            val rubles = i.findViewById<ImageView>(R.id.keyRubles)
            val quest = i.findViewById<ImageView>(R.id.keyQuest)
            val indicator = i.findViewById<View>(R.id.keyIndicator)

            indicator.visibility = View.GONE

            useless.visibility = if (key.details.contains("useless")) View.VISIBLE else View.GONE
            rubles.visibility = if (key.details.contains("expensive")) View.VISIBLE else View.GONE
            quest.visibility = if (key.details.contains("quest")) View.VISIBLE else View.GONE

            Glide.with(this).load(key.icon)
                .into(i.findViewById(R.id.keyImage))

            i.longClicked(R.id.keyRoot) {
                Firebase.database.getReference("users/${Firebase.auth.currentUser?.uid}/keys/have/${key._id}").setValue(!need)
                true
            }

            Firebase.database.getReference("users/${Firebase.auth.currentUser?.uid}/keys/have/${key._id}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    indicator.visibility = if (snapshot.exists() && snapshot.value == true)  View.VISIBLE else View.GONE

                    need = snapshot.exists() && snapshot.value == true
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }.attachTo(recyclerView)

        sharedViewModel.searchKey.observe(requireActivity(), { string ->
            Log.d("KEYSEARCH", string)
            if (string.isNullOrEmpty()) {
                adapter.updateData(filteredKeyList!!)
            } else {
                adapter.updateData(filteredKeyList!!.filter { it.name.contains(string, true) || it.map.contains(string, true) || it.getDetails().contains(string, true) })
            }

        })

        view.findViewById<FloatingActionButton>(R.id.keysFilterFAB).setOnClickListener {
            MaterialDialog(requireActivity()).show {
                listItemsMultiChoice(R.array.keys_filter_list, initialSelection = filterIndices) { dialog, indices, items ->
                    filterIndices = indices
                    filteredKeyList = filteredKeyList!!.filter { items.contains(it.map) }
                    adapter.updateData(filteredKeyList)
                }
                title(text = "Show Only")
                positiveButton(text = "FILTER")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: Int) =
            KeysListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}