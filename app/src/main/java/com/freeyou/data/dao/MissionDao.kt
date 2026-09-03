package com.freeyou.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.freeyou.data.model.Mission
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions ORDER BY date DESC")
    fun getAllMissions(): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getMissionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Mission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMission(mission: Mission): Long
    
    @Update
    fun updateMission(mission: Mission): Int
}
