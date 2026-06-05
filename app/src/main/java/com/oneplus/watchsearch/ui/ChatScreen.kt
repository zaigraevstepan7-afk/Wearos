package com.oneplus.watchsearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.oneplus.watchsearch.data.AiClient
import com.oneplus.watchsearch.data.ChatMessage
import com.oneplus.watchsearch.data.Settings
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    settings: Settings,
    onOpenSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var sending by remember { mutableStateOf(false) }

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    val voice = rememberSearchInput(hint = "Спросите ИИ", onQuery = { q ->
        send(q, messages, scope, settings, listState) { sending = it }
    })

    fun submit(text: String) {
        send(text, messages, scope, settings, listState) { sending = it }
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .rotaryScrollable(
                RotaryScrollableDefaults.behavior(scrollableState = listState),
                focusRequester = focusRequester
            ),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            CompactChip(
                onClick = onOpenSettings,
                label = { Text(settings.provider.displayName) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        modifier = Modifier.size(ChipDefaults.SmallIconSize)
                    )
                },
                colors = ChipDefaults.secondaryChipColors()
            )
        }

        if (messages.isEmpty()) {
            item {
                Text(
                    text = "Задайте вопрос нейросети",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        items(messages.size) { index ->
            MessageBubble(messages[index])
        }

        if (sending) {
            item {
                Text(
                    text = "печатает…",
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        item { InputField(onSubmit = ::submit) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { voice.launchVoice() },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardVoice,
                        contentDescription = "Голос",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item { Spacer(Modifier.size(8.dp)) }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatMessage.Role.USER
    val bg = if (isUser) MaterialTheme.colors.primary else MaterialTheme.colors.surface
    val fg = if (isUser) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = message.text, color = fg, style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
private fun InputField(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    fun fire() {
        val t = text.trim()
        if (t.isNotEmpty()) {
            onSubmit(t)
            text = ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, RoundedCornerShape(20.dp))
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.78f), contentAlignment = Alignment.CenterStart) {
            if (text.isEmpty()) {
                Text(
                    text = "Сообщение…",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.body2
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { fire() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.size(4.dp))
        Button(
            onClick = ::fire,
            modifier = Modifier.size(36.dp),
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Отправить",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun send(
    text: String,
    messages: androidx.compose.runtime.snapshots.SnapshotStateList<ChatMessage>,
    scope: kotlinx.coroutines.CoroutineScope,
    settings: Settings,
    listState: androidx.wear.compose.foundation.lazy.ScalingLazyListState,
    setSending: (Boolean) -> Unit
) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return

    messages.add(ChatMessage(ChatMessage.Role.USER, trimmed))
    setSending(true)

    scope.launch {
        runCatching { listState.animateScrollToItem(messages.size) }

        val provider = settings.provider
        val key = settings.apiKey(provider)
        val reply = try {
            AiClient.complete(provider, key, messages.toList())
        } catch (e: Exception) {
            "⚠️ ${e.message ?: "Ошибка запроса"}"
        }

        messages.add(ChatMessage(ChatMessage.Role.ASSISTANT, reply))
        setSending(false)
        runCatching { listState.animateScrollToItem(messages.size) }
    }
}
