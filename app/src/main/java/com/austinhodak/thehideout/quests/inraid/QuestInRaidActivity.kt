package com.austinhodak.thehideout.quests.inraid

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestInRaidActivity : AppCompatActivity() {
/*
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

                for (i in QuestDetailActivity.Types.values()) {
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
    }*/
}