package com.freeyou.ai

import android.content.Context
import com.freeyou.data.BlockRepo
import com.freeyou.data.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(val isMentor: Boolean, val text: String, val role: String)

class MentorManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private var ttsProvider: TTSProvider? = null
    private var sttProvider: STTProvider? = null
    
    private val onlineLlm = GeminiLLMProvider()
    
    var speechEnabled = true
    var activeMode = "coach"
        set(value) {
            field = value
            addWelcomeMessage()
        }

    init {
        ttsProvider = AndroidTTSProvider(context)
        sttProvider = AndroidSTTProvider(context)
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        val welcomeLine = when (activeMode) {
            "warrior" -> "אני היצר הטוב שלך. בלי תירוצים ובלי בריחות. אנחנו כאן כדי לנצח ולבנות גבר חזק. דבר אליי."
            "compassion" -> "שלום אחי. שום דבר שאתה עובר כאן אינו מביך או חריג. המרחב הזה בטוח, ואני כאן כדי לתמוך בך בכל רגע."
            else -> "שלום אלוף. אני כאן כדי לחדד אותך, להזכיר לך את היעדים שלך ולפרק כל דחף או מכשול בדרך. איך אני יכול לעזור עכשיו?"
        }
        _messages.value = listOf(ChatMessage(true, welcomeLine, "mentor"))
        speak(welcomeLine)
    }

    fun speak(text: String) {
        if (speechEnabled) {
            ttsProvider?.speak(text, activeMode)
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        
        val currentMsgs = _messages.value.toMutableList()
        currentMsgs.add(ChatMessage(false, userText, "user"))
        _messages.value = currentMsgs
        
        _isProcessing.value = true
        
        val history = currentMsgs.map { Pair(it.role, it.text) }
        
        scope.launch {
            val replyText = withContext(Dispatchers.IO) {
                // Determine whether to use online or offline engine
                val isOnline = NetworkUtils.isInternetAvailable(context)
                
                val provider: LLMProvider = if (isOnline) {
                    onlineLlm
                } else {
                    val reasons = BlockRepo.state.value.reasons
                    val days = BlockRepo.daysClean()
                    OfflineMentorEngine(reasons, days)
                }
                
                val systemPrompt = buildSystemPrompt()
                provider.generateReply(systemPrompt, history, userText)
            }
            
            val newMsgs = _messages.value.toMutableList()
            newMsgs.add(ChatMessage(true, replyText, "mentor"))
            _messages.value = newMsgs
            _isProcessing.value = false
            
            speak(replyText)
        }
    }

    private fun buildSystemPrompt(): String {
        val days = BlockRepo.daysClean()
        val reasons = BlockRepo.state.value.reasons
        val reasonsStr = if (reasons.isNotEmpty()) "סיבות מרכזיות של המשתמש: ${reasons.joinToString(", ")}" else ""
        
        val modeInstructions = when (activeMode) {
            "warrior" -> "התנהג כלוחם: חד, קצר, אנרגטי, לא משפיל. תן פקודות פיזיות כדי לעורר אותו."
            "compassion" -> "התנהג בחמלה: רגוע, תומך, ללא ביקורת, מעודד נשימות ומנוחה."
            else -> "התנהג כמאמן (Coach): פרקטי, ממוקד, ניתוח המצב, מציע אלטרנטיבות בריאות."
        }
        
        return """
            אתה "היצר הטוב שלו", העוזר האישי של המשתמש באפליקציית FreeYou נגד התמכרויות והרגלים רעים (בעיקר צפייה בפורנו וגלילה אינסופית).
            מצב נוכחי: המשתמש נקי $days ימים.
            $reasonsStr
            
            כללי התנהגות קריטיים:
            1. $modeInstructions
            2. המסר שלך קצר, מדויק וברור. עד 3 משפטים קצרים בכל תגובה.
            3. ענה בעברית בלבד. בלי פסקאות ארוכות מדי כי זה הולך להיות מוקרא ב-Text to Speech.
            4. אל תהיה מטיף דת, אל תשפוט אותו, אל תשפיל אותו. עזור לו לקבל החלטה נכונה ברגע של דחף.
            5. עודד אותו לעשות פעולה עכשיו: שכיבות סמיכה, כוס מים, לצאת החוצה, לנשום עמוק.
        """.trimIndent()
    }

    fun startListening() {
        sttProvider?.startListening(
            onResult = { text ->
                if (!text.isNullOrBlank()) {
                    sendMessage(text)
                }
            },
            onStatus = { active ->
                _isListening.value = active
            }
        )
    }

    fun stopListening() {
        sttProvider?.stopListening()
    }

    fun destroy() {
        ttsProvider?.shutdown()
        sttProvider?.destroy()
    }
}
