package com.android.smartsoundscheduler.data

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

    @TypeConverter
    fun toLocalTime(timeString: String): LocalTime = LocalTime.parse(timeString)

    @TypeConverter
    fun fromDayOfWeekSet(days: Set<DayOfWeek>): String =
        days.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekSet(daysString: String): Set<DayOfWeek> =
        if (daysString.isEmpty()) emptySet()
        else daysString.split(",").map { DayOfWeek.valueOf(it) }.toSet()

    @TypeConverter
    fun fromSoundMode(mode: SoundMode): String = mode.name

    @TypeConverter
    fun toSoundMode(modeName: String): SoundMode = SoundMode.valueOf(modeName)
}