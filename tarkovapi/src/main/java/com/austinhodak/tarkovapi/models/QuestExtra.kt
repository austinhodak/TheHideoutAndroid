package com.austinhodak.tarkovapi.models

class QuestExtra : ArrayList<QuestExtra.QuestExtraItem>(){
    data class QuestExtraItem(
        val alternatives: List<Int?>? = null,
        val exp: Int? = null,
        val gameId: String? = null,
        val giver: Int? = null,
        val id: Int? = null,
        val locales: Locales? = null,
        val nokappa: Boolean? = null,
        val objectives: List<Objective?>? = null,
        val reputation: List<Any?>? = null,
        val reputationFailure: List<ReputationFailure?>? = null,
        val require: Require? = null,
        val title: String? = null,
        val turnin: Int? = null,
        val unlocks: List<Any?>? = null,
        val wiki: String? = null
    ) {
        data class Locales(
            val en: String? = null
        )
    
        data class Objective(
            val gps: Gps? = null,
            val hint: String? = null,
            val id: Int? = null,
            val location: Int? = null,
            val number: Int? = null,
            //val target: String? = null,
            val type: String? = null,
            val with: List<Any>? = null
        ) {
            data class Gps(
                val floor: String? = null,
                val leftPercent: Double? = null,
                val topPercent: Double? = null
            )
        }
    
        data class ReputationFailure(
            val rep: Double? = null,
            val trader: Int? = null
        )
    
        data class Require(
            val level: Int? = null,
            val quests: List<Any?>? = null
        )
    }
}