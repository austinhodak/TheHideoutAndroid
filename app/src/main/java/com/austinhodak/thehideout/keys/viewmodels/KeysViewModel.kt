package com.austinhodak.thehideout.keys.viewmodels

import androidx.lifecycle.MutableLiveData
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KeysViewModel @Inject constructor(
    private val repository: TarkovRepo
) : SearchViewModel() {

    var sortBy = MutableLiveData(0)

    fun setSort(int: Int) {
        sortBy.value = int
    }

    private val _userData = MutableLiveData<User?>(null)
    val userData = _userData

    init {
        if (uid() != null) {
            questsFirebase.child("users/${uid()}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userData.value = snapshot.getValue<User>()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}