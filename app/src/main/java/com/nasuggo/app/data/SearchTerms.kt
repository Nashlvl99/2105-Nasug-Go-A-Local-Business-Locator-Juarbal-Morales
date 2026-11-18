package com.nasuggo.app.data

/**
 * Composition: a Business HAS-A SearchTerms object instead of inheriting
 * its extra search words from a subclass.
 */
class SearchTerms(words: List<String> = emptyList()) {
    private val words: List<String> = words.map { it.lowercase() }

    fun contains(query: String): Boolean = words.any { it.contains(query) }

    companion object {
        val NONE = SearchTerms()
        val FOOD = SearchTerms(listOf("food", "eat", "kainan"))
        val RETAIL = SearchTerms(listOf("shop", "store", "tindahan", "bilihan"))
    }
}
