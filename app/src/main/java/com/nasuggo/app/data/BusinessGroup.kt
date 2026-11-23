package com.nasuggo.app.data

enum class BusinessGroup(val label: String) {
    FOOD("Food & Beverages"),
    RETAIL("Retail"),
    SERVICES("Services");



    companion object {
        fun fromLabel(label: String?): BusinessGroup? = values().firstOrNull { it.label == label }
    }
}
