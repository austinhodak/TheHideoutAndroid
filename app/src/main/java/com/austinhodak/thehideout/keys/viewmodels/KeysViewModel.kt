package com.austinhodak.thehideout.keys.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.keys.models.Key
import com.austinhodak.thehideout.userRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type

class KeysViewModel(application: Application) : AndroidViewModel(application){
    private val context = getApplication<Application>().applicationContext
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var searchKey = MutableLiveData<String>()

    val keyList: LiveData<List<Key>> get() = _keyList
    private val _keyList = MutableLiveData<List<Key>>()

    init {
        loadKeys()
        //updateKeyStatus()
    }

    private fun updateKeyStatus() {
        userRef("/keys/have").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                snapshot.children.forEach { snap ->
                    //filteredKeyList?.find { it._id == snap.key }?.have = snap.value as Boolean
                    //keyList.value?.find { it._id == snap.key }?.have = snap.value as Boolean
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadKeys() {
        val timeout = Firebase.remoteConfig.getLong("cacheTimeout")

        if ((System.currentTimeMillis() - prefs.getLong("lastKeyLoad", 0)) > timeout) {
            //Loaded over 4 hours ago.

            val fleaRef = Firebase.storage.reference.child("keysData.json")

            val storagePath = File(context.filesDir, "the_hideout")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "keysData.json")

            if (!myFile.exists()) {
                _keyList.value = loadInitialKeys()
            }

            fleaRef.getFile(myFile).addOnSuccessListener {
                _keyList.value = loadKeysFromFile()

                prefs.edit {
                    putLong("lastKeyLoad", System.currentTimeMillis())
                }
            }.addOnFailureListener {
                Timber.e(it)
            }
        } else {
            _keyList.value = loadKeysFromFile()
        }
    }

    private fun loadKeysFromFile(): List<Key> {
        val storagePath = File(context.filesDir, "the_hideout")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

        val myFile = File(storagePath, "keysData.json")

        if (!myFile.exists()) {
            return loadInitialKeys()
        }

        val objectString = FileInputStream(myFile).bufferedReader().use { it.readText() }
        val groupListType: Type = object : TypeToken<ArrayList<Key?>?>() {}.type
        val list: List<Key> = Gson().fromJson(objectString, groupListType)
        //Timber.d("$list")
        return list
    }

    private fun loadInitialKeys(): List<Key> {
        val groupListType: Type = object : TypeToken<ArrayList<Key?>?>() {}.type
        return Gson().fromJson(context.resources.openRawResource(R.raw.keys).bufferedReader().use { it.readText() }, groupListType)
    }
}