package com.austinhodak.thehideout.bsg.models.weapon

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemWeaponBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.io.Serializable

data class BsgWeapon(
    val _id: String,
    val _name: String,
    val _parent: String,
    val _props: Props,
    val _proto: String?,
    val _type: String,
    val fleaItem: FleaItem?,

) : Serializable, AbstractBindingItem<ItemWeaponBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_weapon_id


    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemWeaponBinding {
        return ItemWeaponBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemWeaponBinding, payloads: List<Any>) {
        binding.weapon = this
    }

    data class Props(
        val AdjustCollimatorsToTrajectory: Boolean,
        val AimPlane: Double,
        val AimSensitivity: Double,
        val AllowSpawnOnLocations: List<Any>,
        val AnimationVariantsNumber: Int,
        val BackgroundColor: String,
        val BoltAction: Boolean,
        val BurstShotsCount: Int,
        val CameraRecoil: Double,
        val CameraSnap: Double,
        val CanPutIntoDuringTheRaid: Boolean,
        val CanRequireOnRagfair: Boolean,
        val CanSellOnRagfair: Boolean,
        val CantRemoveFromSlotsDuringRaid: List<Any>,
        val CenterOfImpact: Double,
        val Chambers: List<Chamber>,
        val ChangePriceCoef: Int,
        val CompactHandling: Boolean,
        val ConflictingItems: List<Any>,
        val Convergence: Double,
        val CreditsPrice: Int,
        val Description: String,
        val DeviationCurve: Int,
        val DeviationMax: Int,
        val DiscardingBlock: Boolean,
        val Durability: Int,
        val Ergonomics: Int,
        val ExamineExperience: Int,
        val ExamineTime: Int,
        val ExaminedByDefault: Boolean,
        val ExtraSizeDown: Int,
        val ExtraSizeForceAdd: Boolean,
        val ExtraSizeLeft: Int,
        val ExtraSizeRight: Int,
        val ExtraSizeUp: Int,
        val FixedPrice: Boolean,
        val Foldable: Boolean,
        val FoldedSlot: String,
        val Grids: List<Any>,
        val Height: Int,
        val HideEntrails: Boolean,
        val HipAccuracyRestorationDelay: Double,
        val HipAccuracyRestorationSpeed: Int,
        val HipInnaccuracyGain: Double,
        val IronSightRange: Int,
        val IsAlwaysAvailableForInsurance: Boolean,
        val IsLockedafterEquip: Boolean,
        val IsUnbuyable: Boolean,
        val IsUndiscardable: Boolean,
        val IsUngivable: Boolean,
        val IsUnsaleable: Boolean,
        val ItemSound: String,
        val LootExperience: Int,
        val ManualBoltCatch: Boolean,
        val MaxDurability: Int,
        val MaxRepairDegradation: Double,
        val MergesWithChildren: Boolean,
        val MinRepairDegradation: Int,
        val MustBoltBeOpennedForExternalReload: Boolean,
        val MustBoltBeOpennedForInternalReload: Boolean,
        val Name: String,
        val NotShownInSlot: Boolean,
        val OperatingResource: Int,
        val Prefab: _Prefab,
        val QuestItem: Boolean,
        val RagFairCommissionModifier: Int,
        val Rarity: String,
        val RecoilAngle: Int,
        val RecoilCenter: _RecoilCenter,
        val RecoilForceBack: Int,
        val RecoilForceUp: Int,
        val RecolDispersion: Int,
        val ReloadMode: String,
        val RepairComplexity: Int,
        val RepairCost: Int,
        val RepairSpeed: Int,
        val Retractable: Boolean,
        val RotationCenter: _RotationCenter,
        val RotationCenterNoStock: _RotationCenterNoStock,
        val SendToClient: Boolean,
        val ShortName: String,
        val SightingRange: Int,
        val SizeReduceRight: Int,
        val Slots: List<Slot>,
        val SpawnChance: Double,
        val StackMaxSize: Int,
        val StackObjectsCount: Int,
        val TacticalReloadFixation: Double,
        val TacticalReloadStiffnes: _TacticalReloadStiffnes,
        val Unlootable: Boolean,
        val UnlootableFromSide: List<Any>,
        val UnlootableFromSlot: String,
        val UsePrefab: _UsePrefab,
        val Velocity: Double,
        val Weight: Double,
        val Width: Int,
        val ammoCaliber: String,
        val bEffDist: Int,
        val bFirerate: Int,
        val bHearDist: Int,
        val chamberAmmoCount: Int,
        val defAmmo: String,
        val defMagType: String,
        val durabSpawnMax: Int,
        val durabSpawnMin: Int,
        val isBoltCatch: Boolean,
        val isChamberLoad: Boolean,
        val isFastReload: Boolean,
        val shotgunDispersion: Int,
        val weapClass: String,
        val weapFireType: List<String>,
        val weapUseType: String
    ) : Serializable {
        data class Chamber(
            val _id: String,
            val _mergeSlotWithChildren: Boolean,
            val _name: String,
            val _parent: String,
            val _props: Props,
            val _proto: String,
            val _required: Boolean
        ) : Serializable {
            data class Props(
                val filters: List<Filter>
            ) : Serializable {
                data class Filter(
                    val Filter: List<String>
                ) : Serializable
            }
        }

        data class _Prefab(
            val path: String,
            val rcid: String
        ) : Serializable

        data class _RecoilCenter(
            val x: Int,
            val y: Double,
            val z: Double
        ) : Serializable

        data class _RotationCenter(
            val x: Int,
            val y: Double,
            val z: Double
        ) : Serializable

        data class _RotationCenterNoStock(
            val x: Int,
            val y: Double,
            val z: Double
        ) : Serializable

        data class Slot(
            val _id: String,
            val _mergeSlotWithChildren: Boolean,
            val _name: String,
            val _parent: String,
            val _props: Props,
            val _proto: String,
            val _required: Boolean
        ) : Serializable {
            data class Props(
                val filters: List<Filter>
            ) : Serializable {
                data class Filter(
                    val AnimationIndex: Int,
                    val Filter: List<String>,
                    val Shift: Int
                ) : Serializable
            }
        }

        data class _TacticalReloadStiffnes(
            val x: Double,
            val y: Double,
            val z: Double
        ) : Serializable

        data class _UsePrefab(
            val path: String,
            val rcid: String
        ) : Serializable
    }

    fun getCaliberName(): String = com.austinhodak.thehideout.utils.getCaliberName(_props.ammoCaliber)
}