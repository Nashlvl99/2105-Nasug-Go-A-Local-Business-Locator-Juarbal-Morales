package com.nasuggo.app.data

class SearchTerms(words: List<String> = emptyList()) {
    private val words: List<String> = words.map { it.lowercase() }

    fun contains(query: String): Boolean =
        query.length >= 3 && words.any { it.startsWith(query) }
    companion object {
        val NONE = SearchTerms()
        val FOOD = SearchTerms(listOf("food", "eat", "kainan"))
        val RETAIL = SearchTerms(listOf("shop", "store", "tindahan", "bilihan"))
    }
}
