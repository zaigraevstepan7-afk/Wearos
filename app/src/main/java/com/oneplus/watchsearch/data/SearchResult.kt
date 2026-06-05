package com.oneplus.watchsearch.data

/** A single search result rendered natively on the watch. */
data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String
) {
    val domain: String
        get() = url
            .substringAfter("://", url)
            .substringBefore('/')
            .removePrefix("www.")
}
