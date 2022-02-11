package com.austinhodak.thehideout.keys.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.utils.userFirestore

import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeysViewModel @Inject constructor() : SearchViewModel() {

    var sortBy = MutableLiveData(UserSettingsModel.keySort.value)

    fun setSort(int: Int) {
        sortBy.value = int
        viewModelScope.launch {
            UserSettingsModel.keySort.update(int)
        }
    }

    fun toggleKey(item: Item, fsUser: FSUser) {
        if (fsUser.hasKey(item)) {
            userFirestore()?.update("keys.${item.id}", FieldValue.delete())
        } else {
            userFirestore()?.update("keys.${item.id}", true)
        }
    }
}