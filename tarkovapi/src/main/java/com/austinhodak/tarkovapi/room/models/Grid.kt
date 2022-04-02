package com.austinhodak.tarkovapi.room.models

import java.io.Serializable

data class Grid(
    val _id: String? = null,
    val _name: String? = null,
    val _parent: String? = null,
    val _props: Props? = null,
    val _proto: String? = null
) : Serializable {

    fun getFilters(): List<String> {
        if (_props?.filters?.isNullOrEmpty() == true) return emptyList()
        return _props?.filters?.first()?.Filter?.filterNotNull() ?: emptyList()
    }

    fun getInternalSlots(): Int = _props?.cellsH?.times(_props.cellsV ?: 1) ?: 0

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