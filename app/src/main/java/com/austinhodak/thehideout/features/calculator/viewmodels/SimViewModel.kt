package com.austinhodak.thehideout.features.calculator.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.features.calculator.CalculatorHelper
import com.austinhodak.thehideout.features.calculator.models.Body
import com.austinhodak.thehideout.features.calculator.models.CArmor
import com.austinhodak.thehideout.features.calculator.models.Character
import com.austinhodak.thehideout.utils.toSimArmor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SimViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    var characterList = MutableLiveData<List<Character>>(null)

    private val _selectedCharacter = MutableLiveData<Character>(null)
    val selectedCharacter = _selectedCharacter

    fun selectCharacter(character: Character) {
        _selectedCharacter.value = character
        resetHealth()
    }

    private val _body = MutableLiveData(Body())
    val body = _body

    fun resetHealth() {
        _body.value?.let {
            _selectedCharacter.value?.let { it1 ->
                it.reset(it1)
                updateHealth()
                resetArmor()
            }
        }
    }

    private val _selectedAmmo = MutableLiveData<Ammo>(null)
    val selectedAmmo = _selectedAmmo

    fun selectAmmo(ammo: Ammo) {
        _selectedAmmo.value = ammo
    }

    private val _selectedArmor = MutableLiveData<Item>(null)
    val selectedArmor = _selectedArmor

    fun selectArmor(armor: Item) {
        _selectedArmor.value = armor
        selectedArmorC.value = armor.toSimArmor()
    }

    private val _selectedHelmet = MutableLiveData<Item>(null)
    val selectedHelmet = _selectedHelmet

    fun selectHelmet(armor: Item) {
        _selectedHelmet.value = armor
        selectedHelmetC.value = armor.toSimArmor()
    }

    val selectedArmorC = MutableLiveData<CArmor>(null)
    val selectedHelmetC = MutableLiveData<CArmor>(null)

    var totalHealthTV = MutableLiveData(440)
    var currentHealthTV = MutableLiveData(440)

    fun updateHealth() {
        totalHealthTV.value = _body.value?.getTotalInitialHealth()
        currentHealthTV.value = _body.value?.getTotalHealth()

        //Timber.d(selectedArmorC.value.toString())
        //Timber.d(selectedHelmetC.value.toString())
    }

    private fun resetArmor() {
        selectedArmorC.value = selectedArmor.value?.toSimArmor()
        selectedHelmetC.value = selectedHelmet.value?.toSimArmor()
    }

    init {
        characterList.value = CalculatorHelper.getCharacters(context)
        selectedCharacter.value = characterList.value?.find { it.c_type == "player" }
        Timber.d(selectedCharacter.value.toString())
    }

}