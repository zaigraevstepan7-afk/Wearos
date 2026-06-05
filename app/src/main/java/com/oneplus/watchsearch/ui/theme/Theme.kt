package com.oneplus.watchsearch.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val WearColors = Colors(
    primary = Color(0xFF4285F4),       // Google blue
    primaryVariant = Color(0xFF1A73E8),
    secondary = Color(0xFF34A853),     // Google green
    secondaryVariant = Color(0xFF188038),
    error = Color(0xFFEA4335),         // Google red
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    onError = Color(0xFF000000)
)

@Composable
fun WatchSearchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        content = content
    )
}
