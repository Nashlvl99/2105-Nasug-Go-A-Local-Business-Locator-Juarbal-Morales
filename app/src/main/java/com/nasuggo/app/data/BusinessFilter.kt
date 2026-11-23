package com.nasuggo.app.data

data class BusinessFilter(
    val query: String = "",
    val group: BusinessGroup? = null,
    val barangay: String? = null
) {
    fun accepts(b: Business): Boolean =
        (group == null || b.group == group) &&
            (barangay == null || b.barangay == barangay) &&
            b.matches(query)
}
