package com.austinhodak.tarkovapi.room.models

data class Filter(
    val _id: String,
    val _mergeSlotWithChildren: Boolean,
    val _name: String,
    val _parent: String,
    //val _props: Props,
    val _proto: String,
    val _required: Boolean
) {
    /*data class Props(
       val filters: List<Filter>
    ) {
        data class Filter(
            val Filter: List<String>,
            val Shift: Int
        )
    }*/
}