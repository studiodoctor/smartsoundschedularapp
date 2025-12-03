package com.android.smartsoundscheduler.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(tableName = "time_slots")
data class TimeSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val soundMode: SoundMode,
    val activeDays: Set<DayOfWeek>,
    val isEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val color: Long = 0xFF6750A4,
    val createdAt: Long = System.currentTimeMillis()
)

enum class SoundMode {
    RING, SILENT, VIBRATE
}

data class TimeSlotConflict(
    val existingSlot: TimeSlot,
    val conflictingDays: Set<DayOfWeek>
)