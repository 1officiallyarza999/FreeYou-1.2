package com.freeyou.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // BUSINESS, BODY, MIND, FAMILY, RELATIONSHIPS, PURPOSE
    val title: String,
    val isCompleted: Boolean = false,
    val date: Long = System.currentTimeMillis() // Day it belongs to
)
