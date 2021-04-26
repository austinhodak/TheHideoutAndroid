package com.austinhodak.thehideout.flea_market.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.flea_market.models.PriceAlert
import com.austinhodak.thehideout.uid
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type

class FleaViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var searchKey = MutableLiveData<String>()

    val fleaItems = MutableLiveData<List<FleaItem>>()
    val priceAlerts = MutableLiveData<List<PriceAlert>>()

    init {
        loadData()
        loadPriceAlerts()
    }

    private fun loadData() {
        val minutes15 = 1000 * 60 * 15

        /*try {
            fleaItems.postValue(loadFleaDataFromFile())
        } catch (e: Exception) {
            e.printStackTrace()
        }
*/
        if ((System.currentTimeMillis() - prefs.getLong("lastFleaMarketLoad", 0)) > minutes15) {
            //Loaded over 15 minutes ago.
            val fleaRef = Firebase.storage.reference.child("fleaItems.json")

            val storagePath = File(context.filesDir, "the_hideout")
            if (!storagePath.exists()) {
                storagePath.mkdirs()
            }

            val myFile = File(storagePath, "fleaItems.json")

            fleaRef.getFile(myFile).addOnSuccessListener {
                fleaItems.postValue(loadFleaDataFromFile())

                prefs.edit {
                    putLong("lastFleaMarketLoad", System.currentTimeMillis())
                }
            }.addOnFailureListener {
                Timber.e(it)
            }
        } else {
            fleaItems.postValue(loadFleaDataFromFile())
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

    fun getItemById(uid: String): FleaItem? {
        return fleaItems.value?.find { it.uid == uid }
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

    fun addPriceAlert(spinner: AppCompatSpinner?, editText: TextInputEditText?, dialog: MaterialDialog, item: FleaItem) {
        val selected = when (spinner?.selectedItemPosition) {
            0 -> "below"
            else -> "above"
        }
        val price = editText?.text.toString().replace(",", "").toIntOrNull()

        if (price == null) {
            Toast.makeText(editText?.context, context.getString(R.string.error_whole_number), Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result
            val push = Firebase.database.getReference("priceAlerts").push().key
            Firebase.database.getReference("priceAlerts").child(push!!).setValue(mutableMapOf(
                "itemID" to item.uid,
                "price" to price,
                "token" to token,
                "uid" to uid(),
                "when" to selected
            )).addOnCompleteListener {
                dialog.dismiss()
            }
        })
    }

}