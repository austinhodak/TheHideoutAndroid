package com.austinhodak.thehideout.di

import com.austinhodak.thehideout.realm.models.*
import com.austinhodak.thehideout.realm.models.Map
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.log.LogLevel
import javax.inject.Singleton
import kotlin.collections.setOf

@InstallIn(SingletonComponent::class)
@Module
object RealmModule {

    @Singleton
    @Provides
    fun providesRealm(): Realm {
        val config = RealmConfiguration.Builder(
            setOf(
                Ammo::class,
                Barter::class,
                Craft::class,
                HideoutStation.HideoutStationLevel::class,
                HideoutStation::class,
                Item.ContainedItem.ItemAttribute::class,
                Item.ContainedItem::class,
                Item.ItemPrice.Vendor.FleaMarket::class,
                Item.ItemPrice.Vendor.TraderOffer::class,
                Item.ItemPrice.Vendor::class,
                Item.ItemPrice::class,
                Item::class,
                ItemCategory::class,
                Map.BossSpawn.BossEscort.BossEscortAmount::class,
                Map.BossSpawn.BossEscort::class,
                Map.BossSpawn.BossSpawnLocation::class,
                Map.BossSpawn::class,
                Map::class,
                MobInfo.HealthPart::class,
                MobInfo::class,
                QuestItem::class,
                RequirementHideoutStationLevel::class,
                RequirementItem::class,
                RequirementSkill::class,
                RequirementTrader::class,
                Task::class,
                TaskObjective.TaskObjectiveBuildItem::class,
                TaskObjective.TaskObjectiveExperience::class,
                TaskObjective.TaskObjectiveExtract::class,
                TaskObjective.TaskObjectiveItem::class,
                TaskObjective.TaskObjectiveMark::class,
                TaskObjective.TaskObjectivePlayerLevel::class,
                TaskObjective.TaskObjectiveShoot::class,
                TaskObjective.TaskObjectiveShoot.TaskObjectiveShootList::class,
                TaskObjective.TaskObjectiveSkill::class,
                TaskObjective.TaskObjectiveTaskStatus::class,
                TaskObjective.TaskObjectiveTraderLevel::class,
                TaskObjective.TaskObjectiveQuestItem::class,
                TaskStatusRequirement::class,
                TaskObjective::class,
                TaskRewards::class,
                TraderStanding::class,
                OfferUnlock::class,
                TaskKey::class,
                AttributeThreshold::class,
                NumberCompare::class,
                HealthEffect::class,
                SkillLevel::class,
                Trader.TraderCashOffer::class,
                Trader.TraderLevel::class,
                Trader::class,
                Item.ItemFilters::class,
                Item.ItemProperties::class,
                Item.ItemPropertiesAmmo::class,
                Item.ItemPropertiesArmor::class,
                Item.ItemPropertiesArmorAttachment::class,
                Item.ItemPropertiesBackpack::class,
                Item.ItemPropertiesBarrel::class,
                Item.ItemPropertiesChestRig::class,
                Item.ItemPropertiesContainer::class,
                Item.ItemPropertiesFoodDrink::class,
                Item.ItemPropertiesGlasses::class,
                Item.ItemPropertiesGrenade::class,
                Item.ItemPropertiesHelmet::class,
                Item.ItemPropertiesKey::class,
                Item.ItemPropertiesMagazine::class,
                Item.ItemPropertiesMedicalItem::class,
                Item.ItemPropertiesMedKit::class,
                Item.ItemPropertiesMelee::class,
                Item.ItemPropertiesNightVision::class,
                Item.ItemPropertiesPainkiller::class,
                Item.ItemPropertiesPreset::class,
                Item.ItemPropertiesScope::class,
                Item.ItemPropertiesScope.ZoomLevel::class,
                Item.ItemPropertiesStim::class,
                Item.ItemPropertiesSurgicalKit::class,
                Item.ItemPropertiesWeapon::class,
                Item.ItemPropertiesWeaponMod::class,
                ArmorMaterial::class,
                ItemSlot::class,
                ItemStorageGrid::class,
                StimEffect::class,
            )
        )
            .deleteRealmIfMigrationNeeded()
            .log(LogLevel.ALL).build()
        return Realm.open(config)
    }

}