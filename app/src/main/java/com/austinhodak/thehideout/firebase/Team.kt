package com.austinhodak.thehideout.firebase

import androidx.compose.ui.graphics.Color
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.utils.userFirestore
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue

data class Team (
    var name: String? = null,
    var members: Map<String, MemberSettings>? = null,
    var id: String? = null
) {
    data class MemberSettings (
        var color: String? = null,
        var owner: Boolean? = null
    ) {
        @Exclude
        fun getColorM(): Color {
            color?.let {
                return Color(android.graphics.Color.parseColor(it))
            }
            return Color.White
        }

        fun updateColor(color: String, teamID: String, memberID: String) {
            questsFirebase.child("teams/$teamID/members/$memberID/color").setValue(color)
        }
    }

    fun leave() {
        id?.let {
            if (members?.size == 1) {
                questsFirebase.child("teams/$it").removeValue()
            }
            userFirestore()?.update("teams.$it", FieldValue.delete())
            userRefTracker("teams/$it").removeValue()
            questsFirebase.child("teams/$it/members/${uid()}").removeValue()
        }
    }

    fun updateName(newName: String) {
        id?.let {
            questsFirebase.child("teams/$it/name").setValue(newName)
        }
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "members" to members
        )
    }
}
