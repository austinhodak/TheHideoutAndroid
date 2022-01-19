package com.austinhodak.tarkovapi.tarkovtracker

import com.austinhodak.tarkovapi.tarkovtracker.models.TTUser
import okhttp3.RequestBody
import javax.inject.Inject

class TTRepository @Inject constructor(
    private val ttApiService: TTApiService
) {

    suspend fun getUserProgress() = ttApiService.getUserProgress()

    suspend fun setUserLevel(level: Int) = ttApiService.setUserLevel(level)

    suspend fun updateQuest(id: Int, body: TTUser.TTQuest) = ttApiService.updateQuest(id, body)
    suspend fun updateQuestObjective(id: Int, body: TTUser.TTObjective) = ttApiService.updateQuestObjective(id, body)
    suspend fun updateHideout(id: Int, body: TTUser.TTQuest) = ttApiService.updateHideout(id, body)
    suspend fun updateHideoutObjective(id: Int, body: TTUser.TTObjective) = ttApiService.updateHideoutObjective(id, body)

}