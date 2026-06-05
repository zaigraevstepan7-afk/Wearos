package com.oneplus.watchsearch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.oneplus.watchsearch.data.QueryOpener
import com.oneplus.watchsearch.data.SearchHistory

@Composable
fun SearchApp() {
    val context = LocalContext.current
    val history = remember { SearchHistory(context) }
    val opener = remember { QueryOpener(context) }
    var recent by remember { mutableStateOf(history.recent()) }

    Scaffold(
        timeText = { TimeText() }
    ) {
        SearchScreen(
            recentSearches = recent,
            onSearch = { query ->
                history.add(query)
                recent = history.recent()
                opener.open(query)
            },
            onClearHistory = {
                history.clear()
                recent = emptyList()
            }
        )
    }
}
