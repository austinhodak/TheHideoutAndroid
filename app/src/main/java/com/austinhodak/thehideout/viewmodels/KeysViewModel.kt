package com.austinhodak.thehideout.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Key
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

class KeysViewModel(application: Application) : AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext

    var searchKey = MutableLiveData<String>()

    val keys: LiveData<List<Key>> = liveData {
        emit(getKeys())
    }

    private suspend fun getKeys(): List<Key> = withContext(Dispatchers.IO) {
        val groupListType: Type = object : TypeToken<ArrayList<Key?>?>() {}.type
        Gson().fromJson(context.resources.openRawResource(R.raw.keys).bufferedReader().use { it.readText() }, groupListType)
    }
}