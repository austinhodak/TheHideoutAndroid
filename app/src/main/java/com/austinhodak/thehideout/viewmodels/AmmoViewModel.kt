package com.austinhodak.thehideout.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AmmoViewModel(application: Application) : AndroidViewModel(application){
    val sortBy : LiveData<Int> get() = _sortBy
    private val _sortBy = MutableLiveData<Int>()

    val data: LiveData<List<CaliberModel>> = liveData {
        emit(loadAmmo())
    }

    private val context = getApplication<Application>().applicationContext

    init {
        setSortBy(0)
    }

    fun setSortBy(int: Int) {
        _sortBy.value = int
    }

    private suspend fun loadAmmo(): List<CaliberModel> = withContext(Dispatchers.IO) {
        Json { ignoreUnknownKeys = true }.decodeFromString( context.resources.openRawResource(R.raw.ammo).bufferedReader().use { it.readText() } )
    }

}