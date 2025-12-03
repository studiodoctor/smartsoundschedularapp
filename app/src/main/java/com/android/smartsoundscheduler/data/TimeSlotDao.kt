package com.android.smartsoundscheduler.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {
    @Query("SELECT * FROM time_slots ORDER BY startTime ASC")
    fun getAllTimeSlots(): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE isEnabled = 1")
    fun getEnabledTimeSlots(): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE id = :id")
    suspend fun getTimeSlotById(id: Long): TimeSlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlot): Long

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlot)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlot)

    @Query("DELETE FROM time_slots WHERE id = :id")
    suspend fun deleteTimeSlotById(id: Long)

    @Query("UPDATE time_slots SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, enabled: Boolean)
}