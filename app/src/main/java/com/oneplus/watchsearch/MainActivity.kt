package com.oneplus.watchsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.oneplus.watchsearch.ui.ChatApp
import com.oneplus.watchsearch.ui.theme.WatchSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            WatchSearchTheme {
                ChatApp()
            }
        }
    }
}
