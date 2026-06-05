package com.oneplus.watchsearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun SearchScreen(
    recentSearches: List<String>,
    onSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    listState: ScalingLazyListState = rememberScalingLazyListState()
) {
    val input = rememberSearchInput(
        hint = "Поиск Google",
        onQuery = onSearch
    )

    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { GoogleWordmark() }

        // Primary input: an in-app text field. Works on any device (phone or
        // watch) because it uses the system soft keyboard directly rather than
        // a Wear-only input intent.
        item {
            SearchField(onSubmit = onSearch)
        }

        // Voice search (best-effort; falls back to a toast if unavailable).
        item {
            Button(
                onClick = { input.launchVoice() },
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardVoice,
                    contentDescription = "Голосовой поиск",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (recentSearches.isNotEmpty()) {
            item { ListHeader { Text("Недавние") } }
            items(recentSearches) { query ->
                CompactChip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSearch(query) },
                    label = {
                        Text(
                            text = query,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                CompactChip(
                    onClick = onClearHistory,
                    label = { Text("Очистить") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(ChipDefaults.SmallIconSize)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SearchField(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    fun submit() {
        val q = text.trim()
        if (q.isNotEmpty()) {
            onSubmit(q)
            text = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primary, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                if (text.isEmpty()) {
                    Text(
                        text = "Введите запрос",
                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.body2
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onPrimary
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colors.onPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun GoogleWordmark() {
    val blue = Color(0xFF4285F4)
    val red = Color(0xFFEA4335)
    val yellow = Color(0xFFFBBC05)
    val green = Color(0xFF34A853)
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val style = MaterialTheme.typography.title2
        Text("G", color = blue, style = style)
        Text("o", color = red, style = style)
        Text("o", color = yellow, style = style)
        Text("g", color = blue, style = style)
        Text("l", color = green, style = style)
        Text("e", color = red, style = style)
    }
}
