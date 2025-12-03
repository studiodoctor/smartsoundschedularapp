package com.android.smartsoundscheduler.data

import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSlotRepository @Inject constructor(
    private val timeSlotDao: TimeSlotDao
) {
    fun getAllTimeSlots(): Flow<List<TimeSlot>> = timeSlotDao.getAllTimeSlots()

    fun getEnabledTimeSlots(): Flow<List<TimeSlot>> = timeSlotDao.getEnabledTimeSlots()

    suspend fun getTimeSlotById(id: Long): TimeSlot? = timeSlotDao.getTimeSlotById(id)

    suspend fun insertTimeSlot(timeSlot: TimeSlot): Long = timeSlotDao.insertTimeSlot(timeSlot)

    suspend fun updateTimeSlot(timeSlot: TimeSlot) = timeSlotDao.updateTimeSlot(timeSlot)

    suspend fun deleteTimeSlot(timeSlot: TimeSlot) = timeSlotDao.deleteTimeSlot(timeSlot)

    suspend fun toggleEnabled(id: Long, enabled: Boolean) =
        timeSlotDao.updateEnabled(id, enabled)

    suspend fun checkForConflicts(
        newSlot: TimeSlot,
        existingSlots: List<TimeSlot>,
        excludeId: Long? = null
    ): List<TimeSlotConflict> {
        val conflicts = mutableListOf<TimeSlotConflict>()

        existingSlots
            .filter { it.id != excludeId && it.isEnabled }
            .forEach { existing ->
                val overlappingDays = newSlot.activeDays.intersect(existing.activeDays)

                if (overlappingDays.isNotEmpty()) {
                    if (timesOverlap(newSlot.startTime, newSlot.endTime,
                            existing.startTime, existing.endTime)) {
                        conflicts.add(TimeSlotConflict(existing, overlappingDays))
                    }
                }
            }

        return conflicts
    }

    private fun timesOverlap(
        start1: LocalTime, end1: LocalTime,
        start2: LocalTime, end2: LocalTime
    ): Boolean {
        // Handle overnight slots
        val slot1CrossesMidnight = end1 < start1
        val slot2CrossesMidnight = end2 < start2

        return when {
            !slot1CrossesMidnight && !slot2CrossesMidnight -> {
                start1 < end2 && start2 < end1
            }
            slot1CrossesMidnight && !slot2CrossesMidnight -> {
                start2 < end1 || start1 < end2
            }
            !slot1CrossesMidnight && slot2CrossesMidnight -> {
                start1 < end2 || start2 < end1
            }
            else -> true // Both cross midnight
        }
    }
}