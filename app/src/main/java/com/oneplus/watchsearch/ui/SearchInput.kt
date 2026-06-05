package com.oneplus.watchsearch.ui

import android.app.Activity
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.input.RemoteInputIntentHelper

private const val REMOTE_INPUT_KEY = "watch_search_query"

/**
 * Holds the two system-driven ways a query can be entered: voice recognition
 * and the Wear OS remote text-input flow. Both are launched defensively: on
 * devices without a handler (e.g. a phone, or a watch without the speech
 * service) we show a short message instead of letting the app crash.
 */
class SearchInputController(
    private val context: Context,
    private val voiceLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val keyboardLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val hint: String
) {
    fun launchVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, hint)
        }
        safeLaunch(intent, voiceLauncher, "Голосовой ввод недоступен")
    }

    fun launchKeyboard() {
        val remoteInputs = listOf(
            RemoteInput.Builder(REMOTE_INPUT_KEY)
                .setLabel(hint)
                .build()
        )
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
        safeLaunch(intent, keyboardLauncher, "Системный ввод недоступен")
    }

    private fun safeLaunch(
        intent: Intent,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        failureMessage: String
    ) {
        try {
            launcher.launch(intent)
        } catch (e: Exception) {
            // No activity can handle the intent (common on phones / stripped-down
            // watches). Fall back to the in-app text field instead of crashing.
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Wires up the activity-result launchers for voice and keyboard input and
 * invokes [onQuery] with the resulting text. Returns a controller used to
 * trigger each input method from the UI.
 */
@Composable
fun rememberSearchInput(hint: String, onQuery: (String) -> Unit): SearchInputController {
    val context = LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (spoken.isNotEmpty()) onQuery(spoken)
        }
    }

    val keyboardLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val typed = RemoteInput.getResultsFromIntent(data)
            ?.getCharSequence(REMOTE_INPUT_KEY)
            ?.toString()
            ?.trim()
            .orEmpty()
        if (typed.isNotEmpty()) onQuery(typed)
    }

    return remember(hint) {
        SearchInputController(context, voiceLauncher, keyboardLauncher, hint)
    }
}
