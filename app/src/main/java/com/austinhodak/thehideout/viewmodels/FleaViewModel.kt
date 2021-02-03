package com.austinhodak.thehideout.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class FleaViewModel : ViewModel() {
    var fleaItems = MutableLiveData<MutableList<FleaItem>>()
    var searchKey = MutableLiveData<String>()

    val data = MutableLiveData<List<FleaItem>>()
    val priceAlerts = MutableLiveData<List<PriceAlert>>()

    init {
        getFunction()
        loadPriceAlerts()
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