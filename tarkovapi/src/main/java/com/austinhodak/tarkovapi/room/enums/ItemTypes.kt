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

enum class Traders(var id: String, var icon: Int, var int: Int) {
    PRAPOR("Prapor", R.drawable.prapor_portrait,0),
    THERAPIST("Therapist", R.drawable.therapist_portrait, 1),
    FENCE("Fence", R.drawable.fence_portrait,7),
    SKIER("Skier", R.drawable.skier_portrait, 2),
    PEACEKEEPER("Peacekeeper", R.drawable.peacekeeper_portrait, 3),
    MECHANIC("Mechanic", R.drawable.mechanic_portrait, 4),
    RAGMAN("Ragman", R.drawable.ragman_portrait, 5),
    JAEGER("Jaeger", R.drawable.jaeger_portrait ,6),
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