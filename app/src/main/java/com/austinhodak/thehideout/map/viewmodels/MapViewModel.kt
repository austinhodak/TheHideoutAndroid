package com.austinhodak.thehideout.map.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.tarkovapi.models.MapInteractive
import com.austinhodak.tarkovapi.models.QuestExtra
import com.austinhodak.tarkovapi.utils.QuestExtraHelper
import com.austinhodak.thehideout.R
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val _map = MutableLiveData("customs")
    val map = _map

    private val _mapData = MutableLiveData<MapInteractive>(null)
    val mapData = _mapData

    private val _selectedGroups = MutableLiveData<MutableList<Int>?>(null)
    val selectedGroups = _selectedGroups

    private val _questsExtras = MutableLiveData<List<QuestExtra.QuestExtraItem>>()
    val questsExtra = _questsExtras

    fun removeGroup(int: Int) {
        _selectedGroups.value?.remove(int)
        _selectedGroups.postValue(_selectedGroups.value)
        Timber.d(_selectedGroups.value.toString())
    }

    fun addGroup(int: Int) {
        _selectedGroups.value?.add(int)
        _selectedGroups.postValue(_selectedGroups.value)
        Timber.d(_selectedGroups.value.toString())
    }

    fun setMap(map: String, context: Context) {
        _map.value = map
        updateMap(context)
    }

    init {
        updateMap(context)
        _questsExtras.value = QuestExtraHelper.getQuests(context = context)
    }

    private fun updateMap(context: Context) {
        _mapData.value = Gson().fromJson(
            context.resources.openRawResource(getMapRaw()).bufferedReader().use { it.readText() },
            MapInteractive::class.java
        )?.also {
            _selectedGroups.value = arrayListOf()
            it.groups?.forEach { group ->
                group?.categories?.forEach { category ->
                    category?.id?.let { _ ->
                        //addGroup(id)
                    }
                }
            }
        }

    }

    private fun getMapRaw(map: String? = _map.value): Int {
        return when (_map.value) {
            "customs" -> R.raw.map_customs
            "reserve" -> R.raw.map_reserve
            "interchange" -> R.raw.map_interchange
            "shoreline" -> R.raw.map_shoreline
            "factory" -> R.raw.map_factory
            "the lab" -> R.raw.map_labs
            "woods" -> R.raw.map_woods
            "lighthouse" -> R.raw.map_lighthouse
            "lighthouse_dark" -> R.raw.map_lighthouse_dark
            else -> R.raw.map_interchange
        }
    }
}