package com.android.smartsoundscheduler.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.smartsoundscheduler.data.SoundMode
import com.android.smartsoundscheduler.data.TimeSlotConflict
import com.android.smartsoundscheduler.ui.components.CustomTimePicker
import com.android.smartsoundscheduler.ui.components.DaySelector
import com.android.smartsoundscheduler.ui.components.QuickDaySelector
import com.android.smartsoundscheduler.ui.theme.CardColors
import com.android.smartsoundscheduler.viewmodel.AddEditUiState
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSlotScreen(
    state: AddEditUiState,
    onNameChange: (String) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onSoundModeChange: (SoundMode) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
    onQuickDaySelect: (Set<DayOfWeek>) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onColorChange: (Long) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val selectedColor = Color(state.selectedColor)

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit Schedule" else "New Schedule",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = !state.isSaving && state.conflicts.isEmpty()
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Save",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Error Message
            AnimatedVisibility(
                visible = state.error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                state.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Conflicts Warning
            AnimatedVisibility(
                visible = state.conflicts.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ConflictWarningCard(
                    conflicts = state.conflicts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // Name Input
            SectionCard(
                title = "Schedule Name",
                icon = Icons.Outlined.Label
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = { Text("e.g., Work Hours, Night Mode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        cursorColor = selectedColor
                    )
                )
            }

            // Time Selection
            SectionCard(
                title = "Time Range",
                icon = Icons.Outlined.Schedule
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimePickerButton(
                        label = "Start Time",
                        time = state.startTime,
                        onClick = { showStartTimePicker = true },
                        accentColor = selectedColor,
                        modifier = Modifier.weight(1f)
                    )

                    TimePickerButton(
                        label = "End Time",
                        time = state.endTime,
                        onClick = { showEndTimePicker = true },
                        accentColor = selectedColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Duration indicator
                val duration = calculateDuration(state.startTime, state.endTime)
                Text(
                    text = "Duration: $duration",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Days Selection
            SectionCard(
                title = "Active Days",
                icon = Icons.Outlined.CalendarMonth
            ) {
                QuickDaySelector(
                    selectedDays = state.activeDays,
                    onSelectionChange = onQuickDaySelect
                )

                Spacer(modifier = Modifier.height(16.dp))

                DaySelector(
                    selectedDays = state.activeDays,
                    onDayToggle = onDayToggle,
                    accentColor = selectedColor
                )
            }

            // Sound Mode Selection
            SectionCard(
                title = "Sound Mode",
                icon = Icons.Outlined.VolumeUp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoundModeOption(
                        mode = SoundMode.RING,
                        icon = Icons.Default.VolumeUp,
                        label = "Ring",
                        isSelected = state.soundMode == SoundMode.RING,
                        onClick = { onSoundModeChange(SoundMode.RING) },
                        accentColor = selectedColor,
                        modifier = Modifier.weight(1f)
                    )

                    SoundModeOption(
                        mode = SoundMode.VIBRATE,
                        icon = Icons.Default.Vibration,
                        label = "Vibrate",
                        isSelected = state.soundMode == SoundMode.VIBRATE,
                        onClick = { onSoundModeChange(SoundMode.VIBRATE) },
                        accentColor = selectedColor,
                        modifier = Modifier.weight(1f)
                    )

                    SoundModeOption(
                        mode = SoundMode.SILENT,
                        icon = Icons.Default.VolumeOff,
                        label = "Silent",
                        isSelected = state.soundMode == SoundMode.SILENT,
                        onClick = { onSoundModeChange(SoundMode.SILENT) },
                        accentColor = selectedColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Additional Options
            SectionCard(
                title = "Additional Options",
                icon = Icons.Outlined.Tune
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vibration",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Allow vibration in this mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = state.vibrationEnabled,
                        onCheckedChange = onVibrationChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = selectedColor
                        )
                    )
                }
            }

            // Color Selection
            SectionCard(
                title = "Theme Color",
                icon = Icons.Outlined.Palette
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardColors.forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = state.selectedColor == color.value.toLong(),
                            onClick = { onColorChange(color.value.toLong()) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Time Picker Dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            title = "Select Start Time",
            initialTime = state.startTime,
            onConfirm = { time ->
                onStartTimeChange(time)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            title = "Select End Time",
            initialTime = state.endTime,
            onConfirm = { time ->
                onEndTimeChange(time)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            content()
        }
    }
}

@Composable
private fun TimePickerButton(
    label: String,
    time: LocalTime,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatTime(time),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun SoundModeOption(
    mode: SoundMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected)
        accentColor.copy(alpha = 0.15f)
    else Color.Transparent

    val borderColor = if (isSelected)
        accentColor
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) accentColor
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) accentColor
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ConflictWarningCard(
    conflicts: List<TimeSlotConflict>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Time Conflicts Detected",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            conflicts.forEach { conflict ->
                Text(
                    text = "• Conflicts with \"${conflict.existingSlot.name}\" on ${
                        conflict.conflictingDays.joinToString(", ") {
                            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTime by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            CustomTimePicker(
                selectedTime = selectedTime,
                onTimeSelected = { selectedTime = it }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTime) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(time: LocalTime): String {
    val hour = if (time.hour > 12) time.hour - 12
    else if (time.hour == 0) 12
    else time.hour
    val amPm = if (time.hour >= 12) "PM" else "AM"
    return String.format("%d:%02d %s", hour, time.minute, amPm)
}

private fun calculateDuration(start: LocalTime, end: LocalTime): String {
    val startMinutes = start.hour * 60 + start.minute
    var endMinutes = end.hour * 60 + end.minute

    if (endMinutes <= startMinutes) {
        endMinutes += 24 * 60 // Next day
    }

    val durationMinutes = endMinutes - startMinutes
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60

    return when {
        hours == 0 -> "${minutes}min"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}min"
    }
}