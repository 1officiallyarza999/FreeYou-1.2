package com.freeyou.data

import android.content.Context
import com.freeyou.data.database.AppDatabase
import com.freeyou.data.model.JournalEntry
import com.freeyou.data.model.Mission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class DataRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val journalDao = database.journalDao()
    private val missionDao = database.missionDao()

    // Journal
    val allJournalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun insertJournalEntry(entry: JournalEntry) {
        withContext(Dispatchers.IO) {
            journalDao.insertEntry(entry)
        }
    }

    suspend fun updateJournalEntry(entry: JournalEntry) {
        withContext(Dispatchers.IO) {
            journalDao.updateEntry(entry)
        }
    }

    // Missions
    fun getMissionsForToday(): Flow<List<Mission>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis - 1
        
        return missionDao.getMissionsForDay(startOfDay, endOfDay)
    }
    
    suspend fun insertMission(mission: Mission) {
        withContext(Dispatchers.IO) {
            missionDao.insertMission(mission)
        }
    }
    
    suspend fun updateMission(mission: Mission) {
        withContext(Dispatchers.IO) {
            missionDao.updateMission(mission)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DataRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
