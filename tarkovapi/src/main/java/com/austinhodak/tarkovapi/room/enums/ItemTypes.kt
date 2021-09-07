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
    MOD,
    PROVISION,
    RIG,
    STIM,
    WEARABLE,
    WEAPON,
    FOOD
}

enum class Traders(var id: String, var icon: Int) {
    PRAPOR("Prapor", R.drawable.prapor_portrait),
    THERAPIST("Therapist", R.drawable.therapist_portrait),
    FENCE("Fence", R.drawable.fence_portrait),
    SKIER("Skier", R.drawable.skier_portrait),
    PEACEKEEPER("Peacekeeper", R.drawable.peacekeeper_portrait),
    MECHANIC("Mechanic", R.drawable.mechanic_portrait),
    RAGMAN("Ragman", R.drawable.ragman_portrait),
    JAEGER("Jaeger", R.drawable.jaeger_portrait),
}

enum class Maps(var id: String, var int: Int, var icon: Int) {
    ANY("Any", -1, R.drawable.icons8_map_96),
    FACTORY("Factory", 0, R.drawable.icons8_factory_breakdown_96),
    CUSTOMS("Customs", 1, R.drawable.icons8_structural_96),
    WOODS("Woods", 2, R.drawable.icons8_forest_96),
    SHORELINE("Shoreline", 3, R.drawable.icons8_bay_96),
    INTERCHANGE("Interchange", 4, R.drawable.icons8_shopping_mall_96),
    RESERVE("Reserve", 6, R.drawable.icons8_knight_96),
    THELAB("Labs", 5, R.drawable.icons8_laboratory_96)
}