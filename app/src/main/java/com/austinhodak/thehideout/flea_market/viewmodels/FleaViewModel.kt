package com.austinhodak.thehideout.flea_market.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.utils.userRefTracker
import com.austinhodak.thehideout.workmanager.PriceUpdateWorker
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FleaViewModel @Inject constructor(
    private val tarkovRepo: TarkovRepo,
    @ApplicationContext context: Context
) : SearchViewModel() {

    private var mContext: Context = context

    private val _item by lazy { MutableLiveData<Item>() }
    val item: LiveData<Item> get() = _item

    fun getItemByID(id: String) = viewModelScope.launch {
        tarkovRepo.getItemByID(id).collect {
            _item.value = it
        }
    }

    var sortBy = MutableLiveData(UserSettingsModel.fleaSort.value)

    fun setSort(int: Int) {
        sortBy.value = int
        viewModelScope.launch {
            UserSettingsModel.fleaSort.update(int)
        }
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


    fun refreshList() {
        viewModelScope.launch {
            val work = OneTimeWorkRequestBuilder<PriceUpdateWorker>().build()
            WorkManager.getInstance(mContext).enqueue(work)
        }
    }
}