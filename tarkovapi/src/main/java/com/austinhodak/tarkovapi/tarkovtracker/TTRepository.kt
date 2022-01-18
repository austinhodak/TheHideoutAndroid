package com.austinhodak.tarkovapi.tarkovtracker

import com.austinhodak.tarkovapi.tarkovtracker.models.TTUser
import okhttp3.RequestBody
import javax.inject.Inject

class TTRepository @Inject constructor(
    private val ttApiService: TTApiService
) {

    suspend fun getUserProgress(apiKey: String) = ttApiService.getUserProgress(apiKey = apiKey)

    suspend fun setUserLevel(apiKey: String, level: Int) = ttApiService.setUserLevel(apiKey, level)

    suspend fun updateQuest(apiKey: String, id: Int, body: TTUser.TTQuest) = ttApiService.updateQuest(apiKey, id, body)
    suspend fun updateQuestObjective(apiKey: String, id: Int, body: TTUser.TTObjective) = ttApiService.updateQuestObjective(apiKey, id, body)
    suspend fun updateHideout(apiKey: String, id: Int, body: TTUser.TTQuest) = ttApiService.updateHideout(apiKey, id, body)
    suspend fun updateHideoutObjective(apiKey: String, id: Int, body: TTUser.TTObjective) = ttApiService.updateHideoutObjective(apiKey, id, body)

}