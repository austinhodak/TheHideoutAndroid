package com.austinhodak.thehideout.hideout.models

data class Module(
    var module: String,
    var level: Int,
    var function: String,
    var imgSource: String,
    var require: List<ModuleRequire>,
    var id: Int
) {
    data class ModuleRequire(
        var tyoe: String,
        var name: String,
        var quantity: Int,
        var id: Int
    )
}