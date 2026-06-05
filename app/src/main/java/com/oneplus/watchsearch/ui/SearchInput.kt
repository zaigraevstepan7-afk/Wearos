package com.oneplus.watchsearch.ui

import android.app.Activity
import android.app.RemoteInput
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.wear.input.RemoteInputIntentHelper

private const val REMOTE_INPUT_KEY = "watch_search_query"

/**
 * Holds the two ways a query can be entered on the watch: voice recognition
 * and the system text-input (keyboard / handwriting / voice) flow.
 */
class SearchInputController(
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
        voiceLauncher.launch(intent)
    }

    fun launchKeyboard() {
        val remoteInputs = listOf(
            RemoteInput.Builder(REMOTE_INPUT_KEY)
                .setLabel(hint)
                .build()
        )
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
        keyboardLauncher.launch(intent)
    }
}

/**
 * Wires up the activity-result launchers for voice and keyboard input and
 * invokes [onQuery] with the resulting text. Returns a controller used to
 * trigger each input method from the UI.
 */
@Composable
fun rememberSearchInput(hint: String, onQuery: (String) -> Unit): SearchInputController {
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
        SearchInputController(voiceLauncher, keyboardLauncher, hint)
    }
}
