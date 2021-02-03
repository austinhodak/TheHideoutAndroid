package com.austinhodak.thehideout.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.austinhodak.thehideout.viewmodels.models.FSAmmo
import com.austinhodak.thehideout.viewmodels.models.firestore.FSCaliber
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

class AmmoViewModel(application: Application) : AndroidViewModel(application){

    private val context = getApplication<Application>().applicationContext

    val sortBy : LiveData<Int> get() = _sortBy
    private val _sortBy = MutableLiveData<Int>()

    val caliberList = MutableLiveData<List<FSCaliber>>()
    val ammoList = MutableLiveData<List<FSAmmo>>()

    val data: LiveData<List<CaliberModel>> = liveData {
        emit(loadAmmo())
    }

    val allAmmoList: LiveData<List<AmmoModel>> = liveData {
        val list = loadAmmo()
        emit(list.flatMap { it.ammo })
    }

    init {
        setSortBy(0)
        loadCalibersFirestore()
        loadAmmoFirestore()
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

    private fun loadCalibersFirestore() {
        Firebase.firestore.collection("calibers").get(Source.CACHE).addOnSuccessListener { docs ->
            Log.d("AMMO", "CALIBERS LOADED")

            val list: MutableList<FSCaliber> = ArrayList()

            for (doc in docs) {
                val caliber = doc.toObject<FSCaliber>()
                caliber._id = doc.id
                list.add(caliber)
            }

            caliberList.postValue(list)
        }
    }

    private fun loadAmmoFirestore() {
        Firebase.firestore.collectionGroup("ammo").get(Source.CACHE).addOnSuccessListener { docs ->
            Log.d("AMMO", "AMMO LOADED")

            val list: MutableList<FSAmmo> = ArrayList()

            for (doc in docs) {
                val ammo = doc.toObject<FSAmmo>()
                ammo._id = doc.id
                list.add(ammo)
            }

            ammoList.postValue(list)
        }
    }

    fun getAmmoByCaliber(caliberID: String): List<FSAmmo>? {
        return ammoList.value?.filter { it.caliber == caliberID }
    }

    /*fun getAmmoList(id: String) {
        viewModelScope.launch {
            sortedAmmoList.value = data.value?.find { it._id == id }?.ammo
        }
    }*/


}