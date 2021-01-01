package com.austinhodak.thehideout.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

class AmmoSharedViewModel : ViewModel(){
    val sortBy : LiveData<Int> get() = _sortBy
    private val _sortBy = MutableLiveData<Int>()
    var caliberData = MutableLiveData<List<CaliberModel>>()

    init {
        setSortBy(0)
    }

    fun setSortBy(int: Int) {
        _sortBy.value = int
    }

    fun getCaliberData(context: Context) {
        val objectString = context.resources.openRawResource(R.raw.ammo).bufferedReader().use { it.readText() }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<CaliberModel?>?>() {}.type
        caliberData.value = Gson().fromJson(map.toString(), groupListType)

    }

}