package com.nasuggo.app.data

data class BusinessDto(
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val address: String?,
    val phone: String?,
    val latitude: Double,
    val longitude: Double,
    val offerings: List<String>?,
    val keywords: List<String>?,
    val barangay: String?
)

data class BusinessFile(
    val businesses: List<BusinessDto> = emptyList()
)
