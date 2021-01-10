package com.austinhodak.thehideout.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserFB (
    var quests: UserFBQuests? = UserFBQuests(),
    //var questObjectives: UserFBQuestObjectives? = UserFBQuestObjectives(),
    var keys: UserKeys? = UserKeys()
) {
    data class UserKeys (
        var have: Map<String, Boolean>? = HashMap(),
        var need: Map<String, Boolean>? = HashMap()
    )

    data class UserFBQuests (
        var completed: Map<String, Boolean>? = HashMap(),
        var inProgress: Map<String, Boolean>? = HashMap()
    )

    data class UserFBQuestObjectives (
        var progress: Map<String, Int>? = HashMap()
    )
}