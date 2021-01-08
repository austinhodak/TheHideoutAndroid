package com.austinhodak.thehideout.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

class KeysViewModel : ViewModel(){
    var searchKey = MutableLiveData<String>()
}