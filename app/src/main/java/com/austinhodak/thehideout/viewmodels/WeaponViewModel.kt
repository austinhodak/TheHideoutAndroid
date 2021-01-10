package com.austinhodak.thehideout.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.austinhodak.thehideout.viewmodels.models.WeaponModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miguelcatalan.materialsearchview.SuggestionModel
import org.json.JSONArray
import java.lang.reflect.Type


class WeaponViewModel(application: Application) : AndroidViewModel(application) {

    private var objectString: String? = null
    var list: List<WeaponModel>? = null
    var weaponClasses = MutableLiveData<MutableList<String>>()

    private val context = getApplication<Application>().applicationContext

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
            stringList.add(SuggestionModel("${item.name} ${AmmoHelper.getCaliberByID(context, item.calibre)?.long_name}", item))
        }

        return  stringList
    }
}