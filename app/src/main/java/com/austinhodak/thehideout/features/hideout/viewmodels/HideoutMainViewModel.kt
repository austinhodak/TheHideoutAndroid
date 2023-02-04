package com.austinhodak.thehideout.features.hideout.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.features.hideout.HideoutFilter
import com.austinhodak.thehideout.utils.addQuotes
import com.austinhodak.thehideout.utils.userFirestore

import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HideoutMainViewModel @Inject constructor() : SearchViewModel() {

    var sortBy = MutableLiveData(UserSettingsModel.craftSort.value)

    fun setSort(int: Int) {
        sortBy.value = int
        viewModelScope.launch {
            UserSettingsModel.craftSort.update(int)
        }
    }

    var filterAvailable = MutableLiveData(false)

    fun setFilterAvailable(boolean: Boolean) {
        filterAvailable.value = boolean
    }

    private val _view = MutableLiveData(HideoutFilter.CURRENT)
    val view = _view

    fun setView(int: HideoutFilter) {
        _view.value = int
    }

    fun buildModule(module: Hideout.Module) {
        val objectiveMap = module.require?.associateBy({it?.id?.toString()}, {
            mapOf(
                "completed" to true,
                "timestamp" to Timestamp.now(),
                "progress" to it?.quantity
            )
        })

        userFirestore()?.set(
            hashMapOf(
                "progress" to hashMapOf(
                    "hideoutModules" to hashMapOf(
                        module.id?.toString() to hashMapOf(
                            "completed" to true,
                            "timestamp" to Timestamp.now()
                        )
                    ),
                    "hideoutObjectives" to objectiveMap
                )
            ),
            SetOptions.merge()
        )

        module.require
            ?.filter { it?.type == "item" }
            ?.forEach {
                val itemID = it?.name
                val quantity = it?.quantity ?: 0
                if (quantity > 500) return@forEach
                userRefTracker("items/$itemID/hideoutObjective/${it?.id?.addQuotes()}").removeValue()
            }
    }

    fun undoModule(module: Hideout.Module) {
        userFirestore()?.set(
            hashMapOf(
                "progress" to hashMapOf(
                    "hideoutModules" to hashMapOf(
                        module.id?.toString() to FieldValue.delete()
                    )
                )
            ),
            SetOptions.merge()
        )


        module.require?.forEach { objective ->
            userFirestore()?.set(
                hashMapOf(
                    "progress" to hashMapOf(
                        "hideoutObjectives" to hashMapOf(
                            objective?.id?.toString() to FieldValue.delete()
                        )
                    )
                ),
                SetOptions.merge()
            )
        }
    }
}