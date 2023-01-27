package com.austinhodak.thehideout.realm.converters

import com.apollographql.apollo3.ApolloClient
import com.austinhodak.thehideout.AmmoQuery
import com.austinhodak.thehideout.BartersQuery
import com.austinhodak.thehideout.BossesQuery
import com.austinhodak.thehideout.CraftsQuery
import com.austinhodak.thehideout.HideoutQuery
import com.austinhodak.thehideout.ItemQuery
import com.austinhodak.thehideout.ItemsQuery
import com.austinhodak.thehideout.MapsQuery
import com.austinhodak.thehideout.QuestItemsQuery
import com.austinhodak.thehideout.TaskQuery
import com.austinhodak.thehideout.TasksQuery
import com.austinhodak.thehideout.TradersQuery
import com.austinhodak.thehideout.fragment.ItemFragment
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesAmmo
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesArmor
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesArmorAttachment
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesBackpack
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesBarrel
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesChestRig
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesContainer
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesFoodDrink
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesGlasses
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesGrenade
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesHelmet
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesKey
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesMagazine
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesMedKit
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesMedicalItem
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesMelee
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesNightVision
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesPainkiller
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesPreset
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesScope
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesStim
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesSurgicalKit
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesWeapon
import com.austinhodak.thehideout.fragment.ItemFragment.Properties.Companion.asItemPropertiesWeaponMod
import com.austinhodak.thehideout.fragment.ItemFragment.SellFor.Vendor.Companion.asTraderOffer
import com.austinhodak.thehideout.fragment.ItemPrice.Vendor.Companion.asFleaMarket
import com.austinhodak.thehideout.fragment.ItemPrice.Vendor.Companion.asTraderOffer
import com.austinhodak.thehideout.realm.models.Ammo
import com.austinhodak.thehideout.realm.models.ArmorMaterial
import com.austinhodak.thehideout.realm.models.Barter
import com.austinhodak.thehideout.realm.models.Craft
import com.austinhodak.thehideout.realm.models.HideoutStation
import com.austinhodak.thehideout.realm.models.Item
import com.austinhodak.thehideout.realm.models.Map
import com.austinhodak.thehideout.realm.models.MobInfo
import com.austinhodak.thehideout.realm.models.QuestItem
import com.austinhodak.thehideout.realm.models.StimEffect
import com.austinhodak.thehideout.realm.models.Task
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmObject
import timber.log.Timber
import kotlin.math.roundToInt

suspend fun ItemFragment.toRealmItem(realm: MutableRealm, apollo: ApolloClient): Item {
    val fragment = this
    val item = Item()
    item.apply {
        id = fragment.id
        name = fragment.name
        shortName = fragment.shortName
        normalizedName = fragment.normalizedName
        description = fragment.description
        iconLink = fragment.iconLink
        baseImageLink = fragment.baseImageLink
        inspectImageLink = fragment.inspectImageLink
        image512pxLink = fragment.image512pxLink
        image8xLink = fragment.image8xLink
        gridImageLink = fragment.gridImageLink
        avg24hPrice = fragment.avg24hPrice
        basePrice = fragment.basePrice
        lastLowPrice = fragment.lastLowPrice
        changeLast48h = fragment.changeLast48h
        changeLast48hPercent = fragment.changeLast48hPercent
        low24hPrice = fragment.low24hPrice
        high24hPrice = fragment.high24hPrice
        lastOfferCount = fragment.lastOfferCount
        updated = fragment.updated
        wikiLink = fragment.wikiLink
        width = fragment.width
        height = fragment.height
        weight = fragment.weight
        backgroundColor = fragment.backgroundColor
        types = fragment.types.mapNotNull { it?.rawValue }.toRealmList()
    }.apply {
        sellFor = fragment.sellFor?.map { s ->
            Item.ItemPrice().apply {
                price = s.price
                currency = s.currency
                priceRUB = s.priceRUB
                vendor = Item.ItemPrice.Vendor().apply {
                    name = s.vendor.name
                    normalizedName = s.vendor.normalizedName
                    traderOffer = s.vendor.asTraderOffer()?.let {
                        Item.ItemPrice.Vendor.TraderOffer().apply {
                            trader = findObjectById(realm, it.trader.id)
                            minTraderLevel = it.minTraderLevel
                            it.taskUnlock?.id?.let { it1 ->
                                findObjectById<Task>(realm, it1)?.let { task ->
                                    taskUnlock = task
                                }
                            }
                        }
                    }
                    fleaMarket = s.vendor.asFleaMarket()?.let {
                        Item.ItemPrice.Vendor.FleaMarket().apply {
                            minPlayerLevel = it.minPlayerLevel
                            enabled = it.enabled
                            sellOfferFeeRate = it.sellOfferFeeRate
                            sellRequirementFeeRate = it.sellRequirementFeeRate
                        }
                    }
                }
            }
        }?.toRealmList()
        buyFor = fragment.buyFor?.map { s ->
            Item.ItemPrice().apply {
                price = s.price
                currency = s.currency
                priceRUB = s.priceRUB
                vendor = Item.ItemPrice.Vendor().apply {
                    name = s.vendor.name
                    normalizedName = s.vendor.normalizedName
                    traderOffer = s.vendor.asTraderOffer()?.let {
                        Item.ItemPrice.Vendor.TraderOffer().apply {
                            trader = findObjectById(realm, it.trader.id)
                            minTraderLevel = it.minTraderLevel
                            it.taskUnlock?.id?.let { it1 ->
                                findObjectById<Task>(realm, it1)?.let { task ->
                                    taskUnlock = task
                                }
                            }
                        }
                    }
                    fleaMarket = s.vendor.asFleaMarket()?.let {
                        Item.ItemPrice.Vendor.FleaMarket().apply {
                            minPlayerLevel = it.minPlayerLevel
                            enabled = it.enabled
                            sellOfferFeeRate = it.sellOfferFeeRate
                            sellRequirementFeeRate = it.sellRequirementFeeRate
                        }
                    }
                }
            }
        }?.apply {
            properties = Item.ItemProperties().apply {
                ammo = fragment.properties?.asItemPropertiesAmmo()?.let { ammo ->
                    Item.ItemPropertiesAmmo().apply {
                        caliber = ammo.caliber
                        stackMaxSize = ammo.stackMaxSize
                        tracer = ammo.tracer
                        //tracerColor = ammo.tracerColor
                        ammoType = ammo.ammoType
                        projectileCount = ammo.projectileCount
                        damage = ammo.damage
                        armorDamage = ammo.armorDamage
                        fragmentationChance = ammo.fragmentationChance
                        ricochetChance = ammo.ricochetChance
                        penetrationChance = ammo.penetrationChance
                        penetrationPower = ammo.penetrationPower
                        accuracyModifier = ammo.accuracyModifier
                        recoilModifier = ammo.recoilModifier
                        initialSpeed = ammo.initialSpeed
                        lightBleedModifier = ammo.lightBleedModifier
                        heavyBleedModifier = ammo.heavyBleedModifier
                        durabilityBurnFactor = ammo.durabilityBurnFactor
                        heatFactor = ammo.heatFactor
                    }
                }
                armor = fragment.properties?.asItemPropertiesArmor()?.let { armor ->
                    Item.ItemPropertiesArmor().apply {
                        `class` = armor.`class`
                        durability = armor.durability
                        repairCost = armor.repairCost
                        speedPenalty = armor.speedPenalty
                        turnPenalty = armor.turnPenalty
                        ergoPenalty = armor.ergoPenalty?.toDouble()
                        zones = armor.zones?.filterNotNull()?.toRealmList() ?: realmListOf()
                        material = ArmorMaterial().apply {
                            id = armor.material?.id
                            name = armor.material?.name
                            destructibility = armor.material?.destructibility
                            minRepairDegradation = armor.material?.minRepairDegradation
                            maxRepairDegradation = armor.material?.maxRepairDegradation
                            explosionDestructibility = armor.material?.explosionDestructibility
                            minRepairKitDegradation = armor.material?.minRepairKitDegradation
                            maxRepairDegradation = armor.material?.maxRepairDegradation
                        }
                    }
                }
                armorAttachment = fragment.properties?.asItemPropertiesArmorAttachment()?.let { armorAttachment ->
                    Item.ItemPropertiesArmorAttachment().apply {
                        `class` = armorAttachment.`class`
                        durability = armorAttachment.durability
                        repairCost = armorAttachment.repairCost
                        speedPenalty = armorAttachment.speedPenalty
                        turnPenalty = armorAttachment.turnPenalty
                        ergoPenalty = armorAttachment.ergoPenalty?.toDouble()
                        armorAttachment.headZones?.let {
                            headZones = it.filterNotNull().toRealmList()
                        }
                        material = ArmorMaterial().apply {
                            id = armorAttachment.material?.id
                            name = armorAttachment.material?.name
                            destructibility = armorAttachment.material?.destructibility
                            minRepairDegradation = armorAttachment.material?.minRepairDegradation
                            maxRepairDegradation = armorAttachment.material?.maxRepairDegradation
                            explosionDestructibility = armorAttachment.material?.explosionDestructibility
                            minRepairKitDegradation = armorAttachment.material?.minRepairKitDegradation
                            maxRepairDegradation = armorAttachment.material?.maxRepairDegradation
                        }
                        blindnessProtection = armorAttachment.blindnessProtection?.roundToInt()
                    }
                }
                backpack = fragment.properties?.asItemPropertiesBackpack()?.let { backpack ->
                    Item.ItemPropertiesBackpack().apply {
                        capacity = backpack.capacity
                        //grids = backpack.
                    }
                }
                barrel = fragment.properties?.asItemPropertiesBarrel()?.let { barrel ->
                    Item.ItemPropertiesBarrel().apply {
                        ergonomics = barrel.ergonomics
                        recoilModifier = barrel.recoilModifier
                        centerOfImpact = barrel.centerOfImpact
                        deviationCurve = barrel.deviationCurve
                        deviationMax = barrel.deviationMax
                       // slots = barrel.
                    }
                }
                chestRig = fragment.properties?.asItemPropertiesChestRig()?.let { chestRig ->
                    Item.ItemPropertiesChestRig().apply {
                        `class` = chestRig.`class`
                        durability = chestRig.durability
                        repairCost = chestRig.repairCost
                        speedPenalty = chestRig.speedPenalty
                        turnPenalty = chestRig.turnPenalty
                        ergoPenalty = chestRig.ergoPenalty?.toDouble()
                        chestRig.zones?.let {
                            zones = it.filterNotNull().toRealmList()
                        }
                        material = ArmorMaterial().apply {
                            id = chestRig.material?.id
                            name = chestRig.material?.name
                            destructibility = chestRig.material?.destructibility
                            minRepairDegradation = chestRig.material?.minRepairDegradation
                            maxRepairDegradation = chestRig.material?.maxRepairDegradation
                            explosionDestructibility = chestRig.material?.explosionDestructibility
                            minRepairKitDegradation = chestRig.material?.minRepairKitDegradation
                            maxRepairDegradation = chestRig.material?.maxRepairDegradation
                        }
                        capacity = chestRig.capacity
                        //girds.addAll(armor.girds)
                    }
                }
                container = fragment.properties?.asItemPropertiesContainer()?.let { container ->
                    Item.ItemPropertiesContainer().apply {
                        capacity = container.capacity
                        //grids = container.
                    }
                }
                foodDrink = fragment.properties?.asItemPropertiesFoodDrink()?.let { foodDrink ->
                    Item.ItemPropertiesFoodDrink().apply {
                        energy = foodDrink.energy
                        hydration = foodDrink.hydration
                        units = foodDrink.units
                        //Effects
                    }
                }
                glasses = fragment.properties?.asItemPropertiesGlasses()?.let { glasses ->
                    Item.ItemPropertiesGlasses().apply {
                        `class` = glasses.`class`
                        durability = glasses.durability
                        repairCost = glasses.repairCost
                        material = ArmorMaterial().apply {
                            id = glasses.material?.id
                            name = glasses.material?.name
                            destructibility = glasses.material?.destructibility
                            minRepairDegradation = glasses.material?.minRepairDegradation
                            maxRepairDegradation = glasses.material?.maxRepairDegradation
                            explosionDestructibility = glasses.material?.explosionDestructibility
                            minRepairKitDegradation = glasses.material?.minRepairKitDegradation
                            maxRepairDegradation = glasses.material?.maxRepairDegradation
                        }
                        blindProtection = glasses.blindnessProtection?.roundToInt()
                    }
                }
                grenade = fragment.properties?.asItemPropertiesGrenade()?.let { grenade ->
                    Item.ItemPropertiesGrenade().apply {
                        type = grenade.type
                        fuse = grenade.fuse
                        minExplodeDistance = grenade.minExplosionDistance?.toDouble()
                        maxExplodeDistance = grenade.maxExplosionDistance?.toDouble()
                        fragments = grenade.fragments
                        contusionRadius = grenade.contusionRadius?.toDouble()
                    }
                }
                helmet = fragment.properties?.asItemPropertiesHelmet()?.let { helmet ->
                    Item.ItemPropertiesHelmet().apply {
                        `class` = helmet.`class`
                        durability = helmet.durability
                        repairCost = helmet.repairCost
                        speedPenalty = helmet.speedPenalty
                        turnPenalty = helmet.turnPenalty
                        ergoPenalty = helmet.ergoPenalty?.toDouble()
                        helmet.headZones?.let {
                            headZones = it.filterNotNull().toRealmList()
                        }
                        material = ArmorMaterial().apply {
                            id = helmet.material?.id
                            name = helmet.material?.name
                            destructibility = helmet.material?.destructibility
                            minRepairDegradation = helmet.material?.minRepairDegradation
                            maxRepairDegradation = helmet.material?.maxRepairDegradation
                            explosionDestructibility = helmet.material?.explosionDestructibility
                            minRepairKitDegradation = helmet.material?.minRepairKitDegradation
                            maxRepairDegradation = helmet.material?.maxRepairDegradation
                        }
                        deafening = helmet.deafening
                        blocksHeadset = helmet.blocksHeadset
                        blindnessProtection = helmet.blindnessProtection?.roundToInt()
                        //Slots
                        ricochetX = helmet.ricochetX
                        ricochetY = helmet.ricochetY
                        ricochetZ = helmet.ricochetZ
                    }
                }
                key = fragment.properties?.asItemPropertiesKey()?.let { key ->
                    Item.ItemPropertiesKey().apply {
                        uses = key.uses
                    }
                }
                magazine = fragment.properties?.asItemPropertiesMagazine()?.let { magazine ->
                    Item.ItemPropertiesMagazine().apply {
                        ergonomics = magazine.ergonomics
                        recoilModifier = magazine.recoilModifier
                        capacity = magazine.capacity
                        loadModifier = magazine.loadModifier
                        ammoCheckModifier = magazine.ammoCheckModifier
                        malfunctionChance = magazine.malfunctionChance
                        magazine.allowedAmmo?.mapNotNull { allowedAmmo ->
                            findObjectById<Item>(realm, allowedAmmo!!.id)
                        }?.toRealmList()?.let { list ->
                            allowedAmmo = list
                        }
                        //Slots
                    }
                }
                medicalItem = fragment.properties?.asItemPropertiesMedicalItem()?.let { medicalItem ->
                    Item.ItemPropertiesMedicalItem().apply {
                        uses = medicalItem.uses
                        useTime = medicalItem.useTime
                        medicalItem.cures?.filterNotNull()?.let {
                            cures = it.toRealmList()
                        }
                    }
                }
                medKit = fragment.properties?.asItemPropertiesMedKit()?.let { medKit ->
                    Item.ItemPropertiesMedKit().apply {
                        hitpoints = medKit.hitpoints
                        useTime = medKit.useTime
                        maxHealPerUse = medKit.maxHealPerUse
                        medKit.cures?.filterNotNull()?.let {
                            cures = it.toRealmList()
                        }
                        hpCostLightBleeding = medKit.hpCostLightBleeding
                        hpCostHeavyBleeding = medKit.hpCostHeavyBleeding
                    }
                }
                melee = fragment.properties?.asItemPropertiesMelee()?.let { melee ->
                    Item.ItemPropertiesMelee().apply {
                        slashDamage = melee.slashDamage
                        stabDamage = melee.stabDamage
                        hitRadius = melee.hitRadius
                    }
                }
                nightVision = fragment.properties?.asItemPropertiesNightVision()?.let { nv ->
                    Item.ItemPropertiesNightVision().apply {
                        intensity = nv.intensity
                        noiseIntensity = nv.noiseIntensity
                        noiseScale = nv.noiseScale
                        diffuseIntensity = nv.diffuseIntensity
                    }
                }
                painkiller = fragment.properties?.asItemPropertiesPainkiller()?.let { painkiller ->
                    Item.ItemPropertiesPainkiller().apply {
                        uses = painkiller.uses
                        useTime = painkiller.useTime
                        painkiller.cures?.filterNotNull()?.let {
                            cures = it.toRealmList()
                        }
                        painkillerDuration = painkiller.painkillerDuration
                        energyImpact = painkiller.energyImpact
                        hydrationImpact = painkiller.hydrationImpact
                    }
                }
                preset = fragment.properties?.asItemPropertiesPreset()?.let { preset ->
                    Item.ItemPropertiesPreset().apply {
                        preset.baseItem.let {
                            baseItem = findObjectById(realm, it.id)
                        }
                        ergonomics = preset.ergonomics
                        recoilHorizontal = preset.recoilHorizontal
                        recoilVertical = preset.recoilVertical
                        moa = preset.moa
                    }
                }
                scope = fragment.properties?.asItemPropertiesScope()?.let { scope ->
                    Item.ItemPropertiesScope().apply {
                        ergonomics = scope.ergonomics
                        scope.sightModes?.filterNotNull()?.let {
                            sightModes = it.toRealmList()
                        }
                        sightingRange = scope.sightingRange
                        recoilModifier = scope.recoilModifier
                        zoomLevels = scope.zoomLevels?.filterNotNull()?.map { l1 ->
                            Item.ItemPropertiesScope.ZoomLevel().apply {
                                zoom = l1.filterNotNull().toRealmList()
                            }
                        }?.toRealmList() ?: realmListOf()
                    }
                }
                stim = fragment.properties?.asItemPropertiesStim()?.let { stim ->
                    Item.ItemPropertiesStim().apply {
                        stim.cures?.filterNotNull()?.let {
                            cures = it.toRealmList()
                        }
                        useTime = stim.useTime
                        stim.stimEffects.filterNotNull().map { effect ->
                            StimEffect().apply {
                                type = effect.type
                                chance = effect.chance
                                delay = effect.delay.toDouble()
                                duration = effect.duration.toDouble()
                                value = effect.value
                                //period = effect.period
                                skillName = effect.skillName
                            }
                        }.toRealmList()
                    }
                }
                surgicalKit = fragment.properties?.asItemPropertiesSurgicalKit()?.let { surgicalKit ->
                    Item.ItemPropertiesSurgicalKit().apply {
                        uses = surgicalKit.uses
                        useTime = surgicalKit.useTime

                        surgicalKit.cures?.filterNotNull()?.let {
                            cures = it.toRealmList()
                        }
                    }
                }
                weapon = fragment.properties?.asItemPropertiesWeapon()?.let { weapon ->
                    Item.ItemPropertiesWeapon().apply {
                        caliber = weapon.caliber
                        weapon.defaultAmmo?.id?.let { id ->
                            defaultAmmo = findObjectById(realm, id)
                        }
                        effectiveDistance = weapon.effectiveDistance
                        ergonomics = weapon.ergonomics
                        fireModes = weapon.fireModes?.filterNotNull()?.toRealmList() ?: realmListOf()
                        fireRate = weapon.fireRate
                        maxDurability = weapon.maxDurability
                        recoilVertical = weapon.recoilVertical
                        recoilHorizontal = weapon.recoilHorizontal
                        repairCost = weapon.repairCost
                        sightingRange = weapon.sightingRange
                        centerOfImpact = weapon.centerOfImpact
                        deviationCurve = weapon.deviationCurve
                        deviationMax = weapon.deviationMax
                        defaultWeight = weapon.defaultWeight
                        defaultWidth = weapon.defaultWidth?.toDouble()
                        defaultHeight = weapon.defaultHeight?.toDouble()
                        defaultErgonomics = weapon.defaultErgonomics
                        defaultRecoilHorizontal = weapon.defaultRecoilHorizontal
                        defaultRecoilVertical = weapon.defaultRecoilVertical
                        defaultWeight = weapon.defaultWeight
                        weapon.defaultPreset?.id?.let { id ->
                            defaultPreset = findObjectById(realm, id)
                        }
                        weapon.allowedAmmo?.filterNotNull()?.mapNotNull { ammo ->
                            findObjectById<Item>(realm, ammo.id)
                        }?.toRealmList()?.let { list ->
                            allowedAmmo = list
                        }
                    }
                }
                weaponMod = fragment.properties?.asItemPropertiesWeaponMod()?.let { weaponMod ->
                    Item.ItemPropertiesWeaponMod().apply {
                        ergonomics = weaponMod.ergonomics
                        recoilModifier = weaponMod.recoilModifier
                        accuracyModifier = weaponMod.accuracyModifier
                        //Slots
                    }
                }
            }
        }?.toRealmList()
    }

    return item
}

inline fun <reified T : RealmObject> findObjectById(realm: MutableRealm, id: String): T? {
    Timber.d("findObjectById: $id ${T::class.simpleName}")
    return when (T::class) {
        Ammo::class -> realm.query(T::class, "item.id == '$id'").find().firstOrNull()
        MobInfo::class -> realm.query(T::class, "normalizedName == '$id'").find().firstOrNull()
        else -> realm.query(T::class, "id == '$id'").find().firstOrNull()
    }
}

suspend inline fun <reified T> getApolloData(apollo: ApolloClient): List<T>? {
    return when (T::class) {
        TradersQuery.Data.Trader::class -> {
            apollo.query(TradersQuery()).execute().let { response ->
                response.data?.traders as List<*>
            }
        }

        ItemsQuery.Data.Item::class -> {
            apollo.query(ItemsQuery()).execute().let { response ->
                response.data?.items as List<*>
            }
        }

        QuestItemsQuery.Data.QuestItem::class -> {
            apollo.query(QuestItemsQuery()).execute().let { response ->
                response.data?.questItems as List<*>
            }
        }

        TasksQuery.Data.Task::class -> {
            apollo.query(TasksQuery()).execute().let { response ->
                response.data?.tasks as List<*>
            }
        }

        MapsQuery.Data.Map::class -> {
            apollo.query(MapsQuery()).execute().let { response ->
                response.data?.maps as List<*>
            }
        }

        BossesQuery.Data.Boss::class -> {
            apollo.query(BossesQuery()).execute().let { response ->
                response.data?.bosses as List<*>
            }
        }

        AmmoQuery.Data.Ammo::class -> {
            apollo.query(AmmoQuery()).execute().let { response ->
                response.data?.ammo as List<*>
            }
        }

        CraftsQuery.Data.Craft::class -> {
            apollo.query(CraftsQuery()).execute().let { response ->
                response.data?.crafts as List<*>
            }
        }

        BartersQuery.Data.Barter::class -> {
            apollo.query(BartersQuery()).execute().let { response ->
                response.data?.barters as List<*>
            }
        }

        HideoutQuery.Data.HideoutStation::class -> {
            apollo.query(HideoutQuery()).execute().let { response ->
                response.data?.hideoutStations as List<*>
            }
        }

        else -> {
            null
        }
    } as List<T>?
}

suspend inline fun <reified T> getApolloData(id: String, apollo: ApolloClient): T? {
    Timber.d("Getting API data for $id")
    if (T::class == TradersQuery.Data.Trader::class) {
        return apollo.query(TradersQuery()).execute().let { response ->
            response.data?.traders?.find { it?.id == id } as? T
        }
    }
    if (T::class == ItemFragment::class) {
        return apollo.query(ItemQuery(id)).execute().let { response ->
            response.data?.item as? T
        }
    }
    if (T::class == Task::class) {
        return apollo.query(TaskQuery(id)).execute().let { response ->
            response.data?.task as? T
        }
    }
    if (T::class == Ammo::class) {
        return apollo.query(AmmoQuery()).execute().let { response ->
            response.data?.ammo?.find { it?.item?.id == id } as? T
        }
    }
    if (T::class == Barter::class) {
        return apollo.query(BartersQuery()).execute().let { response ->
            response.data?.barters?.find { it?.id == id } as? T
        }
    }
    if (T::class == Craft::class) {
        return apollo.query(CraftsQuery()).execute().let { response ->
            response.data?.crafts?.find { it?.id == id } as? T
        }
    }
    if (T::class == HideoutStation::class) {
        return apollo.query(HideoutQuery()).execute().let { response ->
            response.data?.hideoutStations?.find { it?.id == id } as? T
        }
    }
    if (T::class == Map::class) {
        return apollo.query(MapsQuery()).execute().let { response ->
            response.data?.maps?.find { it?.id == id } as? T
        }
    }
    if (T::class == QuestItem::class) {
        return apollo.query(QuestItemsQuery()).execute().let { response ->
            response.data?.questItems?.find { it?.id == id } as? T
        }
    }
    if (T::class == MobInfo::class) {
        return apollo.query(BossesQuery()).execute().let { response ->
            response.data?.bosses?.find { it?.normalizedName == id } as? T
        }
    }
    return null
}

