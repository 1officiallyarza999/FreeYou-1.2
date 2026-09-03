package com.freeyou.ai

import com.freeyou.data.MentorLines

class OfflineMentorEngine(
    private val reasons: List<String>,
    private val daysClean: Int
) : LLMProvider {
    
    override suspend fun generateReply(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>,
        userText: String
    ): String {
        // Find the current mode from system prompt or default to coach
        val mode = when {
            systemPrompt.contains("לוחם") || systemPrompt.contains("warrior") -> "warrior"
            systemPrompt.contains("חמלה") || systemPrompt.contains("compassion") -> "compassion"
            else -> "coach"
        }
        
        // Use the existing fallback logic from MentorLines
        return MentorLines.getLocalMentorReply(userText, reasons, daysClean, mode)
    }
}
