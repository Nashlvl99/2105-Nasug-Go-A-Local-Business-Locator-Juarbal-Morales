package com.nasuggo.app.data

object BarangayNames {
    /** "Barangay 10" sorts after "Barangay 2" (numeric order, not alphabetical). */
    fun sortKey(barangay: String): Int =
        barangay.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
}
