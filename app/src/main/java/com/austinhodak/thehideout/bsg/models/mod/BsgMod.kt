package com.austinhodak.thehideout.bsg.models.mod

data class BsgMod(
    val _id: String,
    val _name: String,
    val _parent: String,
    val _props: Props,
    val _proto: String,
    val _type: String
) {
    data class Props(
        val Accuracy: Int,
        val AllowSpawnOnLocations: List<Any>,
        val AnimationVariantsNumber: Int,
        val BackgroundColor: String,
        val BlocksCollapsible: Boolean,
        val BlocksFolding: Boolean,
        val CanPutIntoDuringTheRaid: Boolean,
        val CanRequireOnRagfair: Boolean,
        val CanSellOnRagfair: Boolean,
        val CantRemoveFromSlotsDuringRaid: List<Any>,
        val ChangePriceCoef: Int,
        val ConflictingItems: List<Any>,
        val CreditsPrice: Int,
        val Description: String,
        val DiscardingBlock: Boolean,
        val Durability: Int,
        val EffectiveDistance: Int,
        val Ergonomics: Double,
        val ExamineExperience: Int,
        val ExamineTime: Int,
        val ExaminedByDefault: Boolean,
        val ExtraSizeDown: Int,
        val ExtraSizeForceAdd: Boolean,
        val ExtraSizeLeft: Int,
        val ExtraSizeRight: Int,
        val ExtraSizeUp: Int,
        val FixedPrice: Boolean,
        val Grids: List<Any>,
        val HasShoulderContact: Boolean,
        val Height: Int,
        val HideEntrails: Boolean,
        val IsAlwaysAvailableForInsurance: Boolean,
        val IsAnimated: Boolean,
        val IsLockedafterEquip: Boolean,
        val IsUnbuyable: Boolean,
        val IsUndiscardable: Boolean,
        val IsUngivable: Boolean,
        val IsUnsaleable: Boolean,
        val ItemSound: String,
        val LootExperience: Int,
        val Loudness: Int,
        val MergesWithChildren: Boolean,
        val Name: String,
        val NotShownInSlot: Boolean,
        val Prefab: _Prefab,
        val QuestItem: Boolean,
        val RagFairCommissionModifier: Int,
        val RaidModdable: Boolean,
        val Rarity: String,
        val Recoil: Double,
        val RepairCost: Int,
        val RepairSpeed: Int,
        val SendToClient: Boolean,
        val ShortName: String,
        val SightingRange: Int,
        val Slots: List<Slot>,
        val SpawnChance: Double,
        val StackMaxSize: Int,
        val StackObjectsCount: Int,
        val ToolModdable: Boolean,
        val Unlootable: Boolean,
        val UnlootableFromSide: List<Any>,
        val UnlootableFromSlot: String,
        val UsePrefab: _UsePrefab,
        val Velocity: Double,
        val Weight: Double,
        val Width: Int
    ) {
        data class _Prefab(
            val path: String,
            val rcid: String
        )

        data class Slot(
            val _id: String,
            val _mergeSlotWithChildren: Boolean,
            val _name: String,
            val _parent: String,
            val _props: Props,
            val _proto: String,
            val _required: Boolean
        ) {
            data class Props(
                val filters: List<Filter>
            ) {
                data class Filter(
                    val Filter: List<String>,
                    val Shift: Int
                )
            }
        }

        data class _UsePrefab(
            val path: String,
            val rcid: String
        )
    }
}