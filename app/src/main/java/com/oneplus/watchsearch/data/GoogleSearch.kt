package com.oneplus.watchsearch.data

import android.net.Uri

/** Builds Google search URLs for a given query. */
object GoogleSearch {

    private const val BASE = "https://www.google.com/search"

    fun url(query: String): String {
        val q = Uri.encode(query.trim())
        return "$BASE?q=$q"
    }
}
