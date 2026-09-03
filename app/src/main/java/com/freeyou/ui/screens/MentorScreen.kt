package com.freeyou.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.data.BlockRepo
import com.freeyou.data.MentorLines
import com.freeyou.ui.components.AudioWaveformVisual
import com.freeyou.ui.theme.AppColors
import java.util.Locale

data class ChatMsg(val isMentor: Boolean, val text: String)

@Composable
fun MentorScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by BlockRepo.state.collectAsState()
    val days = BlockRepo.daysClean()

    var activeMode by remember { mutableStateOf(state.mentorMode) }

    val welcomeLine = remember(activeMode) {
        when (activeMode) {
            "warrior" -> "אני היצר הטוב שלך. בלי תירוצים ובלי בריחות. אנחנו כאן כדי לנצח ולבנות גבר חזק. דבר אליי."
            "compassion" -> "שלום אחי. שום דבר שאתה עובר כאן אינו מביך או חריג. המרחב הזה בטוח, ואני כאן כדי לתמוך בך בכל רגע."
            else -> "שלום אלוף. אני כאן כדי לחדד אותך, להזכיר לך את היעדים שלך ולפרק כל דחף או מכשול בדרך. איך אני יכול לעזור עכשיו?"
        }
    }

    var messages by remember {
        mutableStateOf(listOf(ChatMsg(isMentor = true, text = welcomeLine)))
    }
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var speechEnabled by remember { mutableStateOf(true) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(activeMode) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("he", "IL")
                when (activeMode) {
                    "warrior" -> {
                        tts?.setSpeechRate(1.05f)
                        tts?.setPitch(0.85f)
                    }
                    "compassion" -> {
                        tts?.setSpeechRate(0.90f)
                        tts?.setPitch(1.0f)
                    }
                    else -> {
                        tts?.setSpeechRate(0.95f)
                        tts?.setPitch(0.92f)
                    }
                }
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    fun speak(text: String) {
        if (speechEnabled) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mentor_reply")
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val newMsgs = messages.toMutableList()
        newMsgs.add(ChatMsg(isMentor = false, text = userText))
        val reply = MentorLines.getLocalMentorReply(userText, state.reasons, days, activeMode)
        newMsgs.add(ChatMsg(isMentor = true, text = reply))
        messages = newMsgs
        inputText = ""
        speak(reply)
    }

    // Voice recognition setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(Unit) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val sr = SpeechRecognizer.createSpeechRecognizer(context)
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val heard = matches?.firstOrNull()
                    if (!heard.isNullOrBlank()) {
                        sendMessage(heard)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer = sr
        }
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "דבר עם היצר הטוב שלך...")
            }
            try {
                isListening = true
                speechRecognizer?.startListening(intent)
            } catch (_: Exception) {
                isListening = false
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "היצר הטוב שלך",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = if (isListening) "מקשיב לקולך כעת..." else "קול פנימי חכם, רגוע וחזק",
                    fontSize = 12.sp,
                    color = if (isListening) AppColors.Amber else AppColors.Cyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (speechEnabled) "🔊 קול" else "🔇 שקט",
                    fontSize = 12.sp,
                    color = AppColors.TextTertiary,
                    modifier = Modifier
                        .clickable { speechEnabled = !speechEnabled }
                        .padding(4.dp)
                )
            }
        }

        // Personality Mode Selector (Compassion, Coach, Warrior)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.CardSurface)
                .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(14.dp))
                .padding(3.dp)
        ) {
            val modes = listOf(
                Triple("coach", "🎯 מאמן", AppColors.Violet),
                Triple("warrior", "⚔️ לוחם", AppColors.AmberGlow),
                Triple("compassion", "🕊️ חמלה", AppColors.Cyan)
            )

            modes.forEach { (modeId, label, color) ->
                val isSelected = activeMode == modeId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) color else Color.Transparent)
                        .clickable {
                            activeMode = modeId
                            BlockRepo.setMentorMode(modeId)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) (if (modeId == "warrior") Color.Black else Color.White) else AppColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AudioWaveformVisual(isListening = isListening)

        // Chat messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg, mode = activeMode)
            }
        }

        // Quick Contextual Prompt Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SuggestionChip(
                label = "עובר עליי דחף עכשיו",
                onClick = { sendMessage("עובר עליי דחף חזק עכשיו") }
            )
            SuggestionChip(
                label = "למה התחלתי?",
                onClick = { sendMessage("תזכיר לי למה התחלתי את המסע הזה") }
            )
            SuggestionChip(
                label = "עייפות ולחץ",
                onClick = { sendMessage("אני מרגיש עייף ולחוץ מאוד") }
            )
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("דבר או כתוב ליצר הטוב שלך...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Violet,
                    unfocusedBorderColor = AppColors.BorderGlass
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListening) AppColors.Amber else AppColors.CardElevated)
                    .border(
                        1.dp,
                        if (isListening) AppColors.Amber else AppColors.BorderGlass,
                        CircleShape
                    )
                    .clickable {
                        if (isListening) {
                            speechRecognizer?.stopListening()
                            isListening = false
                        } else {
                            recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙️", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.Violet)
                    .clickable { sendMessage(inputText) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "➤", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMsg, mode: String) {
    val align = if (msg.isMentor) Alignment.Start else Alignment.End
    val bg = if (msg.isMentor) {
        when (mode) {
            "warrior" -> AppColors.CardSurface.copy(alpha = 0.95f)
            "compassion" -> AppColors.CardSurface.copy(alpha = 0.95f)
            else -> AppColors.CardSurface.copy(alpha = 0.95f)
        }
    } else {
        AppColors.Violet.copy(alpha = 0.35f)
    }

    val border = if (msg.isMentor) {
        when (mode) {
            "warrior" -> AppColors.Amber.copy(alpha = 0.3f)
            "compassion" -> AppColors.Cyan.copy(alpha = 0.3f)
            else -> AppColors.Violet.copy(alpha = 0.3f)
        }
    } else {
        AppColors.Violet.copy(alpha = 0.6f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (msg.isMentor) 4.dp else 18.dp,
                        bottomEnd = if (msg.isMentor) 18.dp else 4.dp
                    )
                )
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Text(
                text = msg.text,
                color = AppColors.TextPrimary,
                fontSize = 14.5.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.CardSurface)
            .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = AppColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
