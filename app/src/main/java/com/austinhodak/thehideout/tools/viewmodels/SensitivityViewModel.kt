package com.austinhodak.thehideout.tools.viewmodels

import androidx.lifecycle.MutableLiveData
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.utils.toSimArmor
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SensitivityViewModel @Inject constructor(

) : SearchViewModel() {

    private val _selectedHelmet = MutableLiveData<Item>(null)
    val selectedHelmet = _selectedHelmet

    fun selectArmor(armor: Item) {
        _selectedArmor.value = armor
        updateChange()
    }

    private val _selectedArmor = MutableLiveData<Item>(null)
    val selectedArmor = _selectedArmor

    fun selectHelmet(armor: Item) {
        _selectedHelmet.value = armor
        updateChange()
    }

    private val _change = MutableLiveData(0)
    val change = _change

    private val _newDPI = MutableLiveData(UserSettingsModel.dpi.value)
    val newDPI = _newDPI

    private val _newHipfire = MutableLiveData(UserSettingsModel.hipfireSens.value.toDouble())
    val newHipfire = _newHipfire

    private fun updateChange() {
        val userDPI = UserSettingsModel.dpi.value
        val userHipfire = UserSettingsModel.hipfireSens.value

        val helmet = _selectedHelmet.value
        val armor = _selectedArmor.value

        val changeValue = (helmet?.mousePenalty?.roundToInt() ?: 0) + (armor?.mousePenalty?.roundToInt() ?: 0)

        _change.value = changeValue

        val changePercent = changeValue / -100.0

        val newDPI = userDPI + (userDPI * changePercent)
        val newHipfire = userHipfire.toDouble() + (userHipfire.toDouble() * changePercent)

        Timber.d(changeValue.toString())
        Timber.d(newDPI.toString())
        Timber.d(newHipfire.toString())

        _newDPI.value = newDPI.roundToInt()
        _newHipfire.value = newHipfire
    }

    init {

    }
}