package com.austinhodak.thehideout.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.userRef
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class QuestsViewModel : ViewModel() {
    val user = MutableLiveData<UserFB>()
    val quests = MutableLiveData<UserFB.UserFBQuests>()
    val objectives = MutableLiveData<UserFB.UserFBQuestObjectives>()

    init {
        loadUserQuests()
        Log.d("QUESTS", "INIT VIEW MODEL")
        loadUserQuestsObjectives()
    }

    fun loadUserData() {
        val UID = Firebase.auth.uid
        Firebase.database.getReference("users/$UID/").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u = snapshot.getValue<UserFB>()
                user.value = u
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadUserQuests() {
        val UID = Firebase.auth.uid
        Firebase.database.getReference("users/$UID/quests/").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u = snapshot.getValue<UserFB.UserFBQuests>()
                quests.value = u
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
                objectives.value = u
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun markObjectiveComplete(objectiveID: Int, value: Int) {
        userRef("questObjectives/progress/\"$objectiveID\"").setValue(value)
    }

    fun unMarkObjectiveComplete(objectiveID: Int) {
        userRef("questObjectives/progress/\"$objectiveID\"").removeValue()
    }

    fun markQuestCompleted(quest: Quest) {
        //Mark quest objectives completed
        for (obj in quest.objectives) {
            markObjectiveComplete(obj.id, obj.number)
        }

        userRef("quests/completed/\"${quest.id}\"").setValue(true)
    }

    fun jumpToQuest(quest: Quest) {

    }

    fun undoQuest(quest: Quest) {
        for (obj in quest.objectives) {
            unMarkObjectiveComplete(obj.id)
        }

        userRef("quests/completed/\"${quest.id}\"").removeValue()
    }

}