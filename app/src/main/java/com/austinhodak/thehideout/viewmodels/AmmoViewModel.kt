package com.austinhodak.thehideout.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.austinhodak.thehideout.viewmodels.models.firestore.FSAmmo
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type

class AmmoViewModel(application: Application) : AndroidViewModel(application){

    private val context = getApplication<Application>().applicationContext
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val sortBy : LiveData<Int> get() = _sortBy
    private val _sortBy = MutableLiveData<Int>()

    val ammoList = MutableLiveData<List<FSAmmo>>()

    init {
        setSortBy(0)

        val timeout = Firebase.remoteConfig.getLong("cacheTimeout")

        if ((System.currentTimeMillis() - prefs.getLong("lastAmmoLoad", 0)) > timeout) {
            //Loaded over 4 hours ago.

            val fleaRef = Firebase.storage.reference.child("ammoData.json")

            val storagePath = File(context.filesDir, "the_hideout")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "ammoData.json")

            if (!myFile.exists()) {
                ammoList.value = loadInitialAmmo()
            }

            fleaRef.getFile(myFile).addOnSuccessListener {
                // Local temp file has been created
                ammoList.value = loadAmmoFromFile()

                prefs.edit {
                    putLong("lastAmmoLoad", System.currentTimeMillis())
                }
            }.addOnFailureListener {
                // Handle any errors
                Log.e("AMMO", it.toString())
            }
        } else {
            ammoList.value = loadAmmoFromFile()
        }
    }

    private fun loadAmmoFromFile(): List<FSAmmo> {
        val storagePath = File(context.filesDir, "the_hideout")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

        val myFile = File(storagePath, "ammoData.json")

        if (!myFile.exists()) {
            return loadInitialAmmo()
        }

        val objectString = FileInputStream(myFile).bufferedReader().use { it.readText() }
        val groupListType: Type = object : TypeToken<ArrayList<FSAmmo?>?>() {}.type
        val list: List<FSAmmo> = Gson().fromJson(objectString, groupListType)
        Log.d("AMMO", "${list.toString()}")
        return list
    }

    private fun loadInitialAmmo(): List<FSAmmo> {
        val groupListType: Type = object : TypeToken<ArrayList<FSAmmo?>?>() {}.type
        return Gson().fromJson(context.resources.openRawResource(R.raw.ammo_data).bufferedReader().use { it.readText() }, groupListType)
    }

    fun setSortBy(int: Int) {
        _sortBy.value = int
    }

    private suspend fun loadAmmo(): List<CaliberModel> = withContext(Dispatchers.IO) {
        val groupListType: Type = object : TypeToken<ArrayList<CaliberModel?>?>() {}.type
        Gson().fromJson(context.resources.openRawResource(R.raw.ammo).bufferedReader().use { it.readText() }, groupListType)
    }

}