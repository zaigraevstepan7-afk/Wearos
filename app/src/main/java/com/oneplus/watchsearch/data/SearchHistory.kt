package com.oneplus.watchsearch.data

import android.content.Context
import androidx.core.content.edit

/**
 * Lightweight persistence of recent search queries using SharedPreferences.
 * Keeps the most recent [MAX_ITEMS] unique queries, newest first.
 */
class SearchHistory(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun recent(): List<String> {
        val raw = prefs.getString(KEY_ITEMS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    fun add(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val current = recent().toMutableList()
        // De-duplicate (case-insensitive) and move to the front.
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
        val limited = current.take(MAX_ITEMS)
        prefs.edit { putString(KEY_ITEMS, limited.joinToString(SEPARATOR)) }
    }

    fun clear() {
        prefs.edit { remove(KEY_ITEMS) }
    }

    companion object {
        private const val PREFS_NAME = "watch_search_history"
        private const val KEY_ITEMS = "items"
        private const val SEPARATOR = "" // unit separator, safe vs. query text
        private const val MAX_ITEMS = 10
    }
}
