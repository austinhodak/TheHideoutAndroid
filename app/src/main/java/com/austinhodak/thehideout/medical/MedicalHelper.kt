package com.austinhodak.thehideout.medical

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.medical.models.Med
import com.austinhodak.thehideout.medical.models.Stim
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object MedicalHelper {
    private var objectString: String? = null
    private var stimObjectString: String? = null
    private var medList: List<Med>? = null
    private var stimList: List<Stim>? = null

    fun getMeds(context: Context): List<Med> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.meds).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Med?>?>() {}.type
        if (medList == null) {
            medList = Gson().fromJson(map.toString(), groupListType)
        }
        return medList!!.sortedBy { it.name }
    }

    fun getStims(context: Context): List<Stim> {
        if (stimObjectString == null) {
            stimObjectString = context.resources.openRawResource(R.raw.stims).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(stimObjectString)
        val groupListType: Type = object : TypeToken<ArrayList<Stim?>?>() {}.type
        if (stimList == null) {
            stimList = Gson().fromJson(map.toString(), groupListType)
        }
        return stimList!!.sortedBy { it.name }
    }
}