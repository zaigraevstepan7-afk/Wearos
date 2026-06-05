package com.oneplus.watchsearch.data

import android.net.Uri

/** Builds Google search URLs for a given query. */
object GoogleSearch {

    private const val BASE = "https://www.google.com/search"

    fun url(query: String): String {
        val q = Uri.encode(query.trim())
        // igu=1 returns a lightweight results page that renders inside a WebView
        // without the cookie-consent redirect, which suits the small watch screen.
        return "$BASE?q=$q&igu=1"
    }
}
