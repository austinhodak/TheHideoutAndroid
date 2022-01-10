package com.austinhodak.thehideout.team.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
class TeamManagementViewModel @Inject constructor() : ViewModel() {

    private val _userData = MutableLiveData<User?>(null)
    val userData = _userData

    init {
        if (uid() != null) {
            questsFirebase.child("users/${uid()}").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userData.value = snapshot.getValue<User>()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

}