package com.austinhodak.thehideout.weapons.builder.viewmodel

import androidx.lifecycle.MutableLiveData
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.firebase.WeaponBuildFirestore
import com.austinhodak.thehideout.utils.uid
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeaponLoadoutViewModel @Inject constructor(
        private val tarkovRepo: TarkovRepo,
        private val modsRepo: ModsRepo
) : SearchViewModel() {

    private val _userLoadouts = MutableLiveData<List<WeaponBuildFirestore>?>(null)
    val userLoadouts = _userLoadouts

    init {
        Firebase.firestore.collection("loadouts").whereEqualTo("uid", uid()).addSnapshotListener { value, error ->
            val loadouts = ArrayList<WeaponBuildFirestore>()
            for (doc in value!!) {
                val loadout = doc.toObject<WeaponBuildFirestore>()
                loadout.id = doc.id
                loadouts.add(loadout)
            }
            _userLoadouts.value = loadouts
        }
    }
}