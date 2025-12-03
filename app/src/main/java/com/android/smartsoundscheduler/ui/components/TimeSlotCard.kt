package com.android.smartsoundscheduler.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.smartsoundscheduler.data.SoundMode
import com.android.smartsoundscheduler.data.TimeSlot
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotCard(
    timeSlot: TimeSlot,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val cardColor = Color(timeSlot.color)

    val animatedAlpha by animateFloatAsState(
        targetValue = if (timeSlot.isEnabled) 1f else 0.6f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (timeSlot.isEnabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = cardColor.copy(alpha = 0.3f),
                spotColor = cardColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            cardColor.copy(alpha = 0.1f * animatedAlpha),
                            Color.Transparent
                        )
                    )
                )
        ) {
            // Main Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    cardColor,
                                    cardColor.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getSoundModeIcon(timeSlot.soundMode),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Time and Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timeSlot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = animatedAlpha
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatTime(timeSlot.startTime)} - ${formatTime(timeSlot.endTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = animatedAlpha
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayOfWeek.values().take(7).forEach { day ->
                            DayChip(
                                day = day,
                                isActive = timeSlot.activeDays.contains(day),
                                color = cardColor,
                                alpha = animatedAlpha
                            )
                        }
                    }
                }

                // Toggle Switch
                Switch(
                    checked = timeSlot.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = cardColor,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Expanded Content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Mode Badge
                        SoundModeBadge(
                            mode = timeSlot.soundMode,
                            color = cardColor
                        )

                        // Vibration Status
                        if (timeSlot.vibrationEnabled) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Vibration On") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Vibration,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(onClick = onEdit) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Schedule?") },
            text = {
                Text("Are you sure you want to delete \"${timeSlot.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DayChip(
    day: DayOfWeek,
    isActive: Boolean,
    color: Color,
    alpha: Float
) {
    val dayName = day.getDisplayName(TextStyle.NARROW, Locale.getDefault())

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                if (isActive) color.copy(alpha = 0.2f * alpha)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) color.copy(alpha = alpha)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f * alpha)
        )
    }
}

@Composable
private fun SoundModeBadge(mode: SoundMode, color: Color) {
    val (icon, text) = when (mode) {
        SoundMode.RING -> Icons.Default.VolumeUp to "Ring Mode"
        SoundMode.SILENT -> Icons.Default.VolumeOff to "Silent Mode"
        SoundMode.VIBRATE -> Icons.Default.Vibration to "Vibrate Mode"
    }

    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

private fun getSoundModeIcon(mode: SoundMode): ImageVector = when (mode) {
    SoundMode.RING -> Icons.Default.VolumeUp
    SoundMode.SILENT -> Icons.Default.VolumeOff
    SoundMode.VIBRATE -> Icons.Default.Vibration
}

private fun formatTime(time: java.time.LocalTime): String {
    val hour = if (time.hour > 12) time.hour - 12 else if (time.hour == 0) 12 else time.hour
    val amPm = if (time.hour >= 12) "PM" else "AM"
    return String.format("%d:%02d %s", hour, time.minute, amPm)
}