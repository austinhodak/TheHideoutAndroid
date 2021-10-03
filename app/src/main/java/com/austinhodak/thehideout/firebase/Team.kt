package com.austinhodak.thehideout.firebase

data class Team (
    var name: String? = null,
    var members: Map<String, MemberSettings>? = null
) {
    data class MemberSettings (
        var color: String? = null,
    )
}
