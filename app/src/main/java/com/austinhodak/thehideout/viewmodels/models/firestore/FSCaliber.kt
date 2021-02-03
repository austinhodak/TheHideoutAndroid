package com.austinhodak.thehideout.viewmodels.models.firestore

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class FSCaliber (
    var name: String? = null,
    var long_name: String? = null,
    var _id: String? = null
)