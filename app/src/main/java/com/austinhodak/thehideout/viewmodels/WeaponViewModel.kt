package com.austinhodak.thehideout.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.WeaponModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miguelcatalan.materialsearchview.SuggestionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.lang.reflect.Type


class WeaponViewModel(application: Application) : AndroidViewModel(application) {

    private var objectString: String? = null
    var list: List<WeaponModel>? = null
    var weaponClasses = MutableLiveData<MutableList<String>>()

    private val context = getApplication<Application>().applicationContext

    val data: LiveData<List<WeaponModel>> = liveData {
        emit(loadWeapon())
    }

    private suspend fun loadWeapon(): List<WeaponModel> = withContext(Dispatchers.IO) {
        val list: List<WeaponModel> = Json { ignoreUnknownKeys = true }.decodeFromString( context.resources.openRawResource(R.raw.weapons).bufferedReader().use { it.readText() } )
        for (weapon in list) {
            if (weaponClasses.value?.contains(weapon.`class`) == false) {
                weaponClasses.value?.add(weapon.`class`)
            }
        }
        list
    }

    init {
        loadWeapons()
    }

    private fun loadWeapons(): List<WeaponModel> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.weapons).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<WeaponModel?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }

        for (weapon in list!!) {
            if (weaponClasses.value?.contains(weapon.`class`) == false) {
                weaponClasses.value?.add(weapon.`class`)
            } else {

            }
        }

        return list!!.sortedBy { it.name }
    }

    fun getWeaponByID(id: String): WeaponModel {
        return list!!.find { it._id == id }!!
    }

    fun getAllWeaponSearch(): MutableList<SuggestionModel> {
        val ammoList: MutableList<WeaponModel> = ArrayList()
        val stringList: MutableList<SuggestionModel> = ArrayList()
        for (item in list!!) {
            stringList.add(SuggestionModel(item = item))
            //stringList.add(SuggestionModel("${item.name} ${AmmoHelper.getCaliberByID(context, item.calibre)?.long_name}", item))
        }

        return  stringList
    }
}