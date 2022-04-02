package com.austinhodak.thehideout.map.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.encoders.annotations.Encodable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CustomMarker(
    @DocumentId
    val id: String? = null,
    var title: String? = null,
    var description: String? = null,
    val map: String? = null,
    val coordinates: GeoPoint? = null,
    var icon: String? = null
) : Parcelable {

    fun longitude() = coordinates?.longitude
    fun latitude() = coordinates?.latitude

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()?.let {
            val lat = it.split(";")[0].toDouble()
            val lon = it.split(";")[1].toDouble()
            GeoPoint(lat, lon)
        },
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(map)
        parcel.writeString("${coordinates?.latitude};${coordinates?.longitude}")
        parcel.writeString(icon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomMarker> {
        override fun createFromParcel(parcel: Parcel): CustomMarker {
            return CustomMarker(parcel)
        }

        override fun newArray(size: Int): Array<CustomMarker?> {
            return arrayOfNulls(size)
        }
    }

}
