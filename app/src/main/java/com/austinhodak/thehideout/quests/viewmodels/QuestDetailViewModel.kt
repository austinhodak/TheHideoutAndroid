package com.austinhodak.thehideout.quests.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
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

    private val _userData = MutableLiveData<User?>(null)
    val userData = _userData

    init {
        if (uid() != null) {
            questsFirebase.child("users/${uid()}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userData.value = snapshot.getValue<User>()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private val _questDetails = MutableLiveData<Quest?>(null)
    val questDetails = _questDetails

    fun getQuest(id: String) {
        viewModelScope.launch {
            repository.getQuestByID(id).collect {
                _questDetails.value = it
            }
        }
    }

    fun getItem(id: String) = flow {
        viewModelScope.launch(Dispatchers.IO) {
            emit(repository.getItemByID(id))
        }
    }

    fun skipToQuest(quest: Quest) {
        viewModelScope.launch {
            quest.requiredQuestsList()?.forEach { _ ->
                /* val q = quests?.find { it.id.toInt() == id }
                 if (q != null) {
                     markQuestCompleted(q)
                     skipToQuest(q)
                 }*/
            }
        }
    }

    fun toggleObjective(quest: Quest, objective: Quest.QuestObjective) {
        if (userData.value?.isObjectiveCompleted(objective) == true) {
            unMarkObjectiveComplete(objective)
            undoQuest(quest)
        } else {
            markObjectiveComplete(objective)
        }
    }

    private fun markObjectiveComplete(objective: Quest.QuestObjective) {
        log("objective_complete", objective.toString(), objective.toString(), "quest_objective")
        userRefTracker("questObjectives/${objective.id?.toInt()?.addQuotes()}").setValue(
            mapOf(
                "id" to objective.id?.toInt(),
                "progress" to objective.number
            )
        )
    }

    private fun unMarkObjectiveComplete(objective: Quest.QuestObjective) {
        log("objective_un_complete", objective.toString(), objective.toString(), "quest_objective")
        userRefTracker("questObjectives/${objective.id?.toInt()?.addQuotes()}").removeValue()
    }

    fun markQuestCompleted(quest: Quest) {
        log("quest_completed", quest.id, quest.title.toString(), "quest")
        userRefTracker("quests/${quest.id.addQuotes()}").setValue(
            mapOf(
                "id" to quest.id.toInt(),
                "completed" to true
            )
        )

        //Mark quest objectives completed
        for (obj in quest.objective!!) {
            markObjectiveComplete(obj)
        }
    }

    fun undoQuest(quest: Quest, unmarkObjectives: Boolean = false) {
        log("quest_undo", quest.id, quest.title.toString(), "quest")

        if (unmarkObjectives)
            for (obj in quest.objective!!) {
                unMarkObjectiveComplete(obj)
            }

        userRefTracker("quests/${quest.id.addQuotes()}").setValue(
            mapOf(
                "id" to quest.id.toInt(),
                "completed" to false
            )
        )
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