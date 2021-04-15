package com.austinhodak.thehideout.bsg.models

data class BaseItem (
    val _id: String,
    val _name: String,
    val _parent: String,
    val _type: String
)

data class NodeWeaponClassPropData (
    val name: String,
    val ShortName: String,
    val Description: String,
)