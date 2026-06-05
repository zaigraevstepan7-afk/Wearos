package com.oneplus.watchsearch.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.oneplus.watchsearch.data.SearchEngine
import com.oneplus.watchsearch.data.SearchResult

private sealed interface ResultsState {
    data object Loading : ResultsState
    data class Success(val results: List<SearchResult>) : ResultsState
    data class Error(val message: String) : ResultsState
}

/**
 * Fetches results for [query] over the network and renders them as a native,
 * rotary-scrollable list directly on the watch. No WebView is involved.
 */
@Composable
fun ResultsScreen(
    query: String,
    onExit: () -> Unit
) {
    var state by remember { mutableStateOf<ResultsState>(ResultsState.Loading) }

    LaunchedEffect(query) {
        state = ResultsState.Loading
        state = try {
            ResultsState.Success(SearchEngine.search(query))
        } catch (e: Exception) {
            ResultsState.Error("Нет подключения к интернету")
        }
    }

    BackHandler(enabled = true) { onExit() }

    when (val s = state) {
        is ResultsState.Loading -> Centered { CircularProgressIndicator() }
        is ResultsState.Error -> Centered {
            Text(
                text = s.message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        is ResultsState.Success ->
            if (s.results.isEmpty()) {
                Centered {
                    Text(
                        text = "Ничего не найдено по запросу\n«$query»",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                ResultsList(query = query, results = s.results)
            }
    }
}

@Composable
private fun ResultsList(query: String, results: List<SearchResult>) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(
                RotaryScrollableDefaults.behavior(scrollableState = listState),
                focusRequester = focusRequester
            ),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { ListHeader { Text(query, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

        items(results) { result ->
            TitleCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { openOnPhone(context, result.url) },
                title = {
                    Text(
                        text = result.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            ) {
                if (result.snippet.isNotBlank()) {
                    Text(
                        text = result.snippet,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.caption2
                    )
                }
                if (result.domain.isNotBlank()) {
                    Text(
                        text = result.domain,
                        color = MaterialTheme.colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.caption2
                    )
                }
            }
        }
    }
}

/** Best-effort: try to open the full page on the paired phone; never crash. */
private fun openOnPhone(context: android.content.Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        RemoteActivityHelper(context).startRemoteActivity(intent)
        Toast.makeText(context, "Открываю на телефоне", Toast.LENGTH_SHORT).show()
    } catch (e: Throwable) {
        Toast.makeText(context, "Не удалось открыть на телефоне", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
