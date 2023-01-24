package com.austinhodak.thehideout.realm.models

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.austinhodak.thehideout.TasksQuery
import com.austinhodak.thehideout.realm.converters.findObjectById
import com.austinhodak.thehideout.firebase.models.TaskStatus
import com.austinhodak.thehideout.firebase.models.UserData
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveBuildItem
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveExperience
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveExtract
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveItem
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveMark
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectivePlayerLevel
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveQuestItem
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveShoot
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveSkill
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveTaskStatus
import com.austinhodak.thehideout.fragment.TaskFragment.Objective.Companion.asTaskObjectiveTraderLevel
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.callbackFlow
import com.austinhodak.thehideout.base.Result
import com.google.android.gms.tasks.Task as FSTask

class Task : RealmObject {
    @PrimaryKey
    var id: String? = null
    var tarkovDataId: Int? = null
    var name: String = ""
    var normalizedName: String = ""
    var trader: Trader? = null
    var map: Map? = null
    var experience: Int = 0
    var wikiLink: String? = null
    var minPlayerLevel: Int = 0
    var taskRequirements: RealmList<TaskStatusRequirement> = realmListOf()
    var traderLevelRequirements: RealmList<RequirementTrader> = realmListOf()
    var objectives: RealmList<TaskObjective> = realmListOf()
    var startReward: TaskRewards? = null
    var finishReward: TaskRewards? = null
    var factionName: String? = null
    var neededKeys: RealmList<TaskKey> = realmListOf()
    var descriptionMessageId: String? = null
    var successMessageId: String? = null
    var failMessageId: String? = null

    @Composable
    fun getAvailableToFaction(
        style: TextStyle = MaterialTheme.typography.bodyMedium,
    ) {
        Text(
            buildAnnotatedString {
                append("Available to ")
                when (factionName) {
                    "USEC" -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("USEC")
                        }
                        append(" only.")
                    }
                    "BEAR" -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("BEAR")
                        }
                        append(" only.")
                    }
                    else -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("USEC")
                        }
                        append(" and ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("BEAR")
                        }
                        append(".")
                    }
                }
            },
            style = style
        )
    }

    fun mark(userData: UserData, status: TaskStatus) = callbackFlow {
        try {
            if (status == TaskStatus.COMPLETED) {
                markAllObjectives(userData, status)
            }

            userData.ref.update("taskProgress.${this@Task.id}.status", status.name).addOnCompleteListener {
                if (it.isSuccessful) {
                    trySend(Result.Success(Unit))
                } else {
                    trySend(Result.Error(it.exception!!))
                }
            }
        } catch (e: Exception) {
            close(e)
        }
    }

    fun markAllObjectives(userData: UserData, status: TaskStatus): FSTask<Void> {
        return userData.ref.update(
            this.objectives.associate { objective ->
                "objectiveProgress.${objective.id}.status" to status.name
            }
        )
    }
}

class TaskStatusRequirement : EmbeddedRealmObject {
    var task: Task? = null
    var status: RealmList<String> = realmListOf()
}

class TaskObjective : EmbeddedRealmObject {
    var id: String? = null
    var type: String = ""
    var description: String = ""
    var maps: RealmList<Map> = realmListOf()
    var optional: Boolean = false

    var taskObjectiveBuildItem: TaskObjectiveBuildItem? = null
    var taskObjectiveExperience: TaskObjectiveExperience? = null
    var taskObjectiveExtract: TaskObjectiveExtract? = null
    var taskObjectiveItem: TaskObjectiveItem? = null
    var taskObjectiveMark: TaskObjectiveMark? = null
    var taskObjectivePlayerLevel: TaskObjectivePlayerLevel? = null
    var taskObjectiveQuestItem: TaskObjectiveQuestItem? = null
    var taskObjectiveShoot: TaskObjectiveShoot? = null
    var taskObjectiveSkill: TaskObjectiveSkill? = null
    var taskObjectiveTaskStatus: TaskObjectiveTaskStatus? = null
    var taskObjectiveTraderLevel: TaskObjectiveTraderLevel? = null

    fun mark(userData: UserData, status: TaskStatus): FSTask<Void> {
        return userData.ref.update("objectiveProgress.${this.id}.status", status.name)
    }

    class TaskObjectiveBuildItem : EmbeddedRealmObject {
        var item: Item? = null
        var containsAll: RealmList<Item> = realmListOf()
        var containsCategory: RealmList<ItemCategory> = realmListOf()
        var attributes: RealmList<AttributeThreshold> = realmListOf()
    }

    class TaskObjectiveExperience : EmbeddedRealmObject {
        var healthEffect: HealthEffect? = null
    }

    class TaskObjectiveExtract : EmbeddedRealmObject {
        var exitStatus: RealmList<String> = realmListOf()
        var exitName: String? = null
        var zoneNames: RealmList<String> = realmListOf()
    }

    class TaskObjectiveItem : EmbeddedRealmObject {
        var item: Item? = null
        var count: Int = 0
        var foundInRaid: Boolean = false
        var dogTagLevel: Int? = null
        var maxDurability: Int? = null
        var minDurability: Int? = null
    }

    class TaskObjectiveMark : EmbeddedRealmObject {
        var markerItem: Item? = null
    }

    class TaskObjectivePlayerLevel : EmbeddedRealmObject {
        var playerLevel: Int = 0
    }

    class TaskObjectiveQuestItem : EmbeddedRealmObject {
        var questItem: QuestItem? = null
        var count: Int = 0
    }

    class TaskObjectiveShoot : EmbeddedRealmObject {
        var target: String = ""
        var count: Int = 0
        var zoneNames: RealmList<String> = realmListOf()
        var bodyParts: RealmList<String> = realmListOf()
        var usingWeapon: RealmList<Item> = realmListOf()

        var usingWeaponMods: RealmList<TaskObjectiveShootList> = realmListOf()
        var wearing: RealmList<TaskObjectiveShootList> = realmListOf()

        var notWearing: RealmList<Item> = realmListOf()
        var distance: NumberCompare? = null
        var playerHealthEffect: HealthEffect? = null
        var enemyHealthEffect: HealthEffect? = null

        class TaskObjectiveShootList : EmbeddedRealmObject {
            var list: RealmList<Item> = realmListOf()
        }
    }

    class TaskObjectiveSkill : EmbeddedRealmObject {
        var skillLevel: SkillLevel? = null
    }

    class TaskObjectiveTaskStatus : EmbeddedRealmObject {
        var task: Task? = null
        var status: RealmList<String> = realmListOf()
    }

    class TaskObjectiveTraderLevel : EmbeddedRealmObject {
        var trader: Trader? = null
        var level: Int = 0
    }
}

class TaskRewards : EmbeddedRealmObject {
    var traderStanding: RealmList<TraderStanding> = realmListOf()
    var items: RealmList<Item.ContainedItem> = realmListOf()
    var offerUnlock: RealmList<OfferUnlock> = realmListOf()
    var skillLevelReward: RealmList<SkillLevel> = realmListOf()
    var traderUnlock: RealmList<Trader> = realmListOf()
    var craftUnlock: RealmList<Craft> = realmListOf()
}

class TraderStanding : EmbeddedRealmObject {
    var trader: Trader? = null
    var standing: Double = 0.0
}

class OfferUnlock : EmbeddedRealmObject {
    var id: String = ""
    var trader: Trader? = null
    var level: Int = 0
    var item: Item? = null
}

class TaskKey : EmbeddedRealmObject {
    var keys: RealmList<Item> = realmListOf()
    var map: Map? = null
}

class AttributeThreshold : EmbeddedRealmObject {
    var name: String = ""
    var requirement: NumberCompare? = null
}

class NumberCompare : EmbeddedRealmObject {
    var compareMethod: String = ""
    var value: Double = 0.0
}

class HealthEffect : EmbeddedRealmObject {
    var bodyParts: RealmList<String> = realmListOf()
    var effects: RealmList<String> = realmListOf()
    var time: NumberCompare? = null
}

class SkillLevel : EmbeddedRealmObject {
    var name: String = ""
    var level: Double = 0.0
}

fun TasksQuery.Data.Task.toRealm(realm: MutableRealm): Task {
    val t = this
    return Task().apply {
        id = t.id
        name = t.name
        normalizedName = t.normalizedName
        trader = t.trader.id.let { findObjectById(realm, it) }
        map = t.map?.id?.let { findObjectById(realm, it) }
        experience = t.experience
        wikiLink = t.wikiLink
        minPlayerLevel = t.minPlayerLevel ?: 1
        //taskRequirements.addAll(t.taskRequirements.map { it.toRealm(realmObject) })
        taskRequirements = t.taskRequirements.filterNotNull().map {
            TaskStatusRequirement().apply {
                task = it.task.id?.let { findObjectById(realm, it) }
                status = it.status.filterNotNull().toRealmList()
            }
        }.toRealmList() ?: realmListOf()
        //traderLevelRequirements.addAll(t.traderLevelRequirements.map { it.toRealm(realmObject) })
        traderLevelRequirements = t.traderLevelRequirements.filterNotNull().map {
            RequirementTrader().apply {
                trader = it.trader.id.let { findObjectById(realm, it) }
                level = it.level
                id = it.id
            }
        }.toRealmList()
        //objectives.addAll(t.objectives.map { it.toRealm(realmObject) })
        objectives = t.objectives.map { task ->
            TaskObjective().apply {
                id = task?.id
                type = task?.type ?: ""
                description = task?.description ?: ""
                optional = task?.optional ?: false
                maps = task?.maps?.mapNotNull {
                    it?.id?.let { it1 ->
                        findObjectById<Map>(
                            realm,
                            it1
                        )
                    }
                }?.toRealmList() ?: realmListOf()

                taskObjectiveBuildItem = task?.asTaskObjectiveBuildItem()?.let {
                    TaskObjective.TaskObjectiveBuildItem().apply {
                        item = it.item.id.let { it1 -> findObjectById(realm, it1) }
                        containsAll = it.containsAll.filterNotNull().mapNotNull { it1 ->
                            findObjectById<Item>(realm, it1.id)
                        }.toRealmList()
                        attributes = it.attributes.filterNotNull().map { it1 ->
                            AttributeThreshold().apply {
                                name = it1.name
                                requirement = it1.requirement.let { it2 ->
                                    NumberCompare().apply {
                                        compareMethod = it2.compareMethod
                                        value = it2.value
                                    }
                                }
                            }
                        }.toRealmList()
                    }
                }

                taskObjectiveExperience = task?.asTaskObjectiveExperience()?.let {
                    TaskObjective.TaskObjectiveExperience().apply {
                        healthEffect = it.healthEffect.let { it1 ->
                            HealthEffect().apply {
                                bodyParts = it1.bodyParts.filterNotNull().toRealmList()
                                effects = it1.effects.filterNotNull().toRealmList()
                                time = it1.time.let { it2 ->
                                    NumberCompare().apply {
                                        compareMethod = it2?.compareMethod ?: ""
                                        value = it2?.value ?: 0.0
                                    }
                                }
                            }
                        }
                    }
                }

                taskObjectiveExtract = task?.asTaskObjectiveExtract()?.let {
                    TaskObjective.TaskObjectiveExtract().apply {
                        exitStatus = it.exitStatus.filterNotNull().toRealmList()
                        exitName = it.exitName
                        zoneNames = it.zoneNames.filterNotNull().toRealmList()
                    }
                }

                taskObjectiveItem = task?.asTaskObjectiveItem()?.let {
                    TaskObjective.TaskObjectiveItem().apply {
                        item = findObjectById(realm, it.item.id)
                        count = it.count
                        foundInRaid = it.foundInRaid
                        dogTagLevel = it.dogTagLevel
                        maxDurability = it.maxDurability
                        minDurability = it.minDurability
                    }
                }

                taskObjectiveMark = task?.asTaskObjectiveMark()?.let {
                    TaskObjective.TaskObjectiveMark().apply {
                        markerItem = findObjectById(realm, it.markerItem.id)
                    }
                }

                taskObjectivePlayerLevel = task?.asTaskObjectivePlayerLevel()?.let {
                    TaskObjective.TaskObjectivePlayerLevel().apply {
                        playerLevel = it.playerLevel
                    }
                }

                taskObjectiveQuestItem = task?.asTaskObjectiveQuestItem()?.let {
                    TaskObjective.TaskObjectiveQuestItem().apply {
                        questItem = it.id?.let { it1 -> findObjectById(realm, it1) }
                        count = it.count
                    }
                }

                taskObjectiveShoot = task?.asTaskObjectiveShoot()?.let { s ->
                    TaskObjective.TaskObjectiveShoot().apply {
                        target = s.target
                        count = s.count
                        zoneNames = s.zoneNames.filterNotNull().toRealmList()
                        bodyParts = s.bodyParts.filterNotNull().toRealmList()
                        usingWeapon = s.usingWeapon?.filterNotNull()?.mapNotNull { it1 ->
                            findObjectById<Item>(realm, it1.id)
                        }?.toRealmList() ?: realmListOf()

                        usingWeaponMods = s.usingWeaponMods?.filterNotNull()?.map { mods ->
                            TaskObjective.TaskObjectiveShoot.TaskObjectiveShootList().apply {
                                list = mods.filterNotNull().mapNotNull { it1 ->
                                    findObjectById<Item>(realm, it1.id)
                                }.toRealmList()
                            }
                        }?.toRealmList() ?: realmListOf()

                        wearing = s.wearing?.filterNotNull()?.map { mods ->
                            TaskObjective.TaskObjectiveShoot.TaskObjectiveShootList().apply {
                                list = mods.filterNotNull().mapNotNull { it1 ->
                                    findObjectById<Item>(realm, it1.id)
                                }.toRealmList()
                            }
                        }?.toRealmList() ?: realmListOf()

                        notWearing = s.notWearing?.filterNotNull()?.mapNotNull {
                            findObjectById<Item>(realm, it.id)
                        }?.toRealmList() ?: realmListOf()

                        distance = s.distance?.let { it1 ->
                            NumberCompare().apply {
                                compareMethod = it1.compareMethod
                                value = it1.value
                            }
                        }

                        playerHealthEffect = s.playerHealthEffect?.let { it1 ->
                            HealthEffect().apply {
                                bodyParts = it1.bodyParts.filterNotNull().toRealmList()
                                effects = it1.effects.filterNotNull().toRealmList()
                                time = it1.time.let { it2 ->
                                    NumberCompare().apply {
                                        compareMethod = it2?.compareMethod ?: ""
                                        value = it2?.value ?: 0.0
                                    }
                                }
                            }
                        }

                        enemyHealthEffect = s.enemyHealthEffect?.let { it1 ->
                            HealthEffect().apply {
                                bodyParts = it1.bodyParts.filterNotNull().toRealmList()
                                effects = it1.effects.filterNotNull().toRealmList()
                                time = it1.time.let { it2 ->
                                    NumberCompare().apply {
                                        compareMethod = it2?.compareMethod ?: ""
                                        value = it2?.value ?: 0.0
                                    }
                                }
                            }
                        }
                    }
                }

                taskObjectiveSkill = task?.asTaskObjectiveSkill()?.let { s ->
                    TaskObjective.TaskObjectiveSkill().apply {
                        skillLevel = SkillLevel().apply {
                            level = s.skillLevel.level
                            name = s.skillLevel.name
                        }
                    }
                }

                taskObjectiveTaskStatus = task?.asTaskObjectiveTaskStatus()?.let { t ->
                    TaskObjective.TaskObjectiveTaskStatus().apply {
                        this.task = t.task.id?.let { findObjectById(realm, it) }
                        this.status = t.status.filterNotNull().toRealmList()
                    }
                }

                taskObjectiveTraderLevel = task?.asTaskObjectiveTraderLevel()?.let { t ->
                    TaskObjective.TaskObjectiveTraderLevel().apply {
                        trader = t.trader.id.let { findObjectById(realm, it) }
                        level = t.level
                    }
                }
            }
        }.toRealmList()
        startReward = t.startRewards?.let {
            TaskRewards().apply {
                traderStanding = it.traderStanding.filterNotNull().map {
                    TraderStanding().apply {
                        trader = it.trader.id.let { findObjectById(realm, it) }
                        standing = it.standing
                    }
                }.toRealmList()
                items = it.items.filterNotNull().map { it.toRealm(realm) }.toRealmList()
                offerUnlock = it.offerUnlock.filterNotNull().map {
                    OfferUnlock().apply {
                        id = it.id
                        trader = it.trader.id.let { findObjectById(realm, it) }
                        level = it.level
                        item = it.item.id.let { findObjectById(realm, it) }
                    }
                }.toRealmList()
                skillLevelReward = it.skillLevelReward.filterNotNull().map {
                    SkillLevel().apply {
                        name = it.name
                        level = it.level
                    }
                }.toRealmList()
                traderUnlock = it.traderUnlock.filterNotNull().mapNotNull {
                    findObjectById<Trader>(realm, it.id)
                }.toRealmList()
                craftUnlock = it.craftUnlock.filterNotNull().mapNotNull {
                    findObjectById<Craft>(realm, it.id)
                }.toRealmList()
            }
        }
        finishReward = t.finishRewards?.let {
            TaskRewards().apply {
                traderStanding = it.traderStanding.filterNotNull().map {
                    TraderStanding().apply {
                        trader = it.trader.id.let { findObjectById(realm, it) }
                        standing = it.standing
                    }
                }.toRealmList()
                items = it.items.filterNotNull().map { it.toRealm(realm) }.toRealmList()
                offerUnlock = it.offerUnlock.filterNotNull().map {
                    OfferUnlock().apply {
                        id = it.id
                        trader = it.trader.id.let { findObjectById(realm, it) }
                        level = it.level
                        item = it.item.id.let { findObjectById(realm, it) }
                    }
                }.toRealmList()
                skillLevelReward = it.skillLevelReward.filterNotNull().map {
                    SkillLevel().apply {
                        name = it.name
                        level = it.level
                    }
                }.toRealmList()
                traderUnlock = it.traderUnlock.filterNotNull().mapNotNull {
                    findObjectById<Trader>(realm, it.id)
                }.toRealmList()
                craftUnlock = it.craftUnlock.filterNotNull().mapNotNull {
                    findObjectById<Craft>(realm, it.id)
                }.toRealmList()
            }
        }
        factionName = t.factionName
        neededKeys = t.neededKeys?.filterNotNull()?.map {
            TaskKey().apply {
                keys = it.keys.filterNotNull().mapNotNull { findObjectById<Item>(realm, it.id) }
                    .toRealmList()
                map = it.map?.id?.let { findObjectById(realm, it) }
            }
        }?.toRealmList() ?: realmListOf()
        descriptionMessageId = t.descriptionMessageId
        successMessageId = t.successMessageId
        failMessageId = t.failMessageId
    }
}

val sampleTask = Task().apply {
    id = "5936d90786f7742b1420ba5b"
    name = "Debut"
    trader = Trader().apply {
        id = "54cb50c76803fa8b248b4571"
        name = "Prapor"
        normalizedName = "prapor"

    }
    experience = 1700
    minPlayerLevel = 1
    objectives = listOf(
        TaskObjective().apply {
            id = "5967379186f77463860dadd6"
            type = "shoot"
            optional = false
            taskObjectiveShoot = TaskObjective.TaskObjectiveShoot().apply {
                target = "Scavs"
                count = 5
            }
        },
        TaskObjective().apply {
            id = "596737cb86f77463a8115efd"
            type = "giveItem"
            optional = false
            taskObjectiveItem = TaskObjective.TaskObjectiveItem().apply {
                item = sampleItem
                count = 2
                foundInRaid = false
                dogTagLevel = 0
                maxDurability = 100
                minDurability = 0
            }
        }
    ).toRealmList()
}