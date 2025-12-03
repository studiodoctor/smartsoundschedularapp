package com.android.smartsoundscheduler.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.android.smartsoundscheduler.MainActivity
import com.android.smartsoundscheduler.R
import com.android.smartsoundscheduler.data.SoundMode
import com.android.smartsoundscheduler.data.TimeSlotRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.jvm.java

@AndroidEntryPoint
class SoundSchedulerService : Service() {

    @Inject
    lateinit var repository: TimeSlotRepository

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var schedulerJob: Job? = null

    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var alarmManager: AlarmManager

    companion object {
        const val ACTION_RESCHEDULE = "com.android.smartsoundscheduler.RESCHEDULE"
        const val ACTION_CHANGE_MODE = "com.android.smartsoundscheduler.CHANGE_MODE"
        const val EXTRA_SOUND_MODE = "sound_mode"
        const val EXTRA_VIBRATION = "vibration"
        const val EXTRA_SLOT_NAME = "slot_name"

        private const val NOTIFICATION_CHANNEL_ID = "sound_scheduler_channel"
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_INTERVAL_MS = 60000L // 1 minute
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Sound Scheduler Active"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RESCHEDULE -> {
                rescheduleAllAlarms()
            }
            ACTION_CHANGE_MODE -> {
                val mode = intent.getStringExtra(EXTRA_SOUND_MODE)?.let {
                    SoundMode.valueOf(it)
                } ?: SoundMode.RING
                val vibration = intent.getBooleanExtra(EXTRA_VIBRATION, true)
                val slotName = intent.getStringExtra(EXTRA_SLOT_NAME) ?: "Schedule"

                changeSoundMode(mode, vibration, slotName)
            }
            else -> {
                startScheduler()
            }
        }

        return START_STICKY
    }

    private fun startScheduler() {
        schedulerJob?.cancel()
        schedulerJob = serviceScope.launch {
            while (isActive) {
                checkAndApplySchedule()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private fun checkAndApplySchedule() {
        serviceScope.launch {
            try {
                val slots = repository.getEnabledTimeSlots().first()
                val now = LocalDateTime.now()
                val currentTime = now.toLocalTime()
                val currentDay = now.dayOfWeek

                var activeSlot: com.android.smartsoundscheduler.data.TimeSlot? = null

                for (slot in slots) {
                    if (slot.activeDays.contains(currentDay)) {
                        if (isTimeInRange(currentTime, slot.startTime, slot.endTime)) {
                            activeSlot = slot
                            break
                        }
                    }
                }

                activeSlot?.let { slot ->
                    changeSoundMode(slot.soundMode, slot.vibrationEnabled, slot.name)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isTimeInRange(current: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (end < start) {
            // Overnight schedule
            current >= start || current < end
        } else {
            current >= start && current < end
        }
    }

    private fun changeSoundMode(mode: SoundMode, vibrationEnabled: Boolean, slotName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    return
                }
            }

            val ringerMode = when (mode) {
                SoundMode.RING -> AudioManager.RINGER_MODE_NORMAL
                SoundMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                SoundMode.SILENT -> {
                    if (vibrationEnabled) AudioManager.RINGER_MODE_VIBRATE
                    else AudioManager.RINGER_MODE_SILENT
                }
            }

            audioManager.ringerMode = ringerMode

            // Update notification
            val modeText = when (mode) {
                SoundMode.RING -> "Ring Mode"
                SoundMode.SILENT -> "Silent Mode"
                SoundMode.VIBRATE -> "Vibrate Mode"
            }

            val notification = createNotification("$slotName: $modeText active")
            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rescheduleAllAlarms() {
        serviceScope.launch {
            try {
                val slots = repository.getEnabledTimeSlots().first()
                val now = LocalDateTime.now()

                slots.forEach { slot ->
                    slot.activeDays.forEach { day ->
                        scheduleAlarm(slot, day, slot.startTime, true)
                        scheduleAlarm(slot, day, slot.endTime, false)
                    }
                }

                // Also check current schedule
                checkAndApplySchedule()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleAlarm(
        slot: com.android.smartsoundscheduler.data.TimeSlot,
        day: DayOfWeek,
        time: LocalTime,
        isStart: Boolean
    ) {
        val now = LocalDateTime.now()
        var targetDateTime = now.with(day).with(time)

        if (targetDateTime.isBefore(now)) {
            targetDateTime = targetDateTime.plusWeeks(1)
        }

        val intent = Intent(this, SoundSchedulerService::class.java).apply {
            action = ACTION_CHANGE_MODE
            putExtra(EXTRA_SOUND_MODE, if (isStart) slot.soundMode.name else SoundMode.RING.name)
            putExtra(EXTRA_VIBRATION, slot.vibrationEnabled)
            putExtra(EXTRA_SLOT_NAME, slot.name)
        }

        val requestCode = (slot.id * 100 + day.ordinal * 10 + if (isStart) 1 else 0).toInt()

        val pendingIntent = PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = now.until(targetDateTime, ChronoUnit.MILLIS) + System.currentTimeMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sound Scheduler",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for Sound Scheduler service"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sound Scheduler")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        schedulerJob?.cancel()
        serviceScope.cancel()
    }
}