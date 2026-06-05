package com.oneplus.watchsearch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.oneplus.watchsearch.data.SearchHistory

@Composable
fun SearchApp() {
    val context = LocalContext.current
    val history = remember { SearchHistory(context) }
    var recent by remember { mutableStateOf(history.recent()) }

    // Which screen is shown: null = search, non-null = results for that query.
    // A plain state swap (instead of a navigation graph) keeps the crash surface
    // minimal and avoids encoding the query into a route.
    var activeQuery by rememberSaveable { mutableStateOf<String?>(null) }

    val current = activeQuery
    if (current == null) {
        Scaffold(timeText = { TimeText() }) {
            SearchScreen(
                recentSearches = recent,
                onSearch = { query ->
                    history.add(query)
                    recent = history.recent()
                    activeQuery = query
                },
                onClearHistory = {
                    history.clear()
                    recent = emptyList()
                }
            )
        }
    } else {
        ResultsScreen(
            query = current,
            onExit = { activeQuery = null }
        )
    }
}
