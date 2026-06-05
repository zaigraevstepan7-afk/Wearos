package com.oneplus.watchsearch.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.oneplus.watchsearch.data.SearchHistory

private object Routes {
    const val SEARCH = "search"
    const val RESULTS = "results/{query}"
    fun results(query: String) = "results/${Uri.encode(query)}"
}

@Composable
fun SearchApp() {
    val context = LocalContext.current
    val history = remember { SearchHistory(context) }
    var recent by remember { mutableStateOf(history.recent()) }

    val navController = rememberSwipeDismissableNavController()

    Scaffold(
        timeText = { TimeText() }
    ) {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.SEARCH
        ) {
            composable(Routes.SEARCH) {
                SearchScreen(
                    recentSearches = recent,
                    onSearch = { query ->
                        history.add(query)
                        recent = history.recent()
                        navController.navigate(Routes.results(query))
                    },
                    onClearHistory = {
                        history.clear()
                        recent = emptyList()
                    }
                )
            }
            composable(
                route = Routes.RESULTS,
                arguments = listOf(navArgument("query") { type = NavType.StringType })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query").orEmpty()
                ResultsScreen(
                    query = query,
                    onExit = { navController.popBackStack() }
                )
            }
        }
    }
}
