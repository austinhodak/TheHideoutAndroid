package com.austinhodak.thehideout.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

class AmmoViewModel(application: Application) : AndroidViewModel(application){

    private val context = getApplication<Application>().applicationContext

    val sortBy : LiveData<Int> get() = _sortBy
    private val _sortBy = MutableLiveData<Int>()

    val data: LiveData<List<CaliberModel>> = liveData {
        emit(loadAmmo())
    }

    val allAmmoList: LiveData<List<AmmoModel>> = liveData {
        val list = loadAmmo()
        emit(list.flatMap { it.ammo })
    }

    init {
        setSortBy(0)
    }

    fun setSortBy(int: Int) {
        _sortBy.value = int
    }

    private suspend fun loadAmmo(): List<CaliberModel> = withContext(Dispatchers.IO) {
        val groupListType: Type = object : TypeToken<ArrayList<CaliberModel?>?>() {}.type
        Gson().fromJson(context.resources.openRawResource(R.raw.ammo).bufferedReader().use { it.readText() }, groupListType)
    }

    suspend fun getAmmoList(id: String): List<AmmoModel>? = withContext(Dispatchers.IO) {
        data.value?.find { it._id == id }?.ammo
    }

    /*fun getAmmoList(id: String) {
        viewModelScope.launch {
            sortedAmmoList.value = data.value?.find { it._id == id }?.ammo
        }
    }*/


}