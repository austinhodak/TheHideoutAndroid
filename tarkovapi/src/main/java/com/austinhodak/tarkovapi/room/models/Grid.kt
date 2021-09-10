package com.austinhodak.tarkovapi.room.models

import java.io.Serializable

data class Grid(
    val _id: String? = null,
    val _name: String? = null,
    val _parent: String? = null,
    val _props: Props? = null,
    val _proto: String? = null
) : Serializable {
    data class Props(
        val cellsH: Int? = null,
        val cellsV: Int? = null,
        val filters: List<Filter?>? = null,
        val isSortingTable: Boolean? = null,
        val maxCount: Int? = null,
        val maxWeight: Int? = null,
        val minCount: Int? = null
    ) : Serializable {
        data class Filter(
            val ExcludedFilter: List<String?>? = null,
            val Filter: List<String?>? = null
        ) : Serializable
    }
}