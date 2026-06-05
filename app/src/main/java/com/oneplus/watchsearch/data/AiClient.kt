package com.oneplus.watchsearch.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends a conversation to the selected AI provider and returns the reply text.
 * All network work happens off the main thread. API errors are surfaced as
 * exceptions whose message is shown to the user (e.g. invalid key, bad model).
 */
object AiClient {

    suspend fun complete(
        provider: AiProvider,
        apiKey: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        when (provider) {
            AiProvider.GEMINI -> gemini(apiKey, history)
            AiProvider.OPENAI -> openAi(apiKey, history)
            AiProvider.CLAUDE -> claude(apiKey, history)
        }
    }

    // --- Gemini -------------------------------------------------------------

    private fun gemini(apiKey: String, history: List<ChatMessage>): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/" +
            "${AiProvider.GEMINI.model}:generateContent?key=$apiKey"
        val contents = JSONArray()
        history.forEach { m ->
            val role = if (m.role == ChatMessage.Role.USER) "user" else "model"
            contents.put(
                JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(JSONObject().put("text", m.text)))
            )
        }
        val body = JSONObject().put("contents", contents).toString()
        val (code, resp) = post(url, emptyMap(), body)
        if (code !in 200..299) throw RuntimeException(extractError(resp, code))

        return JSONObject(resp)
            .getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
            .getString("text").trim()
    }

    // --- OpenAI -------------------------------------------------------------

    private fun openAi(apiKey: String, history: List<ChatMessage>): String {
        val messages = JSONArray()
        history.forEach { m ->
            val role = if (m.role == ChatMessage.Role.USER) "user" else "assistant"
            messages.put(JSONObject().put("role", role).put("content", m.text))
        }
        val body = JSONObject()
            .put("model", AiProvider.OPENAI.model)
            .put("messages", messages)
            .toString()
        val (code, resp) = post(
            "https://api.openai.com/v1/chat/completions",
            mapOf("Authorization" to "Bearer $apiKey"),
            body
        )
        if (code !in 200..299) throw RuntimeException(extractError(resp, code))

        return JSONObject(resp)
            .getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").getString("content").trim()
    }

    // --- Claude -------------------------------------------------------------

    private fun claude(apiKey: String, history: List<ChatMessage>): String {
        val messages = JSONArray()
        history.forEach { m ->
            val role = if (m.role == ChatMessage.Role.USER) "user" else "assistant"
            messages.put(JSONObject().put("role", role).put("content", m.text))
        }
        val body = JSONObject()
            .put("model", AiProvider.CLAUDE.model)
            .put("max_tokens", 1024)
            .put("messages", messages)
            .toString()
        val (code, resp) = post(
            "https://api.anthropic.com/v1/messages",
            mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to "2023-06-01"
            ),
            body
        )
        if (code !in 200..299) throw RuntimeException(extractError(resp, code))

        return JSONObject(resp)
            .getJSONArray("content").getJSONObject(0)
            .getString("text").trim()
    }

    // --- HTTP ---------------------------------------------------------------

    private fun post(
        urlStr: String,
        headers: Map<String, String>,
        body: String
    ): Pair<Int, String> {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }
        return try {
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            code to text
        } finally {
            conn.disconnect()
        }
    }

    private fun extractError(resp: String, code: Int): String {
        return try {
            val obj = JSONObject(resp)
            val err = obj.opt("error")
            when (err) {
                is JSONObject -> err.optString("message", "Ошибка $code")
                is String -> err
                else -> obj.optString("message", "Ошибка $code")
            }
        } catch (e: Exception) {
            "Ошибка $code"
        }
    }
}
