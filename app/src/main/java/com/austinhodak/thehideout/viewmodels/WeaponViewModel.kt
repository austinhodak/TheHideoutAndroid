package com.austinhodak.thehideout.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Weapon
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type


class WeaponViewModel(application: Application) : AndroidViewModel(application) {

    val weaponsList: LiveData<List<Weapon>> get() = _weaponsList
    private val _weaponsList = MutableLiveData<List<Weapon>>()

    private val context = getApplication<Application>().applicationContext
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        loadWeapons()
    }

    private fun loadWeapons() {
        val timeout = Firebase.remoteConfig.getLong("cacheTimeout")

        if ((System.currentTimeMillis() - prefs.getLong("lastWeaponLoad", 0)) > timeout) {
            //Loaded over 4 hours ago.

            val fleaRef = Firebase.storage.reference.child("weaponsData.json")

            val storagePath = File(context.filesDir, "the_hideout")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "weaponsData.json")

            if (!myFile.exists()) {
                _weaponsList.value = loadInitialWeapons()
            }

            fleaRef.getFile(myFile).addOnSuccessListener {
                _weaponsList.value = loadWeaponsFromFile()

                prefs.edit {
                    putLong("lastWeaponLoad", System.currentTimeMillis())
                }
            }.addOnFailureListener {
                Timber.e(it)
            }
        } else {
            _weaponsList.value = loadWeaponsFromFile()
        }
    }

    private fun loadWeaponsFromFile(): List<Weapon> {
        val storagePath = File(context.filesDir, "the_hideout")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

        val myFile = File(storagePath, "weaponsData.json")

        if (!myFile.exists()) {
            return loadInitialWeapons()
        }

        val objectString = FileInputStream(myFile).bufferedReader().use { it.readText() }
        val groupListType: Type = object : TypeToken<ArrayList<Weapon?>?>() {}.type
        val list: List<Weapon> = Gson().fromJson(objectString, groupListType)
        Timber.d("$list")
        return list
    }

    private fun loadInitialWeapons(): List<Weapon> {
        val groupListType: Type = object : TypeToken<ArrayList<Weapon?>?>() {}.type
        return Gson().fromJson(context.resources.openRawResource(R.raw.weapons).bufferedReader().use { it.readText() }, groupListType)
    }
}