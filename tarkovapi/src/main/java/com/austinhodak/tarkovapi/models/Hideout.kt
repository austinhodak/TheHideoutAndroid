package com.austinhodak.tarkovapi.models

data class Hideout(
    var modules: List<Module?>? = null,
    var stations: List<Station?>? = null
) {
    data class Module(
        var id: Int? = null,
        var level: Int? = null,
        var module: String? = null,
        var require: List<Require?>? = null,
        var stationId: Int? = null
    ) {
        data class Require(
            var id: Int? = null,
            var name: Any? = null,
            var quantity: Int? = null,
            var type: String? = null
        )
    }

    data class Station(
        var disabled: Boolean? = null,
        var function: String? = null,
        var id: Int? = null,
        var imgSource: String? = null,
        var locales: Locales? = null
    ) {
        data class Locales(
            var en: String? = null
        )
    }
}