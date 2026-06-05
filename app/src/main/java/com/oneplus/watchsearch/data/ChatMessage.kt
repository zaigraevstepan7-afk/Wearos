package com.oneplus.watchsearch.data

/** One turn in the conversation. */
data class ChatMessage(
    val role: Role,
    val text: String
) {
    enum class Role { USER, ASSISTANT }
}
