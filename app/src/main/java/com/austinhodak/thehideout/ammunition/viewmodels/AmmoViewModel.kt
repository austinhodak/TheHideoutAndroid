package com.austinhodak.thehideout.ammunition.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.models.Ammo
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type

class AmmoViewModel(application: Application) : AndroidViewModel(application){

    private val context = getApplication<Application>().applicationContext
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val sortBy : LiveData<Int> get() = _sortBy
    private val _sortBy = MutableLiveData<Int>()

    val ammoList = MutableLiveData<List<Ammo>>()

    init {
        setSortBy(0)
        loadAmmo()
    }

    private fun loadAmmo() {
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
                ammoList.value = loadAmmoFromFile()

                prefs.edit {
                    putLong("lastAmmoLoad", System.currentTimeMillis())
                }
            }.addOnFailureListener {
                Timber.e(it)
            }
        } else {
            ammoList.value = loadAmmoFromFile()
        }
    }

    private fun loadAmmoFromFile(): List<Ammo> {
        val storagePath = File(context.filesDir, "the_hideout")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

        val myFile = File(storagePath, "ammoData.json")

        if (!myFile.exists()) {
            return loadInitialAmmo()
        }

        val objectString = FileInputStream(myFile).bufferedReader().use { it.readText() }
        val groupListType: Type = object : TypeToken<ArrayList<Ammo?>?>() {}.type
        val list: List<Ammo> = Gson().fromJson(objectString, groupListType)
        return list
    }

    private fun loadInitialAmmo(): List<Ammo> {
        val groupListType: Type = object : TypeToken<ArrayList<Ammo?>?>() {}.type
        return Gson().fromJson(context.resources.openRawResource(R.raw.ammo_data).bufferedReader().use { it.readText() }, groupListType)
    }

    fun setSortBy(int: Int) {
        _sortBy.value = int
    }

}