package com.oneplus.watchsearch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.oneplus.watchsearch.data.Settings

@Composable
fun ChatApp() {
    val context = LocalContext.current
    val settings = remember { Settings(context) }

    // Show settings first until a key is configured; reachable later via the
    // provider chip on the chat screen.
    var showSettings by rememberSaveable { mutableStateOf(!settings.isReady()) }

    Scaffold(timeText = { TimeText() }) {
        if (showSettings) {
            SettingsScreen(settings = settings, onSaved = { showSettings = false })
        } else {
            ChatScreen(settings = settings, onOpenSettings = { showSettings = true })
        }
    }
}
