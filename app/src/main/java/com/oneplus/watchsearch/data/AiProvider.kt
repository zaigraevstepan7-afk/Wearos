package com.oneplus.watchsearch.data

/** Supported AI providers and their default models / display names. */
enum class AiProvider(val displayName: String, val model: String) {
    GEMINI("Gemini", "gemini-2.0-flash"),
    OPENAI("ChatGPT", "gpt-4o-mini"),
    CLAUDE("Claude", "claude-haiku-4-5-20251001");

    companion object {
        fun fromName(name: String?): AiProvider =
            entries.firstOrNull { it.name == name } ?: GEMINI
    }
}
