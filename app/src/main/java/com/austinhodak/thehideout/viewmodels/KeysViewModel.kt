package com.austinhodak.thehideout.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class KeysViewModel(application: Application) : AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext

    var searchKey = MutableLiveData<String>()

    val keys: LiveData<List<Key>> = liveData {
        emit(getKeys())
    }


    private suspend fun getKeys(): List<Key> = withContext(Dispatchers.IO) {
        Json.decodeFromString(context.resources.openRawResource(R.raw.keys).bufferedReader().use { it.readText() })
    }

    private val _keysData = MutableLiveData<List<Key>>()

    val keysData: LiveData<List<Key>> get() = _keysData

    private fun getData() {
        viewModelScope.launch {
            _keysData.value = getKeys()
        }
    }

    init {
        getData()
    }
}