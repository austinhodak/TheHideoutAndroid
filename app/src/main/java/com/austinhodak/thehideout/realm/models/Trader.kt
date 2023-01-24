package com.austinhodak.thehideout.realm.models

import com.apollographql.apollo3.ApolloClient
import com.austinhodak.thehideout.realm.converters.findObjectById
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Trader : RealmObject {
    @PrimaryKey
    var id: String? = null
    var name: String = ""
    var normalizedName: String = ""
    var description: String? = null
    var resetTime: String? = null
    var currency: Item? = null
    var discount: Double = 0.0
    var levels: RealmList<TraderLevel> = realmListOf()
    var tarkovDataId: Int? = null
    var cashOffers: RealmList<TraderCashOffer> = realmListOf()

    //var barters: RealmList<Barter> = realmListOf()
    //var cashOffers: RealmList<CashOffer> = realmListOf()

    class TraderLevel : EmbeddedRealmObject {
        var level: Int = 0
        var requiredPlayerLevel: Int = 0
        var requiredReputation: Double = 0.0
        var requiredCommerce: Int = 0
        var payRate: Double = 0.0
        var insuranceRate: Double = 0.0
        var repairCostMultiplier: Double = 0.0

        //var barters: RealmList<Barter> = realmListOf()
        var cashOffers: RealmList<TraderCashOffer> = realmListOf()

    }

    class TraderCashOffer : EmbeddedRealmObject {
        var item: Item? = null
        var minTraderLevel: Int? = null
        var price: Int? = null
        var currency: String? = null
        var currencyItem: Item? = null
        var priceRUB: Int? = null

        //var taskUnlock: Task? = null
    }
}

suspend fun com.austinhodak.thehideout.fragment.Trader.asRealmTrader(realm: MutableRealm, apollo: ApolloClient): Trader {
    val oldTrader = this
    val trader = Trader()
    trader.apply {
        id = oldTrader.id
        name = oldTrader.name
        normalizedName = oldTrader.normalizedName
        description = oldTrader.description
        resetTime = oldTrader.resetTime
        currency = findObjectById(realm, oldTrader.currency.id)
        levels = oldTrader.levels.map { l ->
            Trader.TraderLevel().apply {
                level = l.level
                requiredPlayerLevel = l.requiredPlayerLevel
                requiredReputation = l.requiredReputation
                requiredCommerce = l.requiredCommerce
                payRate = l.payRate
                insuranceRate = l.insuranceRate ?: 0.0
                repairCostMultiplier = l.repairCostMultiplier ?: 0.0
                cashOffers = l.cashOffers.map { c ->
                    Trader.TraderCashOffer().apply {
                        item = c?.let {
                            findObjectById(realm, it.item.id)
                        }
                        minTraderLevel = c?.minTraderLevel
                        price = c?.price
                        currency = c?.currency
                        currencyItem = c?.currencyItem?.let {
                            findObjectById(realm, it.id)
                        }
                        priceRUB = c?.priceRUB
                    }
                }.toRealmList()
            }
        }.toRealmList()
    }
    return trader
}