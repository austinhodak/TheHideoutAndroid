package com.austinhodak.tarkovapi.utils

import com.austinhodak.tarkovapi.BartersQuery
import com.austinhodak.tarkovapi.CraftsQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.QuestsQuery
import com.austinhodak.tarkovapi.fragment.ContainsItem
import com.austinhodak.tarkovapi.fragment.ItemFragment
import com.austinhodak.tarkovapi.fragment.ItemPrice
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.type.ItemType
import org.json.JSONObject
import kotlin.math.roundToInt

//Maps TarkovTools Quest Item to our Quest item.
fun QuestsQuery.Quest.toQuest(): Quest {
    val quest = this.questFragment
    return Quest(
        quest?.id!!,
        quest.title,
        quest.wikiLink,
        quest.exp,
        quest.giver.traderFragment,
        quest.turnin.traderFragment,
        quest.unlocks,
        Quest.QuestRequirement(
            level = quest.requirements?.level,
            quests = quest.requirements?.quests
        ),
        quest.objectives.map {
            val obj = it?.objectiveFragment
            Quest.QuestObjective(
                obj?.id,
                obj?.type,
                obj?.target,
                obj?.number,
                obj?.location,
                obj?.targetItem?.itemFragment?.toClass()
            )
        }
    )
}

//Determines item type from the raw item file
fun JSONObject.itemType(): ItemTypes {
    val props = getJSONObject("_props")
    if (
        getString("_name").equals("Ammo")
        || getString("_parent").isNullOrBlank()
        || getString("_parent") == "54009119af1c881c07000029"
        || getString("_parent") == "5661632d4bdc2d903d8b456b"
    ) {
        return ItemTypes.NULL
    }

    return when {
        props.has("weapFireType") -> ItemTypes.WEAPON
        props.has("Caliber") && !props.getString("Name")
            .contains("Shrapnel", true) -> ItemTypes.AMMO
        props.has("Prefab") && props.getJSONObject("Prefab").getString("path")
            .contains("assets/content/items/mods") -> ItemTypes.MOD
        else -> {
            ItemTypes.NONE
        }
    }
}

//Maps TarkovTools Item to our Pricing Item.
fun ItemFragment.toClass(): Pricing {
    val item = this
    return Pricing(
        item.id,
        item.name,
        item.shortName,
        item.iconLink,
        item.imageLink,
        item.gridImageLink,
        item.avg24hPrice,
        item.basePrice,
        item.lastLowPrice,
        item.changeLast48h,
        item.low24hPrice,
        item.high24hPrice,
        item.updated,
        types = item.types,
        item.width,
        item.height,
        sellFor = item.sellFor?.map { s1 ->
            val s = s1.itemPrice
            s.toBuySell()
        },
        buyFor = item.buyFor?.map { s1 ->
            val s = s1.itemPrice
            s.toBuySell()
        },
        item.wikiLink,
        item.types.contains(ItemType.noFlea),
        containsItem = item.containsItems?.mapNotNull {
            val i = it?.containsItem
            Pricing.Contains(
                quantity = i?.quantity,
                count = i?.count?.toInt(),
                item = i?.toClass()
            )
        }
    )
}

fun ContainsItem.toClass(): Pricing.ContainsItem {
    val item = this.item
    return Pricing.ContainsItem(
        item.id,
        item.name,
        item.shortName,
        item.iconLink,
        item.imageLink,
        item.gridImageLink,
        item.avg24hPrice,
        item.basePrice,
        item.lastLowPrice,
        item.changeLast48h,
        item.low24hPrice,
        item.high24hPrice,
        item.updated,
        types = item.types,
        item.width,
        item.height,
        sellFor = item.sellFor?.map { s1 ->
            val s = s1.itemPrice
            s.toBuySell()
        },
        buyFor = item.buyFor?.map { s1 ->
            val s = s1.itemPrice
            s.toBuySell()
        },
        item.wikiLink,
        item.types.contains(ItemType.noFlea)
    )
}

fun ItemPrice?.toBuySell(): Pricing.BuySellPrice {
    val s = this
    return Pricing.BuySellPrice(
        s?.source?.rawValue,
        s?.price,
        s?.requirements?.mapNotNull { requirement ->
            if (requirement?.type?.rawValue != null && requirement.value != null) {
                Pricing.BuySellPrice.Requirement(
                    requirement.type.rawValue,
                    requirement.value
                )
            } else null
        } ?: emptyList(),
        s?.currency
    )
}

//Maps TarkovTools Craft to our Craft.
fun CraftsQuery.Craft.toCraft(): Craft {
    return Craft(
        duration = duration,
        requiredItems = requiredItems.map {
            Craft.CraftItem(
                it?.taskItem?.count?.roundToInt(),
                it?.taskItem?.item?.itemFragment?.toClass()
            )
        },
        rewardItems = rewardItems.map {
            Craft.CraftItem(
                it?.taskItem?.count?.roundToInt(),
                it?.taskItem?.item?.itemFragment?.toClass()
            )
        },
        source = source
    )
}

//Maps TarkovTools Barter to our barter..
fun BartersQuery.Barter.toBarter(): Barter {
    return Barter(
        requiredItems = requiredItems.map {
            Craft.CraftItem(
                it?.taskItem?.count?.roundToInt(),
                it?.taskItem?.item?.itemFragment?.toClass()
            )
        },
        rewardItems = rewardItems.map {
            Craft.CraftItem(
                it?.taskItem?.count?.roundToInt(),
                it?.taskItem?.item?.itemFragment?.toClass()
            )
        },
        source = source
    )
}

fun ItemsByTypeQuery.ItemsByType.toPricing(): Pricing {
    val item = this.itemFragment
    return item?.toClass()!!
}

fun JSONObject.getItemType(): ItemTypes {
    val props = getJSONObject("_props")
    return when {
        props.has("weapFireType") -> ItemTypes.WEAPON
        props.has("Prefab") && props.getJSONObject("Prefab").getString("path")
            .contains("assets/content/items/mods") -> ItemTypes.MOD
        props.has("Caliber") -> ItemTypes.AMMO
        this.getString("_parent").equals("5448e54d4bdc2dcc718b4568") -> ItemTypes.ARMOR
        this.getString("_parent").equals("5448e5284bdc2dcb718b4567") -> ItemTypes.RIG
        this.getString("_parent").equals("5448e53e4bdc2d60728b4567") -> ItemTypes.BACKPACK
        this.getString("_parent").equals("5a341c4686f77469e155819e") -> ItemTypes.FACECOVER
        this.getString("_parent").equals("5448e5724bdc2ddf718b4568") -> ItemTypes.GLASSES
        this.getString("_parent").equals("543be6564bdc2df4348b4568") -> ItemTypes.GRENADE
        this.getString("_parent").equals("5645bcb74bdc2ded0b8b4578") -> ItemTypes.HEADSET
        this.getString("_parent").equals("5a341c4086f77401f2541505") || this.getString("_parent")
            .equals("57bef4c42459772e8d35a53b") -> ItemTypes.HELMET
        this.getString("_parent").equals("5c99f98d86f7745c314214b3") || this.getString("_parent")
            .equals("5c164d2286f774194c5e69fa") -> ItemTypes.KEY
        this.getString("_parent").equals("5448f3a64bdc2d60728b456a") -> ItemTypes.STIM
        this.getString("_parent").equals("5448f39d4bdc2d0a728b4568") -> ItemTypes.MED
        this.getString("_parent").equals("5448f3ac4bdc2dce718b4569") -> ItemTypes.MED
        this.getString("_parent").equals("5448f3a14bdc2d27728b4569") -> ItemTypes.MED
        this.getString("_parent").equals("5448e8d04bdc2ddf718b4569") -> ItemTypes.FOOD
        this.getString("_parent").equals("5448e8d64bdc2dce718b4568") -> ItemTypes.FOOD
        this.getString("_parent").equals("5447e1d04bdc2dff2f8b4567") -> ItemTypes.MELEE
        else -> ItemTypes.NONE
    }
}