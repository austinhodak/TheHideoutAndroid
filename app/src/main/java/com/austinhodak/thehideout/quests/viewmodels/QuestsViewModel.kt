package com.austinhodak.thehideout.quests.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.log
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.userRef
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

class QuestsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    val completedQuests = MutableLiveData<UserFB.UserFBQuests>()
    val completedObjectives = MutableLiveData<UserFB.UserFBQuestObjectives>()

    val quests: LiveData<List<Quest>> = liveData {
        emit(loadInitialQuests())
    }

    init {
        loadUserQuests()
        loadUserQuestsObjectives()
    }

    private fun loadUserQuests() {
        val UID = Firebase.auth.uid
        Firebase.database.getReference("users/$UID/quests/").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u = snapshot.getValue<UserFB.UserFBQuests>()
                completedQuests.value = u ?: UserFB.UserFBQuests()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadUserQuestsObjectives() {
        val UID = Firebase.auth.uid
        Firebase.database.getReference("users/$UID/questObjectives/").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u = snapshot.getValue<UserFB.UserFBQuestObjectives>()
                completedObjectives.value = u ?: UserFB.UserFBQuestObjectives()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun markObjectiveComplete(objectiveID: Int, value: Int) {
        log("objective_complete", objectiveID.toString(), objectiveID.toString(), "quest_objective")
        userRef("questObjectives/progress/\"$objectiveID\"").setValue(value)
    }

    fun unMarkObjectiveComplete(objectiveID: Int) {
        log("objective_un_complete", objectiveID.toString(), objectiveID.toString(), "quest_objective")
        userRef("questObjectives/progress/\"$objectiveID\"").removeValue()
    }

    fun markQuestCompleted(quest: Quest) {
        log("quest_completed", quest.id.toString(), quest.title, "quest")
        //Mark quest objectives completed
        for (obj in quest.objectives) {
            markObjectiveComplete(obj.id, obj.number)
        }

        userRef("quests/completed/\"${quest.id}\"").setValue(true)
    }

    fun resetQuestProgress() {
        userRef("quests").removeValue()
        userRef("questObjectives").removeValue()
    }

    fun jumpToQuest(quest: Quest) {

    }

    fun undoQuest(quest: Quest) {
        log("quest_undo", quest.id.toString(), quest.title, "quest")

        for (obj in quest.objectives) {
            unMarkObjectiveComplete(obj.id)
        }

        userRef("quests/completed/\"${quest.id}\"").removeValue()
    }

    private suspend fun loadInitialQuests(): List<Quest> = withContext(Dispatchers.IO) {
        val groupListType: Type = object : TypeToken<ArrayList<Quest?>?>() {}.type
        Gson().fromJson(context.resources.openRawResource(R.raw.quests).bufferedReader().use { it.readText() }, groupListType)
    }

}