package com.austinhodak.thehideout.hideout.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.hideout.models.HideoutCraft
import com.austinhodak.thehideout.hideout.models.HideoutModule
import com.austinhodak.thehideout.utils.userRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.lang.reflect.Type

class HideoutViewModel(application: Application) : AndroidViewModel(application) {
    var moduleList = MutableLiveData<List<HideoutModule>>()
    var craftsList = MutableLiveData<List<HideoutCraft>>()
    var completedModules = MutableLiveData<UserFB.UserFBHideout>()

    private val context = getApplication<Application>().applicationContext

    init {
        getHideoutModules()
        getHideoutCrafts()
        loadModuleFirebase()
    }

    private fun getHideoutModules() {
        val objectString = context.resources.openRawResource(R.raw.hideout).bufferedReader().use { it.readText() }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<HideoutModule?>?>() {}.type
        moduleList.value = Gson().fromJson(map.toString(), groupListType)
    }

    private fun getHideoutCrafts() {
        val groupListType: Type = object : TypeToken<ArrayList<HideoutCraft?>?>() {}.type
        craftsList.value = Gson().fromJson(context.resources.openRawResource(R.raw.crafts).bufferedReader().use { it.readText() }, groupListType)
    }

    private suspend fun getHideoutCraftsAsync(): List<HideoutCraft> =  withContext(Dispatchers.IO){
        val groupListType: Type = object : TypeToken<ArrayList<HideoutCraft?>?>() {}.type
        Gson().fromJson(context.resources.openRawResource(R.raw.crafts).bufferedReader().use { it.readText() }, groupListType)
    }

    fun getHideoutByID(id: Int, callback: (result: HideoutModule) -> Unit) {
        callback.invoke(moduleList.value?.find { it.id == id }!!)
    }

    private fun loadModuleFirebase() {
        userRef("/hideout/").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completedModules.value = snapshot.getValue<UserFB.UserFBHideout>()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun getAvailableModules(mCompletedModules: MutableList<HideoutModule>): List<HideoutModule> {
        val available: MutableList<HideoutModule> = ArrayList()
        for (module in moduleList.value!!) {
            //Already built, skip./
            if (mCompletedModules.contains(module)) continue

            val requiredModules: MutableList<HideoutModule> = ArrayList()
            var bool = false

            for (requirement in module.require.filter { it.type == "module" }) {
                val m = moduleList.value!!.find { it.module == requirement.name && it.level == requirement.quantity }
                if (m != null) {
                    requiredModules.add(m)
                }

                if (!mCompletedModules.contains(m)) {
                    bool = false
                    break
                } else {
                    bool = true
                }
            }

            if (bool || module.require.none { it.type == "module" }) {
                available.add(module)
            }
        }
        return available
    }

    fun getLockedModules(mCompletedModules: MutableList<HideoutModule>): List<HideoutModule> {
        val locked: MutableList<HideoutModule> = ArrayList()
        for (module in moduleList.value!!) {
            //Already built, skip./
            if (mCompletedModules.contains(module)) continue

            val requiredModules: MutableList<HideoutModule> = ArrayList()
            var bool = false

            for (requirement in module.require.filter { it.type == "module" }) {
                val m = moduleList.value!!.find { it.module == requirement.name && it.level == requirement.quantity }
                if (m != null) {
                    requiredModules.add(m)
                }

                if (!mCompletedModules.contains(m)) {
                    bool = false
                } else {
                    bool = true
                    break
                }
            }

            if (!bool && !module.require.none { it.type == "module" }) {
                locked.add(module)
            }
        }
        return locked
    }
}