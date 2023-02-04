package com.austinhodak.thehideout.firebase.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class UserData(
    @DocumentId
    val id: String = "",
    val playerLevel: Int? = 70,
    val taskProgress: HashMap<String, TaskProgress>? = null,
    val objectiveProgress: HashMap<String, ObjectiveProgress>? = null
) {
    val ref: DocumentReference
        get() = Firebase.firestore.collection("users").document(id)

    data class TaskProgress(
        val status: String = "UNAVAILABLE"
    ) {
        val taskStatus: TaskStatus
            get() = TaskStatus.valueOf(status)
    }
    data class ObjectiveProgress(
        val status: String = "UNAVAILABLE",
    ) {
        val taskStatus: TaskStatus
            get() = TaskStatus.valueOf(status)
    }
}
enum class TaskStatus {
    AVAILABLE,
    COMPLETED,
    IN_PROGRESS,
    LOCKED,
    UNAVAILABLE
}