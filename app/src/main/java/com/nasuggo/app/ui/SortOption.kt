package com.nasuggo.app.ui

import com.nasuggo.app.data.BarangayNames

/**
 * Strategy objects: each option supplies its own comparator.
 * The ViewModel HAS-A SortOption and sorts through the common type (polymorphism + composition).
 */
enum class SortOption(val label: String) {
    NEAREST("Nearest") {
        override fun comparator(): Comparator<BusinessItem> = compareBy { it.distanceMeters }
    },
    BARANGAY("Barangay") {
        override fun comparator(): Comparator<BusinessItem> =
            compareBy<BusinessItem> { BarangayNames.sortKey(it.business.barangay) }
                .thenBy { it.distanceMeters }
    },
    NAME("Name A–Z") {
        override fun comparator(): Comparator<BusinessItem> =
            compareBy { it.business.name.lowercase() }
    };

    abstract fun comparator(): Comparator<BusinessItem>

    companion object {
        fun fromLabel(label: String?): SortOption = values().firstOrNull { it.label == label } ?: NEAREST
    }
}
