package com.oneplus.watchsearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.oneplus.watchsearch.data.AiProvider
import com.oneplus.watchsearch.data.Settings

@Composable
fun SettingsScreen(
    settings: Settings,
    onSaved: () -> Unit
) {
    var provider by remember { mutableStateOf(settings.provider) }
    var key by remember(provider) { mutableStateOf(settings.apiKey(provider)) }

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(
                RotaryScrollableDefaults.behavior(scrollableState = listState),
                focusRequester = focusRequester
            ),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                text = "Нейросеть",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center
            )
        }

        AiProvider.entries.forEach { p ->
            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = provider == p,
                    onCheckedChange = {
                        provider = p
                        key = settings.apiKey(p)
                    },
                    label = { Text(p.displayName) },
                    toggleControl = {
                        androidx.wear.compose.material.Icon(
                            imageVector = ToggleChipDefaults.radioIcon(provider == p),
                            contentDescription = null
                        )
                    },
                    colors = ToggleChipDefaults.toggleChipColors()
                )
            }
        }

        item {
            Text(
                text = "API-ключ для ${provider.displayName}",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item { ApiKeyField(value = key, onValueChange = { key = it }) }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                enabled = key.isNotBlank(),
                onClick = {
                    settings.provider = provider
                    settings.setApiKey(provider, key)
                    onSaved()
                },
                label = { Text("Сохранить") },
                colors = ChipDefaults.primaryChipColors()
            )
        }
    }
}

@Composable
private fun ApiKeyField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = "Вставьте ключ",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.body2
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.body2.copy(
                color = MaterialTheme.colors.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
