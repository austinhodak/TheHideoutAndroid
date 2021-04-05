package com.austinhodak.thehideout.quests

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ActivityQuestDetailBinding
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.quests.viewmodels.QuestsViewModel
import com.austinhodak.thehideout.quests.views.QuestObjective
import net.idik.lib.slimadapter.SlimAdapter

class QuestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestDetailBinding
    private lateinit var questsViewModel: QuestsViewModel
    private var quest: Quest? = null
    private var objectivesList: UserFB.UserFBQuestObjectives = UserFB.UserFBQuestObjectives()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestDetailBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        setupToolbar()

        questsViewModel = ViewModelProvider(this).get(QuestsViewModel::class.java)
        questsViewModel.quests.observe(this) { quests ->
            if (intent.hasExtra("questID")) {
                quest = quests.find { it.id == intent.getIntExtra("questID", 0) }
                questLoaded()
            }
        }
    }

    private fun questLoaded() {
        binding.quest = quest
        setupObjectives()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun setupObjectives() {
        SlimAdapter.create().register<Quest.QuestObjectives>(R.layout.item_quest_task) { objective, i ->

            val objectiveView = i.findViewById<QuestObjective>(R.id.questOb)
            objectiveView.setObjective(objective, objectivesList)

            i.clicked(R.id.questOb) {
                if (objective.isCompleted(objectivesList)) {
                    questsViewModel.unMarkObjectiveComplete(objective.id)
                } else {
                    questsViewModel.markObjectiveComplete(objective.id, objective.number)
                }
            }

        }.attachTo(binding.questDetailObjectivesRV).updateData(quest?.objectives)
        binding.questDetailObjectivesRV.layoutManager = LinearLayoutManager(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.questDetailToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.questDetailToolbar.setNavigationOnClickListener { super.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_quest_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }
}