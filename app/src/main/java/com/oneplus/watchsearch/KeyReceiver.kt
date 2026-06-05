package com.oneplus.watchsearch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.oneplus.watchsearch.data.AiProvider
import com.oneplus.watchsearch.data.Settings

/**
 * Convenience for setting the API key from a computer (typing a long key on a
 * watch is painful), e.g.:
 *
 *   adb shell am broadcast -n com.oneplus.watchsearch/.KeyReceiver \
 *       --es provider GEMINI --es key "YOUR_KEY"
 */
class KeyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings = Settings(context)
        intent.getStringExtra("provider")?.let {
            settings.provider = AiProvider.fromName(it.uppercase())
        }
        intent.getStringExtra("key")?.let { key ->
            settings.setApiKey(settings.provider, key)
        }
        Toast.makeText(context, "Ключ сохранён (${settings.provider.displayName})", Toast.LENGTH_SHORT).show()
    }
}
