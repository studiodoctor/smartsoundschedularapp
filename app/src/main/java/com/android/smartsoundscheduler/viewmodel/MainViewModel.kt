package com.android.smartsoundscheduler.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.smartsoundscheduler.data.*
import com.android.smartsoundscheduler.service.SoundSchedulerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

data class MainUiState(
    val timeSlots: List<TimeSlot> = emptyList(),
    val isLoading: Boolean = true,
    val currentSoundMode: SoundMode = SoundMode.RING,
    val hasNotificationPolicyAccess: Boolean = false,
    val error: String? = null
)

data class AddEditUiState(
    val name: String = "",
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(17, 0),
    val soundMode: SoundMode = SoundMode.SILENT,
    val activeDays: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    ),
    val vibrationEnabled: Boolean = true,
    val selectedColor: Long = 0xFF6750A4,
    val isEditing: Boolean = false,
    val editingSlotId: Long? = null,
    val conflicts: List<TimeSlotConflict> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: TimeSlotRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _addEditState = MutableStateFlow(AddEditUiState())
    val addEditState: StateFlow<AddEditUiState> = _addEditState.asStateFlow()

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        loadTimeSlots()
        checkNotificationPolicyAccess()
        updateCurrentSoundMode()
    }

    private fun loadTimeSlots() {
        viewModelScope.launch {
            repository.getAllTimeSlots()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { slots ->
                    _uiState.update {
                        it.copy(timeSlots = slots, isLoading = false)
                    }
                }
        }
    }

    fun checkNotificationPolicyAccess(): Boolean {
        val hasAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else true

        _uiState.update { it.copy(hasNotificationPolicyAccess = hasAccess) }
        return hasAccess
    }

    fun requestNotificationPolicyAccess(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun updateCurrentSoundMode() {
        val mode = when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> SoundMode.RING
            AudioManager.RINGER_MODE_VIBRATE -> SoundMode.VIBRATE
            else -> SoundMode.SILENT
        }
        _uiState.update { it.copy(currentSoundMode = mode) }
    }

    // Add/Edit Operations
    fun initAddSlot() {
        _addEditState.value = AddEditUiState()
    }

    fun initEditSlot(slotId: Long) {
        viewModelScope.launch {
            repository.getTimeSlotById(slotId)?.let { slot ->
                _addEditState.value = AddEditUiState(
                    name = slot.name,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    soundMode = slot.soundMode,
                    activeDays = slot.activeDays,
                    vibrationEnabled = slot.vibrationEnabled,
                    selectedColor = slot.color,
                    isEditing = true,
                    editingSlotId = slot.id
                )
            }
        }
    }

    fun updateName(name: String) {
        _addEditState.update { it.copy(name = name, error = null) }
    }

    fun updateStartTime(time: LocalTime) {
        _addEditState.update { it.copy(startTime = time, error = null) }
        checkConflicts()
    }

    fun updateEndTime(time: LocalTime) {
        _addEditState.update { it.copy(endTime = time, error = null) }
        checkConflicts()
    }

    fun updateSoundMode(mode: SoundMode) {
        _addEditState.update { it.copy(soundMode = mode) }
    }

    fun toggleDay(day: DayOfWeek) {
        _addEditState.update { state ->
            val newDays = if (state.activeDays.contains(day)) {
                state.activeDays - day
            } else {
                state.activeDays + day
            }
            state.copy(activeDays = newDays, error = null)
        }
        checkConflicts()
    }

    fun updateVibration(enabled: Boolean) {
        _addEditState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun updateColor(color: Long) {
        _addEditState.update { it.copy(selectedColor = color) }
    }

    private fun checkConflicts() {
        viewModelScope.launch {
            val state = _addEditState.value
            val currentSlot = TimeSlot(
                id = state.editingSlotId ?: 0,
                name = state.name,
                startTime = state.startTime,
                endTime = state.endTime,
                soundMode = state.soundMode,
                activeDays = state.activeDays,
                vibrationEnabled = state.vibrationEnabled,
                color = state.selectedColor
            )

            val conflicts = repository.checkForConflicts(
                currentSlot,
                _uiState.value.timeSlots,
                state.editingSlotId
            )

            _addEditState.update { it.copy(conflicts = conflicts) }
        }
    }

    fun saveTimeSlot() {
        val state = _addEditState.value

        // Validation
        if (state.name.isBlank()) {
            _addEditState.update { it.copy(error = "Please enter a name") }
            return
        }

        if (state.activeDays.isEmpty()) {
            _addEditState.update { it.copy(error = "Please select at least one day") }
            return
        }

        if (state.conflicts.isNotEmpty()) {
            _addEditState.update {
                it.copy(error = "Please resolve time conflicts before saving")
            }
            return
        }

        viewModelScope.launch {
            _addEditState.update { it.copy(isSaving = true) }

            try {
                val timeSlot = TimeSlot(
                    id = state.editingSlotId ?: 0,
                    name = state.name,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    soundMode = state.soundMode,
                    activeDays = state.activeDays,
                    vibrationEnabled = state.vibrationEnabled,
                    color = state.selectedColor
                )

                if (state.isEditing) {
                    repository.updateTimeSlot(timeSlot)
                } else {
                    repository.insertTimeSlot(timeSlot)
                }

                _addEditState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }

                // Reschedule alarms
                rescheduleAlarms()

            } catch (e: Exception) {
                _addEditState.update {
                    it.copy(isSaving = false, error = e.message)
                }
            }
        }
    }

    fun toggleSlotEnabled(slot: TimeSlot) {
        viewModelScope.launch {
            repository.toggleEnabled(slot.id, !slot.isEnabled)
            rescheduleAlarms()
        }
    }

    fun deleteTimeSlot(slot: TimeSlot) {
        viewModelScope.launch {
            repository.deleteTimeSlot(slot)
            rescheduleAlarms()
        }
    }

    private fun rescheduleAlarms() {
        val context = getApplication<Application>()
        val intent = Intent(context, SoundSchedulerService::class.java).apply {
            action = SoundSchedulerService.ACTION_RESCHEDULE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
        _addEditState.update { it.copy(error = null) }
    }

    fun resetSaveSuccess() {
        _addEditState.update { it.copy(saveSuccess = false) }
    }
}