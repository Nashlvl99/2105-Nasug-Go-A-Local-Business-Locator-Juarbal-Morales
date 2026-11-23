package com.nasuggo.app.data

object BarangayNames {

    fun sortKey(barangay: String): Int =
        barangay.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
    //keeps the digits only to sort
}
