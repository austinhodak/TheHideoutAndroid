package com.austinhodak.thehideout.quests.inraid

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.addQuotes
import com.austinhodak.thehideout.databinding.ActivityQuestInRaidBinding
import com.austinhodak.thehideout.getObjectiveIcon
import com.austinhodak.thehideout.quests.inraid.models.Category
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.quests.viewmodels.QuestsViewModel
import com.austinhodak.thehideout.utils.Map
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class QuestInRaidActivity : AppCompatActivity() {

    private lateinit var questList: List<Quest>
    lateinit var map: Map
    private lateinit var binding: ActivityQuestInRaidBinding

    private val questViewModel: QuestsViewModel by viewModels()

    private lateinit var adapter: FastAdapter<*>
    private lateinit var categoryAdapter: ItemAdapter<Category>

    //Hide kappa items. Offer option to switch on later.
    private var includeKappa = false

    private val objectiveTypes = listOf("kill", "collect", "pickup", "key", "place", "mark", "locate", "find", "warning", "survive")

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        map = intent.getSerializableExtra("map") as Map
        binding = ActivityQuestInRaidBinding.inflate(layoutInflater).also {
            it.map = map
            setContentView(it.root)
        }

        categoryAdapter = ItemAdapter()
        adapter = FastAdapter.with(listOf(categoryAdapter))
        binding.inRaidRV.layoutManager = LinearLayoutManager(this)
        binding.inRaidRV.adapter = adapter

        binding.inRaidFAB.setOnClickListener {
            finish()
        }

        setupToolbar()

        questViewModel.completedQuests.observe(this) { list ->
            questList = questViewModel.quests.value ?: emptyList()
            questViewModel.quests.observe(this) { quests ->
                questList = quests

                val completedQuestList = if (list != null) {
                    list.completed?.map { q ->
                        questList.find { "\"${it.id}\"" == q.key }?.title
                    }
                } else {
                    emptyList<String>()
                }

                Timber.d(quests.toString())
                //Filter map
                questList = questList.filter { it.getLocation().contains("Any") || it.getLocation().contains(map.mapName) }

                //Filter out all completed quests.
                questList = questList.filterNot {
                    list?.completed?.containsKey(it.id.addQuotes()) ?: false
                }

                questList = questList.filter {
                    if (!it.require.quest.isNullOrEmpty()) {
                        var bool = false
                        for (q in it.require.quest) {
                            if (completedQuestList?.contains(q) == false) {
                                bool = false
                                break
                            } else {
                                bool = true
                            }
                        }

                        bool
                    } else completedQuestList?.contains(it.title) == false
                }

                for (i in Types.values()) {
                    if (questList.any { it.objectives.filter { it.type.equals(i.name, true) }.any { it.location == "Any" || it.location == map.mapName } }) {
                        categoryAdapter.add(Category (
                            title = i.title.toUpperCase(),
                            icon = i.name.getObjectiveIcon(),
                            objectiveType = i.name,
                            items = questList.filter { it.objectives.map { obj -> obj.type.toUpperCase() }.contains(i.name) },
                            map = map.mapName
                        ))
                    }
                }

            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.questInRaidToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.md_nav_back)
        binding.questInRaidToolbar.setNavigationOnClickListener { super.onBackPressed() }
    }

    enum class Types(var title: String) {
        KILL("YOU NEED TO KILL"),
        COLLECT("YOU NEED TO FIND"),
        PICKUP("YOU NEED TO PICKUP"),
        PLACE("YOU NEED TO PLACE"),
        MARK("YOU NEED TO MARK"),
        LOCATE("YOU NEED TO LOCATE"),
        FIND("YOU NEED TO FIND (IN RAID)"),
        WARNING("YOU NEED TO"),
        SURVIVE("YOU NEED TO SURVIVE")
    }
}