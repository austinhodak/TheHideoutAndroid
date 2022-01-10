package com.austinhodak.tarkovapi.room.models

import junit.framework.TestCase

class PricingTest : TestCase() {

    lateinit var pricing: Pricing

    public override fun setUp() {
        super.setUp()
        pricing = Pricing (
            id = "54527a984bdc2d4e668b4567",
            name = "5.56x45mm M855",
            shortName = "M855",
            iconLink = "https://assets.tarkov-tools.com/54527a984bdc2d4e668b4567-icon.jpg",
            imageLink = "https://assets.tarkov-tools.com/54527a984bdc2d4e668b4567-image.jpg",
            gridImageLink = "https://assets.tarkov-tools.com/54527a984bdc2d4e668b4567-grid-image.jpg",
            avg24hPrice = 326,
            basePrice = 110,
            lastLowPrice = 349,
            changeLast48h = -21.66,
            low24hPrice = 160,
            high24hPrice = 1977,
            updated = "2022-01-04T23:23:12.000Z",
            types = emptyList(),
            width = null,
            height = null,
            sellFor = listOf(
                Pricing.BuySellPrice(
                    "prapor",
                    price = 3615,
                    requirements = emptyList()
                ),
                Pricing.BuySellPrice(
                    "fence",
                    price = 2892,
                    requirements = emptyList()
                ),
                Pricing.BuySellPrice(
                    "fleaMarket",
                    price = 12321,
                    requirements = listOf(
                        Pricing.BuySellPrice.Requirement(
                            "playerLevel",
                            15
                        )
                    )
                ),
            ),
            buyFor = listOf(
                Pricing.BuySellPrice(
                    "peacekeeper",
                    price = 2,
                    requirements = listOf(
                        Pricing.BuySellPrice.Requirement(
                            "loyaltyLevel",
                            1
                        )
                    )
                ),
                Pricing.BuySellPrice(
                    "fleaMarket",
                    price = 326,
                    requirements = listOf(
                        Pricing.BuySellPrice.Requirement(
                            "playerLevel",
                            15
                        )
                    )
                )
            ),
            wikiLink = "https://escapefromtarkov.fandom.com/wiki/5.56x45mm_M855",
            false
        )
    }

    fun testGetIcon() {
        assertEquals(pricing.getIcon(),  "https://assets.tarkov-tools.com/54527a984bdc2d4e668b4567-grid-image.jpg")
    }

    fun testGetCleanIcon() {
        assertEquals(pricing.getCleanIcon(),  "https://assets.tarkov-tools.com/54527a984bdc2d4e668b4567-icon.jpg")
    }

    fun testGetCheapestBuy() {
        val cheapest = Pricing.BuySellPrice(
            "peacekeeper",
            price = 2,
            requirements = listOf(
                Pricing.BuySellPrice.Requirement(
                    "loyaltyLevel",
                    1
                )
            )
        )
        assertEquals(pricing.getCheapestBuy(), cheapest)
    }

    fun testGetHighestSell() {
        val highest = Pricing.BuySellPrice(
            "fleaMarket",
            price = 12321,
            requirements = listOf(
                Pricing.BuySellPrice.Requirement(
                    "playerLevel",
                    15
                )
            )
        )

        assertEquals(pricing.getHighestSell(), highest)
    }

    fun testGetHighestSellTrader() {
        val highestTrader = Pricing.BuySellPrice(
            "prapor",
            price = 3615,
            requirements = emptyList()
        )

        assertEquals(pricing.getHighestSellTrader(), highestTrader)
    }

    fun testGetPrice() {
        assertEquals(pricing.getPrice(), 326)
    }

    fun testGetLastPrice() {
        assertEquals(pricing.getPrice(), 349)
    }

    fun testGetInstaProfit() {
        assertEquals(pricing.getInstaProfit(), 3289)
    }

    fun testGetTotalCostWithExplanation() {

    }

    fun testCalculateTax() {

    }
}