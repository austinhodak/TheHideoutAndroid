package com.austinhodak.thehideout.quests.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.SearchViewModel
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.mapsList
import com.austinhodak.thehideout.quests.QuestFilter
import com.austinhodak.thehideout.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QuestMainViewModel @Inject constructor(
    private val repository: TarkovRepo
) : SearchViewModel() {

    private val _view = MutableLiveData(QuestFilter.AVAILABLE)
    val view = _view

    fun setView(int: QuestFilter) {
        _view.value = int
    }

    private val _selectedTrader = MutableLiveData(Traders.PRAPOR)
    val selectedTrader = _selectedTrader

    fun selectTrader(trader: Traders) {
        _selectedTrader.value = trader
    }

    val pmcElimsTotal = MutableLiveData(0)
    val scavElimsTotal = MutableLiveData(0)
    val questItemsTotal = MutableLiveData(0)
    val questFIRItemsTotal = MutableLiveData(0)
    val handoverItemsTotal = MutableLiveData(0)
    val placedTotal = MutableLiveData(0)
    val pickupTotal = MutableLiveData(0)

    val questTotalCompletedUser = MutableLiveData(0)
    val pmcElimsTotalUser = MutableLiveData(0)
    val scavElimsTotalUser = MutableLiveData(0)
    val questItemsTotalUser = MutableLiveData(0)
    //val questFIRItemsTotalUser = MutableLiveData(0)
    //val handoverItemsTotalUser = MutableLiveData(0)
    val placedTotalUser = MutableLiveData(0)
    val pickupTotalUser = MutableLiveData(0)

    private suspend fun updateTotals(quests: List<Quest>) {
        Timber.d(quests.size.toString())

        repository.getAllQuests().collect { quests ->
            pmcElimsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "kill" && objective.target?.contains("PMCs") == true) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            scavElimsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "kill" && objective.target?.contains("Scavs") == true) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            questItemsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "find" || objective.type == "collect" && objective.number!! < 500) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            questFIRItemsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "find" && objective.number!! < 500) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            handoverItemsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.number!! >= 500) total += 1
                    if (objective.type == "collect" && objective.number!! < 500) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            placedTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "place" || objective.type == "mark") {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            pickupTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "pickup") {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            updateUserTotals(quests)
        }
    }

    private fun updateUserTotals(quests: List<Quest>) {
        Timber.d(quests.size.toString())
        //if (!this::quests.isInitialized) return
        val userData = _userData.value ?: return
        questTotalCompletedUser.value = userData.quests?.values?.sumOf {
            val total = if (it?.completed == true) 1 else 0
            total
        } ?: 0

        val allObjectives = quests.flatMap { it.objective!! }

        var pmc = 0
        var scav = 0
        var items = 0
        var place = 0
        var pickup = 0

        userData.questObjectives?.values?.forEach { obj ->
            val objective = allObjectives.find { it.id?.toInt() == obj?.id }?: return@forEach

            when {
                objective.type == "kill" && objective.target?.contains("PMCs") == true -> pmc += obj?.progress ?: 0
                objective.type == "kill" && objective.target?.contains("Scavs") == true -> scav += obj?.progress ?: 0
                objective.type == "find" || objective.type == "collect" && objective.number!! < 500 -> items += obj?.progress ?: 0
                objective.type == "place" || objective.type == "mark" -> place += obj?.progress ?: 0
                objective.type == "pickup" -> pickup += obj?.progress ?: 0
            }
        }

        pmcElimsTotalUser.value = pmc
        scavElimsTotalUser.value = scav
        questItemsTotalUser.value = items
        placedTotalUser.value = place
        pickupTotalUser.value = pickup
    }

    private val _userData = MutableLiveData<User?>(null)
    val userData = _userData

    init {

        var quests: List<Quest>? = null

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllQuests().collect {
                viewModelScope.launch {
                    //Timber.d(it.toString())
                    quests = it
                    updateTotals(it)
                }
            }
        }

        if (uid() != null) {
            questsFirebase.child("users/${uid()}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userData.value = snapshot.getValue<User>()

                    viewModelScope.launch {
                        quests?.let { updateUserTotals(it) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
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
            "key" -> "$itemName needed on $location"
            "pickup" -> "Pick-up $itemName on $location"
            "kill" -> "Eliminate ${questObjective.number} $itemName on $location"
            "collect" -> "Hand over ${questObjective.number} $itemName"
            "place" -> "Place $itemName on $location"
            "mark" -> "Place MS2000 marker at $location"
            "locate" -> "Locate $itemName on $location"
            "find" -> "Find in raid ${questObjective.number} $itemName"
            "reputation" -> "Reach loyalty level ${questObjective.number} with ${Traders.values()[itemName?.toInt()!!].id}"
            "warning" -> "$itemName"
            "skill" -> "Reach skill level ${questObjective.number} with $itemName"
            "survive" -> "Survive in the raid at $location ${questObjective.number} times."
            "build" -> "Build $itemName"
            else -> ""
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
}