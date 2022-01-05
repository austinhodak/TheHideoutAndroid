package com.austinhodak.thehideout.quests.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.models.QuestExtra
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.utils.QuestExtraHelper
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.mapsList
import com.austinhodak.thehideout.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestInRaidViewModel @Inject constructor(
    private val repository: TarkovRepo,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _questsExtras = MutableLiveData<List<QuestExtra.QuestExtraItem>>()
    val questsExtra = _questsExtras

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

        _questsExtras.value = QuestExtraHelper.getQuests(context = context)
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
        /*viewModelScope.launch {
            quest.requiredQuestsList()?.forEach { id ->
                val q = questsList.value?.find { it.id.toInt() == id }
                if (q != null) {
                    q.completed()
                    skipToQuest(q)
                }
            }
        }*/
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
        objective.target?.first()?.let {
            when (objective.type) {
                "collect", "find", "key", "build" -> {
                    userRefTracker("items/${it}/questObjective/${objective.id?.addQuotes()}").removeValue()
                }
                else -> {}
            }
        }
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

    suspend fun getObjectiveText(questObjective: Quest.QuestObjective): String {
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

        return when (questObjective.type) {
            "key" -> "$itemName needed"
            "pickup" -> "$itemName"
            "kill" -> "${questObjective.number} $itemName"
            "collect" -> "${questObjective.number} $itemName"
            "place" -> "$itemName"
            "mark" -> "Place MS2000 marker"
            "locate" -> "$itemName"
            "find" -> "${questObjective.number} $itemName"
            "reputation" -> "Loyalty level ${questObjective.number} with ${Traders.values().find { it.int == questObjective.target?.first()?.toInt() ?: 0}?.id}"
            "warning" -> "$itemName"
            "skill" -> "Skill level ${questObjective.number} with $itemName"
            "survive" -> "$location ${questObjective.number} times."
            "build" -> "$itemName"
            else -> ""
        }
    }
}