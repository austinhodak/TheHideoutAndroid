package com.austinhodak.thehideout.flea_market.models

data class BsgItem(
    val _id: String,
    val _name: String,
    val _parent: String,
    val _props: Props,
    val _proto: String,
    val _type: String
) {
    data class Prefab(
        val path: String,
        val rcid: String
    )

    data class Props(
        val AllowSpawnOnLocations: List<Any>,
        val AnimationVariantsNumber: Int,
        val BackgroundColor: String,
        val CanRequireOnRagfair: Boolean,
        val CanSellOnRagfair: Boolean,
        val ChangePriceCoef: Int,
        val ConflictingItems: List<Any>,
        val CreditsPrice: Int,
        val Description: String,
        val DiscardingBlock: Boolean,
        val ExamineExperience: Int,
        val ExamineTime: Int,
        val ExaminedByDefault: Boolean,
        val ExtraSizeDown: Int,
        val ExtraSizeForceAdd: Boolean,
        val ExtraSizeLeft: Int,
        val ExtraSizeRight: Int,
        val ExtraSizeUp: Int,
        val FixedPrice: Boolean,
        val Height: Int,
        val HideEntrails: Boolean,
        val IsLockedafterEquip: Boolean,
        val IsUnbuyable: Boolean,
        val IsUndiscardable: Boolean,
        val IsUngivable: Boolean,
        val IsUnsaleable: Boolean,
        val ItemSound: String,
        val LootExperience: Int,
        val MaxResource: Int,
        val MergesWithChildren: Boolean,
        val Name: String,
        val NotShownInSlot: Boolean,
        val Prefab: Prefab,
        val QuestItem: Boolean,
        val RagFairCommissionModifier: Int,
        val Rarity: String,
        val RepairCost: Int,
        val RepairSpeed: Int,
        val Resource: Int,
        val SendToClient: Boolean,
        val ShortName: String,
        val SpawnChance: Double,
        val StackMaxSize: Int,
        val StackObjectsCount: Int,
        val Unlootable: Boolean,
        val UnlootableFromSide: List<Any>,
        val UnlootableFromSlot: String,
        val UsePrefab: UsePrefab,
        val Weight: Double,
        val Width: Int
    )

    data class UsePrefab(
        val path: String,
        val rcid: String
    )
}

