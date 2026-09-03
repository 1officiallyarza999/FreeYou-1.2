package com.freeyou.ai

interface LLMProvider {
    suspend fun generateReply(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>, // Pair<Role, Text> (e.g. "user" to text)
        userText: String
    ): String
}

interface TTSProvider {
    fun speak(text: String, mode: String)
    fun stop()
    fun shutdown()
}

interface STTProvider {
    fun startListening(onResult: (String?) -> Unit, onStatus: (Boolean) -> Unit)
    fun stopListening()
    fun destroy()
}
