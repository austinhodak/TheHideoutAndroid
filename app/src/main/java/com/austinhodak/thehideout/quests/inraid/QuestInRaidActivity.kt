package com.austinhodak.thehideout.quests.inraid

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ActivityQuestInRaidBinding
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.QuestsHelper
import com.austinhodak.thehideout.quests.models.Maps
import com.austinhodak.thehideout.utils.Map
import com.austinhodak.thehideout.viewmodels.QuestsViewModel

class QuestInRaidActivity : AppCompatActivity() {

    lateinit var map: Map
    private lateinit var binding: ActivityQuestInRaidBinding
    private lateinit var questViewModel: QuestsViewModel
    private lateinit var questList: UserFB.UserFBQuests

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questViewModel = ViewModelProvider(this).get(QuestsViewModel::class.java)
        map = intent.getSerializableExtra("map") as Map
        binding = ActivityQuestInRaidBinding.inflate(layoutInflater).also {
            it.map = map
            setContentView(it.root)
        }

        setupToolbar()

        questViewModel.completedQuests.observe(this) { list ->
            questList = list

            Log.d("QUESTS", list.completed?.getOrDefault("\"1\"", false).toString())

            //var quests = QuestsHelper.getQuests(this).filter { it.getLocation().contains(map.mapName) || it.getLocation().contains("Any") }
            val quests = QuestsHelper.getActiveQuests(quests = list, any = true, map = Maps.valueOf(map.toString()))
            val string = quests.joinToString(separator = "\n\n") { it.title }
            binding.textView16.text = string
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.questInRaidToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.questInRaidToolbar.setNavigationOnClickListener { super.onBackPressed() }
    }
}