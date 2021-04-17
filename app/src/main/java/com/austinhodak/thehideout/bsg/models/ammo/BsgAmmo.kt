package com.austinhodak.thehideout.bsg.models.ammo

data class BsgAmmo(
    val _id: String,
    val _name: String,
    val _parent: String,
    val _props: Props,
    val _proto: String,
    val _type: String
) {
    data class Props(
        val AllowSpawnOnLocations: List<Any>,
        val AmmoLifeTimeSec: Int,
        val AnimationVariantsNumber: Int,
        val ArmorDamage: Int,
        val ArmorDistanceDistanceDamage: _ArmorDistanceDistanceDamage,
        val BackgroundColor: String,
        val BallisticCoeficient: Double,
        val Blindness: _Blindness,
        val Caliber: String,
        val CanRequireOnRagfair: Boolean,
        val CanSellOnRagfair: Boolean,
        val ChangePriceCoef: Double,
        val ConflictingItems: List<Any>,
        val Contusion: _Contusion,
        val CreditsPrice: Int,
        val Damage: Int,
        val Description: String,
        val Deterioration: Int,
        val DiscardingBlock: Boolean,
        val ExamineExperience: Int,
        val ExamineTime: Int,
        val ExaminedByDefault: Boolean,
        val ExplosionStrength: Int,
        val ExplosionType: String,
        val ExtraSizeDown: Int,
        val ExtraSizeForceAdd: Boolean,
        val ExtraSizeLeft: Int,
        val ExtraSizeRight: Int,
        val ExtraSizeUp: Int,
        val FixedPrice: Boolean,
        val FragmentType: String,
        val FragmentationChance: Double,
        val FragmentsCount: Int,
        val FuzeArmTimeSec: Double,
        val HasGrenaderComponent: Boolean,
        val HeavyBleedingDelta: Double,
        val Height: Int,
        val HideEntrails: Boolean,
        val InitialSpeed: Int,
        val IsAlwaysAvailableForInsurance: Boolean,
        val IsLightAndSoundShot: Boolean,
        val IsLockedafterEquip: Boolean,
        val IsUnbuyable: Boolean,
        val IsUndiscardable: Boolean,
        val IsUngivable: Boolean,
        val IsUnsaleable: Boolean,
        val ItemSound: String,
        val LightAndSoundShotAngle: Double,
        val LightAndSoundShotSelfContusionStrength: Double,
        val LightAndSoundShotSelfContusionTime: Double,
        val LightBleedingDelta: Double,
        val LootExperience: Int,
        val MaxExplosionDistance: Int,
        val MaxFragmentsCount: Int,
        val MergesWithChildren: Boolean,
        val MinExplosionDistance: Int,
        val MinFragmentsCount: Int,
        val MisfireChance: Double,
        val Name: String,
        val NotShownInSlot: Boolean,
        val PenetrationChance: Double,
        val PenetrationPower: Int,
        val PenetrationPowerDiviation: Double,
        val Prefab: _Prefab,
        val ProjectileCount: Int,
        val QuestItem: Boolean,
        val RagFairCommissionModifier: Int,
        val Rarity: String,
        val RepairCost: Int,
        val RepairSpeed: Int,
        val RicochetChance: Double,
        val SendToClient: Boolean,
        val ShortName: String,
        val ShowBullet: Boolean,
        val ShowHitEffectOnExplode: Boolean,
        val SpawnChance: Int,
        val SpeedRetardation: Double,
        val StackMaxRandom: Int,
        val StackMaxSize: Int,
        val StackMinRandom: Int,
        val StackObjectsCount: Int,
        val StaminaBurnPerDamage: Double,
        val Tracer: Boolean,
        val TracerColor: String,
        val TracerDistance: Double,
        val Unlootable: Boolean,
        val UnlootableFromSide: List<Any>,
        val UnlootableFromSlot: String,
        val UsePrefab: _UsePrefab,
        val Weight: Double,
        val Width: Int,
        val ammoAccr: Int,
        val ammoDist: Int,
        val ammoHear: Int,
        val ammoRec: Int,
        val ammoSfx: String,
        val ammoShiftChance: Int,
        val ammoType: String,
        val buckshotBullets: Int,
        val casingEjectPower: Int,
        val casingMass: Double,
        val casingName: String,
        val casingSounds: String
    ) {
        data class _ArmorDistanceDistanceDamage(
            val x: Double,
            val y: Double,
            val z: Double
        )

        data class _Blindness(
            val x: Double,
            val y: Double,
            val z: Double
        )

        data class _Contusion(
            val x: Double,
            val y: Double,
            val z: Double
        )

        data class _Prefab(
            val path: String,
            val rcid: String
        )

        data class _UsePrefab(
            val path: String,
            val rcid: String
        )
    }


}