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
import com.austinhodak.thehideout.firebase.Team
import com.austinhodak.thehideout.fsUser
import com.austinhodak.thehideout.mapsList
import com.austinhodak.thehideout.utils.questsFirebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestDetailViewModel @Inject constructor(
    private val repository: TarkovRepo,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _questsExtras = MutableLiveData<List<QuestExtra.QuestExtraItem>>()
    val questsExtra = _questsExtras

    private val _teamsData = MutableLiveData<List<Team>>(null)
    val teamsData = _teamsData

    init {
        fsUser.value?.let { user ->
            user.teams?.forEach {
                val teamID = it.key
                val teams: MutableList<Team> = mutableListOf()
                questsFirebase.child("teams/$teamID")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                _teamsData.value = emptyList()
                                return
                            }
                            val team = snapshot.getValue(Team::class.java)
                            team?.let { team ->
                                teams.add(team)
                                _teamsData.value = teams.toList()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }

            if (user.teams == null) {
                _teamsData.value = emptyList()
            }
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

    suspend fun getObjectiveText(questObjective: Quest.QuestObjective): String {
        val location = mapsList.getMap(questObjective.location?.toInt()) ?: "Any Map"
        val item = if (questObjective.type == "key" || questObjective.targetItem == null) {
            repository.getItemByID(questObjective.target?.get(0) ?: "").firstOrNull()?.pricing
                ?: questObjective.target?.first()
        } else {
            questObjective.targetItem
        }

        val itemName = if (item is Pricing) {
            item.name
        } else {
            item as String
        }

        return when (questObjective.type) {
            "key" -> "$itemName needed on $location"
            "pickup" -> "Pick-up $itemName on $location"
            "kill" -> "Eliminate ${questObjective.number} $itemName on $location"
            "collect" -> "Hand over ${questObjective.number} $itemName"
            "place" -> "Place $itemName on $location"
            "mark" -> "Place MS2000 marker at $location"
            "locate" -> "Locate $itemName on $location"
            "find" -> "Find in raid ${questObjective.number} $itemName"
            "reputation" -> "Reach loyalty level ${questObjective.number} with ${
                Traders.values().find { it.int == questObjective.target?.first()?.toInt() ?: 0 }?.id
            }"
            "warning" -> "$itemName"
            "skill" -> "Reach skill level ${questObjective.number} with $itemName"
            "survive" -> "Survive in the raid at $location ${questObjective.number} times."
            "build" -> "Build $itemName"
            else -> ""
        }
    }
}