package com.austinhodak.thehideout.realm.models

import android.text.format.DateUtils
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class Item : RealmObject {
    @PrimaryKey
    var id: String? = null
    var name: String? = null
    var normalizedName: String? = null
    var shortName: String? = null
    var description: String? = null
    var updated: String? = null
    var bsgCategoryId: String? = null

    var basePrice: Int = 0
    var avg24hPrice: Int? = null
    var lastLowPrice: Int? = null
    var changeLast48h: Double? = null
    var changeLast48hPercent: Double? = null
    var low24hPrice: Int? = null
    var high24hPrice: Int? = null
    var lastOfferCount: Int? = null

    var sellFor: RealmList<ItemPrice>? = null
    var buyFor: RealmList<ItemPrice>? = null

    var weight: Double? = null
    var width: Int = 1
    var height: Int = 1
    var backgroundColor: String = "blue"
    var link: String? = null

    var iconLink: String? = null
    var gridImageLink: String? = null
    var baseImageLink: String? = null
    var inspectImageLink: String? = null
    var image512pxLink: String? = null
    var image8xLink: String? = null
    var wikiLink: String? = null

    var conflictingItems: RealmList<Item> = realmListOf()
    var conflictingSlotIds: RealmList<String> = realmListOf()
    var containsItems: RealmList<ContainedItem> = realmListOf()

    var types: RealmList<String> = realmListOf()

    //var usedInTasks: RealmList<Task> = realmListOf()
    //var receivedFromTasks: RealmList<Task> = realmListOf()
    //var bartersFor: RealmList<Barter> = realmListOf()
    //var bartersUsing: RealmList<Barter> = realmListOf()
    //var craftsFor: RealmList<Craft> = realmListOf()
    //var craftsUsing: RealmList<Craft> = realmListOf()

    var properties: ItemProperties? = null

    fun cleanIcon(): String {
        return iconLink ?: gridImageLink ?: ""
    }

    fun fleaBanned(): Boolean {
        return types.contains("noFlea")
    }

    fun pricePerSlot(price: Int = lastLowPrice ?: basePrice): Int {
        return price / (width * height)
    }

    fun updatedTime(resolution: Long = DateUtils.MINUTE_IN_MILLIS, ): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val date = sdf.parse(updated ?: "2021-07-01T08:36:35.194Z") ?: Calendar.getInstance().time
        val currentTime = System.currentTimeMillis()
        val timeString = DateUtils.getRelativeTimeSpanString(date.time, currentTime, resolution, DateUtils.FORMAT_ABBREV_RELATIVE).toString()

        return timeString.removeSuffix(". ago")
    }

    fun flea(): ItemPrice? = buyFor?.first { it.vendor?.isFleaMarket() == true }

    fun sellTraders(): List<ItemPrice>? = sellFor?.filter { it.vendor?.traderOffer != null }

    fun buyTraders(): List<ItemPrice>? = buyFor?.filter { it.vendor?.traderOffer != null }

    fun highestTraderSell(): ItemPrice? = sellTraders()?.maxByOrNull { it.priceRUB ?: 0 }

    fun lowestTraderBuy(): ItemPrice? = buyTraders()?.minByOrNull { it.priceRUB ?: 0 }

    fun bestBuyPrice(): ItemPrice? {
        return buyFor?.minByOrNull { it.priceRUB ?: 0 }
    }

    class ContainedItem : EmbeddedRealmObject {
        var item: Item? = null
        var count: Double = 0.0
        var attributes: RealmList<ItemAttribute> = realmListOf()

        class ItemAttribute : EmbeddedRealmObject {
            var type: String = ""
            var name: String = ""
            var value: String = ""
        }
    }

    class ItemPrice : EmbeddedRealmObject {
        var price: Int? = null
        var currency: String? = null
        var currencyItem: Item? = null
        var priceRUB: Int? = null
        var vendor: Vendor? = null

        class Vendor : EmbeddedRealmObject {
            var name: String = ""
            var normalizedName: String = ""
            var traderOffer: TraderOffer? = null
            var fleaMarket: FleaMarket? = null

            class FleaMarket : EmbeddedRealmObject {
                var minPlayerLevel: Int = 15
                var enabled: Boolean = true
                var sellOfferFeeRate: Double = 0.0
                var sellRequirementFeeRate: Double = 0.0
            }

            class TraderOffer : EmbeddedRealmObject {
                var trader: Trader? = null
                var minTraderLevel: Int? = null
                var taskUnlock: Task? = null
            }

            fun isTraderOffer(): Boolean {
                return traderOffer != null
            }

            fun isFleaMarket(): Boolean {
                return fleaMarket != null
            }
        }
    }

    class ItemFilters : EmbeddedRealmObject {
        var allowedCategories: RealmList<ItemCategory> = realmListOf()
        var allowedItems: RealmList<Item> = realmListOf()
        var excludedCategories: RealmList<ItemCategory> = realmListOf()
        var excludedItems: RealmList<Item> = realmListOf()
    }

    class ItemProperties: EmbeddedRealmObject {
        var ammo: ItemPropertiesAmmo? = null
        var armor: ItemPropertiesArmor? = null
        var armorAttachment: ItemPropertiesArmorAttachment? = null
        var backpack: ItemPropertiesBackpack? = null
        var barrel: ItemPropertiesBarrel? = null
        var chestRig: ItemPropertiesChestRig? = null
        var container: ItemPropertiesContainer? = null
        var foodDrink: ItemPropertiesFoodDrink? = null
        var glasses: ItemPropertiesGlasses? = null
        var grenade: ItemPropertiesGrenade? = null
        var helmet: ItemPropertiesHelmet? = null
        var key: ItemPropertiesKey? = null
        var magazine: ItemPropertiesMagazine? = null
        var medicalItem: ItemPropertiesMedicalItem? = null
        var melee: ItemPropertiesMelee? = null
        var medKit: ItemPropertiesMedKit? = null
        var nightVision: ItemPropertiesNightVision? = null
        var painkiller: ItemPropertiesPainkiller? = null
        var preset: ItemPropertiesPreset? = null
        var scope: ItemPropertiesScope? = null
        var surgicalKit: ItemPropertiesSurgicalKit? = null
        var weapon: ItemPropertiesWeapon? = null
        var weaponMod: ItemPropertiesWeaponMod? = null
        var stim: ItemPropertiesStim? = null
    }

    class ItemPropertiesAmmo : EmbeddedRealmObject {
        var caliber: String? = null
        var stackMaxSize: Int? = null
        var tracer: Boolean? = null
        var tracerColor: String? = null
        var ammoType: String? = null
        var projectileCount: Int? = null
        var damage: Int? = null
        var armorDamage: Int? = null
        var fragmentationChance: Double? = null
        var ricochetChance: Double? = null
        var penetrationChance: Double? = null
        var penetrationPower: Int? = null
        var accuracyModifier: Double? = null
        var recoilModifier: Double? = null
        var initialSpeed: Double? = null
        var lightBleedModifier: Double? = null
        var heavyBleedModifier: Double? = null
        var durabilityBurnFactor: Double? = null
        var heatFactor: Double? = null
    }

    class ItemPropertiesArmor : EmbeddedRealmObject {
        var `class`: Int? = null
        var durability: Int? = null
        var repairCost: Int? = null
        var speedPenalty: Double? = null
        var turnPenalty: Double? = null
        var ergoPenalty: Double? = null
        var zones: RealmList<String> = realmListOf()
        var material: ArmorMaterial? = null
    }

    class ItemPropertiesArmorAttachment : EmbeddedRealmObject {
        var `class`: Int? = null
        var durability: Int? = null
        var repairCost: Int? = null
        var speedPenalty: Double? = null
        var turnPenalty: Double? = null
        var ergoPenalty: Double? = null
        var headZones: RealmList<String> = realmListOf()
        var material: ArmorMaterial? = null
        var blindnessProtection: Int? = null
    }

    class ItemPropertiesBackpack : EmbeddedRealmObject {
        var capacity: Int? = null
        var grids: RealmList<ItemStorageGrid> = realmListOf()
    }

    class ItemPropertiesBarrel : EmbeddedRealmObject {
        var ergonomics: Double? = null
        var recoilModifier: Double? = null
        var centerOfImpact: Double? = null
        var deviationCurve: Double? = null
        var deviationMax: Double? = null
        var slots: RealmList<ItemSlot> = realmListOf()
    }

    class ItemPropertiesChestRig : EmbeddedRealmObject {
        var `class`: Int? = null
        var durability: Int? = null
        var repairCost: Int? = null
        var speedPenalty: Double? = null
        var turnPenalty: Double? = null
        var ergoPenalty: Double? = null
        var zones: RealmList<String> = realmListOf()
        var material: ArmorMaterial? = null
        var capacity: Int? = null
        var girds: RealmList<ItemStorageGrid> = realmListOf()
    }

    class ItemPropertiesContainer : EmbeddedRealmObject {
        var capacity: Int? = null
        var grids: RealmList<ItemStorageGrid> = realmListOf()
    }

    class ItemPropertiesFoodDrink : EmbeddedRealmObject {
        var energy: Int? = null
        var hydration: Int? = null
        var units: Int? = null
        var stimEffects: RealmList<StimEffect> = realmListOf()
    }

    class ItemPropertiesGlasses : EmbeddedRealmObject {
        var `class`: Int? = null
        var durability: Int? = null
        var repairCost: Int? = null
        var blindProtection: Int? = null
        var material: ArmorMaterial? = null
    }

    class ItemPropertiesGrenade : EmbeddedRealmObject {
        var type: String? = null
        var fuse: Double? = null
        var minExplodeDistance: Double? = null
        var maxExplodeDistance: Double? = null
        var fragments: Int? = null
        var contusionRadius: Double? = null
    }

    class ItemPropertiesHelmet : EmbeddedRealmObject {
        var `class`: Int? = null
        var durability: Int? = null
        var repairCost: Int? = null
        var speedPenalty: Double? = null
        var turnPenalty: Double? = null
        var ergoPenalty: Double? = null
        var headZones: RealmList<String> = realmListOf()
        var material: ArmorMaterial? = null
        var deafening: String? = null
        var blocksHeadset: Boolean? = null
        var blindnessProtection: Int? = null
        var slots: RealmList<ItemSlot> = realmListOf()
        var ricochetX: Double? = null
        var ricochetY: Double? = null
        var ricochetZ: Double? = null
    }

    class ItemPropertiesKey : EmbeddedRealmObject {
        var uses: Int? = null
    }

    class ItemPropertiesMagazine : EmbeddedRealmObject {
        var ergonomics: Double? = null
        var recoilModifier: Double? = null
        var capacity: Int? = null
        var loadModifier: Double? = null
        var ammoCheckModifier: Double? = null
        var malfunctionChance: Double? = null
        var slots: RealmList<ItemSlot> = realmListOf()
        var allowedAmmo: RealmList<Item> = realmListOf()
    }

    class ItemPropertiesMedicalItem : EmbeddedRealmObject {
        var uses: Int? = null
        var useTime: Int? = null
        var cures: RealmList<String> = realmListOf()
    }

    class ItemPropertiesMedKit : EmbeddedRealmObject {
        var hitpoints: Int? = null
        var useTime: Int? = null
        var maxHealPerUse: Int? = null
        var cures: RealmList<String> = realmListOf()
        var hpCostLightBleeding: Int? = null
        var hpCostHeavyBleeding: Int? = null
    }

    class ItemPropertiesMelee : EmbeddedRealmObject {
        var slashDamage: Int? = null
        var stabDamage: Int? = null
        var hitRadius: Double? = null
    }

    class ItemPropertiesNightVision : EmbeddedRealmObject {
        var intensity: Double? = null
        var noiseIntensity: Double? = null
        var noiseScale: Double? = null
        var diffuseIntensity: Double? = null
    }

    class ItemPropertiesPainkiller : EmbeddedRealmObject {
        var uses: Int? = null
        var useTime: Int? = null
        var cures: RealmList<String> = realmListOf()
        var painkillerDuration: Int? = null
        var energyImpact: Int? = null
        var hydrationImpact: Int? = null
    }

    class ItemPropertiesPreset : EmbeddedRealmObject {
        var baseItem: Item? = null
        var ergonomics: Double? = null
        var recoilVertical: Int? = null
        var recoilHorizontal: Int? = null
        var moa: Double? = null
    }

    class ItemPropertiesScope : EmbeddedRealmObject {
        var ergonomics: Double? = null
        var sightModes: RealmList<Int> = realmListOf()
        var sightingRange: Int? = null
        var recoilModifier: Double? = null
        var slots: RealmList<ItemSlot> = realmListOf()
        var zoomLevels: RealmList<ZoomLevel> = realmListOf()

        class ZoomLevel : EmbeddedRealmObject {
            var zoom: RealmList<Double> = realmListOf()
        }
    }

    class ItemPropertiesStim : EmbeddedRealmObject {
        var useTime: Int? = null
        var cures: RealmList<String> = realmListOf()
        var stimEffects: RealmList<StimEffect> = realmListOf()
    }

    class ItemPropertiesSurgicalKit : EmbeddedRealmObject {
        var uses: Int? = null
        var useTime: Int? = null
        var cures: RealmList<String> = realmListOf()
        var minLimbHealth: Double? = null
        var maxLimbHealth: Double? = null
    }

    class ItemPropertiesWeapon : EmbeddedRealmObject {
        var caliber: String? = null
        var defaultAmmo: Item? = null
        var effectiveDistance: Int? = null
        var ergonomics: Double? = null
        var fireModes: RealmList<String> = realmListOf()
        var fireRate: Int? = null
        var maxDurability: Int? = null
        var recoilVertical: Int? = null
        var recoilHorizontal: Int? = null
        var repairCost: Int? = null
        var sightingRange: Int? = null
        var centerOfImpact: Double? = null
        var deviationCurve: Double? = null
        var deviationMax: Double? = null
        var defaultWidth: Double? = null
        var defaultHeight: Double? = null
        var defaultErgonomics: Double? = null
        var defaultRecoilVertical: Int? = null
        var defaultRecoilHorizontal: Int? = null
        var defaultWeight: Double? = null
        var defaultPreset: Item? = null
        var presets: RealmList<Item> = realmListOf()
        var slots: RealmList<ItemSlot> = realmListOf()
        var allowedAmmo: RealmList<Item> = realmListOf()
    }

    class ItemPropertiesWeaponMod : EmbeddedRealmObject {
        var ergonomics: Double? = null
        var recoilModifier: Double? = null
        var accuracyModifier: Double? = null
        var slots: RealmList<ItemSlot> = realmListOf()
    }
}

class ArmorMaterial : EmbeddedRealmObject {
    var id: String? = null
    var name: String? = null
    var destructibility: Double? = null
    var minRepairDegradation: Double? = null
    var maxRepairDegradation: Double? = null
    var explosionDestructibility: Double? = null
    var minRepairKitDegradation: Double? = null
    var maxRepairKitDegradation: Double? = null
}

class ItemSlot : EmbeddedRealmObject {
    var id: String = ""
    var name: String = ""
    var nameId: String = ""
    var filters: Item.ItemFilters? = null
    var required: Boolean? = null
}

class ItemStorageGrid : EmbeddedRealmObject {
    var width: Int = 0
    var height: Int = 0
    var filters: Item.ItemFilters? = null
}

class StimEffect : EmbeddedRealmObject {
    var type: String = ""
    var chance: Double = 0.0
    var delay: Double = 0.0
    var duration: Double = 0.0
    var value: Double = 0.0
    var period: Double = 0.0
    var skillName: String? = null
}

val sampleItem = Item().apply {
    id = "54491c4f4bdc2db1078b4568"
    name = "MP-133 12ga pump-action shotgun"
    shortName = "MP-133"
    basePrice = 14443
    lastLowPrice = 20000
    iconLink = "https://assets.tarkov.dev/54491c4f4bdc2db1078b4568-icon.jpg"
    gridImageLink = "https://assets.tarkov.dev/54491c4f4bdc2db1078b4568-grid-image.jpg"
    baseImageLink = "https://assets.tarkov.dev/54491c4f4bdc2db1078b4568-base-image.png"
    image512pxLink = "https://assets.tarkov.dev/54491c4f4bdc2db1078b4568-512.webp"
}