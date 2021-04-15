package com.austinhodak.thehideout.bsg.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.bsg.models.mod.BsgMod
import com.austinhodak.thehideout.bsg.models.weapon.BsgWeapon
import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber

class BSGViewModel (application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private val allData by lazy {
        val liveData = MutableLiveData<List<Any>>()
        liveData.value = processBSGData(loadBSGData())
        return@lazy liveData
    }

    init {
        val data = allData.value
        val allMods = data?.filterIsInstance<BsgMod>()
        Timber.d(data?.sumBy {
            if (it is BsgMod) 1 else 0
        }.toString())

        val allModsForMP7A1: List<BsgMod> = ArrayList()
        val mp7a1 = data?.find { if (it is BsgWeapon) it._props.ShortName == "ADAR 2-15" else false } as BsgWeapon

        var list = mp7a1._props.Slots.flatMap {
            it._props.filters[0].Filter.map {
                allMods?.find { mod -> mod._id == it }
            }
        }

        Timber.d(list.map {
            it?._props?.Name?.trim()
        }.joinToString(separator = "\n"))
    }

    private fun loadBSGData(): JSONObject {
        return JSONObject(context.resources.openRawResource(R.raw.bsg_items).bufferedReader().use { it.readText() })
    }

    private fun processBSGData(data: JSONObject): List<Any> {
        val list: MutableList<Any> = ArrayList()
        for (i in data.keys()) {
            val item = data.getJSONObject(i)

            //Item is a weapon
            if (item.getJSONObject("_props").has("weapFireType")) {
                list.add(Gson().fromJson(item.toString(), BsgWeapon::class.java))
                continue
            }

            if (item.getJSONObject("_props").has("Prefab"))
            if (item.getJSONObject("_props").getJSONObject("Prefab").getString("path").contains("assets/content/items/mods")) {
                //Weapon mod
                list.add(Gson().fromJson(item.toString(), BsgMod::class.java))
                continue
            }
        }
        return list
    }
}