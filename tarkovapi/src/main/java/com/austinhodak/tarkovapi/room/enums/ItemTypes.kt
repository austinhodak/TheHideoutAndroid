package com.austinhodak.tarkovapi.room.enums

import com.austinhodak.tarkovapi.R

enum class ItemTypes {
    NULL,
    NONE,
    AMMO,
    ARMOR,
    BACKPACK,
    FACECOVER,
    GLASSES,
    GRENADE,
    GUN,
    HEADSET,
    HELMET,
    KEY,
    MED,
    MELEE,
    MOD,
    PROVISION,
    RIG,
    STIM,
    WEARABLE,
    WEAPON,
    FOOD,
    NOFLEA
}

enum class Traders(var id: String, var icon: Int, var int: Int, var bsgID: String) {
    PRAPOR("Prapor", R.drawable.prapor_portrait,0, "54cb50c76803fa8b248b4571"),
    THERAPIST("Therapist", R.drawable.therapist_portrait, 1, "54cb57776803fa99248b456e"),
    FENCE("Fence", R.drawable.fence_portrait,7, "579dc571d53a0658a154fbec"),
    SKIER("Skier", R.drawable.skier_portrait, 2, "58330581ace78e27b8b10cee"),
    PEACEKEEPER("Peacekeeper", R.drawable.peacekeeper_portrait, 3, "5935c25fb3acc3127c3d8cd9"),
    MECHANIC("Mechanic", R.drawable.mechanic_portrait, 4, "5a7c2eca46aef81a7ca2145d"),
    RAGMAN("Ragman", R.drawable.ragman_portrait, 5, "5ac3b934156ae10c4430e83c"),
    JAEGER("Jaeger", R.drawable.jaeger_portrait ,6, "5c0647fdd443bc2504c2d371"),
}

enum class Maps(var id: String, var int: Int, var icon: Int) {
    ANY("Any", -1, R.drawable.icons8_map_96),
    FACTORY("Factory", 0, R.drawable.icons8_factory_breakdown_96),
    CUSTOMS("Customs", 1, R.drawable.icons8_structural_96),
    WOODS("Woods", 2, R.drawable.icons8_forest_96),
    SHORELINE("Shoreline", 3, R.drawable.icons8_bay_96),
    INTERCHANGE("Interchange", 4, R.drawable.icons8_shopping_mall_96),
    RESERVE("Reserve", 6, R.drawable.icons8_knight_96),
    THELAB("Labs", 5, R.drawable.icons8_laboratory_96),
    LIGHTHOUSE("Lighthouse", 7, R.drawable.icons8_lighthouse_96),
}