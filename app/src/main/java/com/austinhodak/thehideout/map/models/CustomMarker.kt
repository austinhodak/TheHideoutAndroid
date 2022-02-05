package com.austinhodak.thehideout.map.models

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class CustomMarker(
    val map: String? = null,
    val coordinates: GeoPoint? = null
) : Serializable {

    fun getX() = coordinates?.longitude
    fun getY() = coordinates?.latitude



}
