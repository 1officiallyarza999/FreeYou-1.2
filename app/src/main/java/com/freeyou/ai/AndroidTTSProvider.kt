package com.freeyou.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AndroidTTSProvider(context: Context) : TTSProvider {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: Pair<String, String>? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("he", "IL")
                isInitialized = true
                pendingText?.let {
                    speak(it.first, it.second)
                    pendingText = null
                }
            }
        }
    }

    override fun speak(text: String, mode: String) {
        if (!isInitialized) {
            pendingText = Pair(text, mode)
            return
        }
        
        when (mode) {
            "warrior" -> {
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(0.85f)
            }
            "compassion" -> {
                tts?.setSpeechRate(0.90f)
                tts?.setPitch(1.0f)
            }
            else -> { // coach
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(0.92f)
            }
        }
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mentor_reply")
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
