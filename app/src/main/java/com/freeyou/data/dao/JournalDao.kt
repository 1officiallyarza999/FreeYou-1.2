package com.freeyou.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.freeyou.data.model.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEntry(entry: JournalEntry): Long
    
    @Update
    fun updateEntry(entry: JournalEntry): Int

    @Query("DELETE FROM journal_entries")
    fun clearAll(): Int
}
