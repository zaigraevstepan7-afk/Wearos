package com.oneplus.watchsearch.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Fetches web search results over plain HTTP and returns them as data, so they
 * can be rendered natively on the watch (which has no WebView/browser).
 *
 * Uses DuckDuckGo's no-JavaScript HTML endpoint, which returns a lightweight,
 * parseable results page without API keys or a cookie-consent wall — the only
 * approach that works on a device that cannot render web pages itself.
 */
object SearchEngine {

    private const val ENDPOINT = "https://html.duckduckgo.com/html/"
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val html = fetch(query)
        parse(html)
    }

    private fun fetch(query: String): String {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val url = URL("$ENDPOINT?q=$q&kl=ru-ru")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept-Language", "ru,en;q=0.9")
        }
        return try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private val LINK = Regex(
        "class=\"result__a\"[^>]*href=\"(.*?)\"[^>]*>(.*?)</a>",
        RegexOption.DOT_MATCHES_ALL
    )
    private val SNIPPET = Regex(
        "class=\"result__snippet\"[^>]*>(.*?)</a>",
        RegexOption.DOT_MATCHES_ALL
    )
    private val UDDG = Regex("uddg=([^&]+)")
    private val TAGS = Regex("<[^>]*>")

    private fun parse(html: String): List<SearchResult> {
        val links = LINK.findAll(html).toList()
        val snippets = SNIPPET.findAll(html).toList()

        return links.mapIndexedNotNull { i, match ->
            val title = clean(match.groupValues[2])
            if (title.isBlank()) return@mapIndexedNotNull null
            val url = resolveUrl(match.groupValues[1])
            val snippet = snippets.getOrNull(i)?.groupValues?.get(1)?.let(::clean).orEmpty()
            SearchResult(title = title, url = url, snippet = snippet)
        }
    }

    private fun resolveUrl(raw: String): String {
        val uddg = UDDG.find(raw)?.groupValues?.get(1)
        val target = if (uddg != null) {
            runCatching { URLDecoder.decode(uddg, "UTF-8") }.getOrDefault(raw)
        } else {
            raw
        }
        return if (target.startsWith("//")) "https:$target" else target
    }

    private fun clean(html: String): String =
        TAGS.replace(html, "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#x27;", "'")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
