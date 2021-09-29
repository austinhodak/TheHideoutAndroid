package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.QuestExtra
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object QuestExtraHelper {

    var quests: MutableList<QuestExtra.QuestExtraItem> = mutableListOf()

    fun getQuests(context: Context): List<QuestExtra.QuestExtraItem> {
        return if (quests.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<QuestExtra.QuestExtraItem?>?>() {}.type
            Gson().fromJson(context.resources.openRawResource(R.raw.quests).bufferedReader().use { it.readText() }, groupListType)
        } else quests
    }
}