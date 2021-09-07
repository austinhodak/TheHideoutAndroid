package com.austinhodak.thehideout.quests.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Quest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestDetailViewModel @Inject constructor(
    private val repository: TarkovRepo
) : ViewModel() {

    private val _questDetails = MutableLiveData<Quest?>(null)
    val questDetails = _questDetails

    fun getQuest(id: String) {
        viewModelScope.launch {
            repository.getQuestByID(id).catch { e ->

            }.collect {
                _questDetails.value = it
            }
        }
    }

    fun getItem(id: String) = flow {
        viewModelScope.launch(Dispatchers.IO) {
            emit(repository.getItemByID(id))
        }
    }

   /* fun getObjectiveText(questObjective: Quest.QuestObjective) = liveData {
        val location = mapsList.getMap(questObjective.location?.toInt()) ?: "Any Map"

        val item = if (questObjective.type == "key" || questObjective.targetItem == null) {
            repository.getItemByID(questObjective.target?.get(0) ?: "").firstOrNull()?.pricing ?: questObjective.target?.first()
        } else {
            questObjective.targetItem
        }

        val itemName = if (item is Pricing) {
            item.name
        } else {
            item as String
        }

        val text = when {
            questObjective.type == "key" -> "$itemName needed on $location"
            questObjective.type == "pickup" -> "Pick-up $itemName on $location"
            questObjective.type == "kill" -> "Eliminate ${questObjective.number} $itemName on $location"
            questObjective.type == "collect" -> "Hand over ${questObjective.number} $itemName"
            questObjective.type == "place" -> "Place $itemName on $location"
            questObjective.type == "mark" -> "Place MS2000 marker at $location"
            questObjective.type == "locate" -> "Locate $itemName on $location"
            questObjective.type == "find" -> "Find in raid ${questObjective.number} $itemName"
            questObjective.type == "reputation" -> "Reach loyalty level ${questObjective.number} with ${Traders.values()[itemName?.toInt()!!].id}"
            questObjective.type == "warning" -> "$itemName"
            questObjective.type == "skill" -> "Reach skill level ${questObjective.number} with $itemName"
            questObjective.type == "survive" -> "Survive in the raid at $location ${questObjective.number} times."
            questObjective.type == "build" -> "Build $itemName"
            else -> ""
        }

        emit(text)
    }*/
}