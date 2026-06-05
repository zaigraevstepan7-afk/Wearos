package com.oneplus.watchsearch.data

import android.content.Context
import androidx.core.content.edit

/** Stores the chosen provider and its API key (per provider). */
class Settings(context: Context) {

    private val prefs = context.getSharedPreferences("ai_chat_settings", Context.MODE_PRIVATE)

    var provider: AiProvider
        get() = AiProvider.fromName(prefs.getString(KEY_PROVIDER, null))
        set(value) = prefs.edit { putString(KEY_PROVIDER, value.name) }

    fun apiKey(provider: AiProvider): String =
        prefs.getString(keyFor(provider), "").orEmpty().trim()

    fun setApiKey(provider: AiProvider, key: String) =
        prefs.edit { putString(keyFor(provider), key.trim()) }

    /** True once the currently selected provider has a key configured. */
    fun isReady(): Boolean = apiKey(provider).isNotEmpty()

    private fun keyFor(provider: AiProvider) = "api_key_${provider.name}"

    companion object {
        private const val KEY_PROVIDER = "provider"
    }
}
