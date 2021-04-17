package com.austinhodak.thehideout.bsg

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.bsg.models.ammo.BsgAmmo
import com.austinhodak.thehideout.bsg.models.mod.BsgMod
import com.austinhodak.thehideout.bsg.models.weapon.BsgWeapon
import com.austinhodak.thehideout.bsg.models.weapon.WeaponClass
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class BSGRepository @Inject constructor(@ApplicationContext context: Context) {

    val weaponClasses = mutableSetOf(
        WeaponClass("5447b5fc4bdc2d87278b4567", "Assault Carbine"),
        WeaponClass("5447b6194bdc2d67278b4567", "Marksman Rifle"),
        WeaponClass("5447b6094bdc2dc3278b4567", "Shotgun"),
        WeaponClass("5447bedf4bdc2d87278b4568", "Grenade Launcher"),
        WeaponClass("5447b6254bdc2dc3278b4568", "Sniper Rifle"),
        WeaponClass("5447b5cf4bdc2d65278b4567", "Handgun"),
        WeaponClass("5447b5f14bdc2d61278b4567", "Assault Rifle"),
        WeaponClass("5447b5e04bdc2d62278b4567", "SMG"),
        WeaponClass("5447bed64bdc2d97278b4568", "Machine Gun"),
    ).sortedBy { it.name }.toMutableSet()



    suspend fun getRawData(@ApplicationContext context: Context): JSONObject = withContext(Dispatchers.IO) {
        JSONObject(context.resources.openRawResource(R.raw.bsg_items).bufferedReader().use { it.readText() })
    }

    suspend fun processRawData(@ApplicationContext context: Context, raw: JSONObject? = null): List<Any> = withContext(Dispatchers.IO) {
        val data = raw ?: getRawData(context)
        val list: MutableList<Any> = ArrayList()
        for (i in data.keys()) {
            val item = data.getJSONObject(i)

            //Item is a weapon
            if (item.getJSONObject("_props").has("weapFireType")) {
                list.add(Gson().fromJson(item.toString(), BsgWeapon::class.java))
                continue
            }

            if (item.getJSONObject("_props").has("Prefab")) {
                if (item.getJSONObject("_props").getJSONObject("Prefab").getString("path").contains("assets/content/items/mods")) {
                    //Weapon mod
                    list.add(Gson().fromJson(item.toString(), BsgMod::class.java))
                    continue
                }
            }

            if (item.getJSONObject("_props").has("Caliber")) {
                list.add(Gson().fromJson(item.toString(), BsgAmmo::class.java))
            }
        }
        list
    }
}