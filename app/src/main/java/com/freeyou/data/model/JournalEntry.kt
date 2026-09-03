package com.freeyou.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val trigger: String? = null,
    val emotion: String? = null,
    val intensity: Int = 0, // 1 to 10
    val notes: String? = null,
    val intervention: String? = null,
    val outcome: String? = null // "Success", "Relapse"
)
