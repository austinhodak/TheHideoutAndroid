package com.austinhodak.thehideout.quests.models

data class Quest(
    var id: Int,
    var require: QuestRequire,
    var giver: String,
    var turnin: String,
    var title: String,
    var wiki: String,
    var exp: Int,
    var unlocks: List<String>,
    var reputation: List<QuestReputation>,
    var objectives: List<QuestObjectives>
) {
    data class QuestRequire(
        var level: Int,
        var quest: List<String>
    )

    data class QuestReputation(
        var trader: String,
        var rep: Double
    )

    data class QuestObjectives(
        var type: String,
        var target: String,
        var number: Int,
        var location: String,
        var id: Int
    )
}

enum class Traders (var id: String) {
    PRAPOR      ("Prapor"),
    THERAPIST   ("Therapist"),
    FENCE       ("Fence"),
    SKIER       ("Skier"),
    PEACEKEEPER ("Peacekeeper"),
    MECHANIC    ("Mechanic"),
    RAGMAN      ("Ragman"),
    JAEGER      ("Jaeger")
}