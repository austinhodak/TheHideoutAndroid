package com.austinhodak.thehideout.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.thehideout.flea
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.github.kittinunf.fuel.httpGet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

class FleaViewModel : ViewModel() {
    var fleaItems = MutableLiveData<MutableList<FleaItem>>()
    var searchKey = MutableLiveData<String>()

    init {
        getAllItemsRTDB()
    }

    private fun getAllItems() {
        Log.d("FLEA MARKET", "GET ALL ITEMS")
        val groupListType: Type = object : TypeToken<ArrayList<FleaItem?>?>() {}.type
        "https://tarkov-market.com/api/v1/items/all".httpGet().header(mapOf("x-api-key" to "29Iu6DcYUekij5sT")).responseString { request, response, result ->
            //Log.d("FLEA", "RESPONSE: ${result.get()}")
            val array = JSONArray(result.get())
            val list: MutableList<FleaItem> = Gson().fromJson(result.get(), groupListType)
            //Log.d("FLEA", list.toString())
            fleaItems.postValue(list)
        }
    }

    private fun getAllItemsRTDB() {
        Firebase.flea().child("items").addValueEventListener(object : ValueEventListener {
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

}