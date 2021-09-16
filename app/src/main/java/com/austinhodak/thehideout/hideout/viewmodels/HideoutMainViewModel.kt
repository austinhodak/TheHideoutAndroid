package com.austinhodak.thehideout.hideout.viewmodels

import androidx.lifecycle.MutableLiveData
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.hideout.HideoutFilter
import com.austinhodak.thehideout.utils.addQuotes
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HideoutMainViewModel @Inject constructor() : SearchViewModel() {

    private val _view = MutableLiveData(HideoutFilter.CURRENT)
    val view = _view

    fun setView(int: HideoutFilter) {
        _view.value = int
    }

    private val _userData = MutableLiveData<User?>(null)
    val userData = _userData

    fun buildModule(module: Hideout.Module) {
        userRefTracker("hideoutModules/${module.id?.addQuotes()}").setValue(
            mutableMapOf(
                "id" to module.id,
                "complete" to true
            )
        )

        module.require?.forEach { objective ->
            userRefTracker("hideoutObjectives/${objective?.id?.addQuotes()}").setValue(
                mutableMapOf(
                    "id" to objective?.id,
                    "progress" to objective?.quantity
                )
            )
        }
    }

    fun undoModule(module: Hideout.Module) {
        userRefTracker("hideoutModules/${module.id?.addQuotes()}").removeValue()

        module.require?.forEach { objective ->
            userRefTracker("hideoutObjectives/${objective?.id?.addQuotes()}").removeValue()
        }
    }

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