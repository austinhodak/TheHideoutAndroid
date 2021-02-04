package com.austinhodak.thehideout.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.austinhodak.thehideout.flea
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.uid
import com.austinhodak.thehideout.viewmodels.models.PriceAlert
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type

class FleaViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var fleaItems = MutableLiveData<MutableList<FleaItem>>()
    var searchKey = MutableLiveData<String>()

    val data = MutableLiveData<List<FleaItem>>()
    val priceAlerts = MutableLiveData<List<PriceAlert>>()

    init {
        //getFunction()
        loadPriceAlerts()
        loadData()
    }

    private fun loadData() {
        val minutes15 = 1000 * 60 * 15

        if ((System.currentTimeMillis() - prefs.getLong("lastFleaMarketLoad", 0)) > minutes15) {
            //Loaded over 15 minutes ago.
            val fleaRef = Firebase.storage.reference.child("fleaItems.json")

            val storagePath = File(context.filesDir, "the_hideout")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "fleaItems.json")

            fleaRef.getFile(myFile).addOnSuccessListener {
                // Local temp file has been created
                data.postValue(loadFleaDataFromFile())
                Log.d("FLEA", "LOADED")

                prefs.edit {
                    putLong("lastFleaMarketLoad", System.currentTimeMillis())
                }
            }.addOnFailureListener {
                // Handle any errors
                Log.e("FLEA", it.toString())
            }
        } else {
            data.postValue(loadFleaDataFromFile())
        }
    }

    private fun loadFleaDataFromFile(): List<FleaItem> {
        val storagePath = File(context.filesDir, "the_hideout")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

        val objectString = FileInputStream(File(storagePath, "fleaItems.json")).bufferedReader().use { it.readText() }
        val groupListType: Type = object : TypeToken<ArrayList<FleaItem?>?>() {}.type
        return Gson().fromJson(objectString, groupListType)
    }

    private fun getFunction() {
        Firebase.functions.getHttpsCallable("getFleaMarketItems").call().addOnSuccessListener { task ->
            val result = task?.data as String
            val groupListType: Type = object : TypeToken<ArrayList<FleaItem?>?>() {}.type
            data.postValue(Gson().fromJson(result, groupListType))
        }
    }

    private fun getAllItemsRTDB() {
        flea().child("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.map {
                    it.getValue<FleaItem>()!!
                }.toMutableList()
                fleaItems.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun getItemById(uid: String): FleaItem? {
        return data.value?.find { it.uid == uid }
    }

    private fun loadPriceAlerts() {
        Firebase.database.getReference("priceAlerts").orderByChild("uid").equalTo(uid()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.map {
                    val alert = it.getValue<PriceAlert>()!!
                    alert.reference = it.ref
                    alert
                }.toMutableList()
                priceAlerts.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}