package com.android.smartsoundscheduler.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CustomTimePicker(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHourPicker by remember { mutableStateOf(true) }
    var isAM by remember { mutableStateOf(selectedTime.hour < 12) }
    var selectedHour by remember { mutableStateOf(
        if (selectedTime.hour == 0) 12
        else if (selectedTime.hour > 12) selectedTime.hour - 12
        else selectedTime.hour
    )}
    var selectedMinute by remember { mutableStateOf(selectedTime.minute) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hour
            TimeUnit(
                value = selectedHour,
                isSelected = showHourPicker,
                onClick = { showHourPicker = true }
            )

            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Minute
            TimeUnit(
                value = selectedMinute,
                isSelected = !showHourPicker,
                onClick = { showHourPicker = false }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // AM/PM Toggle
            Column {
                AmPmButton(
                    text = "AM",
                    isSelected = isAM,
                    onClick = {
                        isAM = true
                        updateTime(selectedHour, selectedMinute, true, onTimeSelected)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                AmPmButton(
                    text = "PM",
                    isSelected = !isAM,
                    onClick = {
                        isAM = false
                        updateTime(selectedHour, selectedMinute, false, onTimeSelected)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Clock Face
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showHourPicker) {
                HourClockFace(
                    selectedHour = selectedHour,
                    onHourSelected = { hour ->
                        selectedHour = hour
                        updateTime(hour, selectedMinute, isAM, onTimeSelected)
                    }
                )
            } else {
                MinuteClockFace(
                    selectedMinute = selectedMinute,
                    onMinuteSelected = { minute ->
                        selectedMinute = minute
                        updateTime(selectedHour, minute, isAM, onTimeSelected)
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeUnit(
    value: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent
    ) {
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AmPmButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HourClockFace(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    val hours = (1..12).toList()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Hour numbers
        hours.forEach { hour ->
            val angle = (hour - 3) * 30.0 * Math.PI / 180
            val radius = 100.dp

            Box(
                modifier = Modifier
                    .offset(
                        x = (cos(angle) * radius.value).dp,
                        y = (sin(angle) * radius.value).dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = { onHourSelected(hour) },
                    shape = CircleShape,
                    color = if (hour == selectedHour)
                        MaterialTheme.colorScheme.primary
                    else Color.Transparent
                ) {
                    Text(
                        text = hour.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (hour == selectedHour)
                            FontWeight.Bold else FontWeight.Normal,
                        color = if (hour == selectedHour)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Center dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Hour hand
        val handAngle = (selectedHour - 3) * 30f
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(80.dp)
                .rotate(handAngle)
                .offset(y = (-40).dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun MinuteClockFace(
    selectedMinute: Int,
    onMinuteSelected: (Int) -> Unit
) {
    val minutes = (0..55 step 5).toList()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Minute numbers
        minutes.forEach { minute ->
            val angle = (minute - 15) * 6.0 * Math.PI / 180
            val radius = 100.dp

            Box(
                modifier = Modifier
                    .offset(
                        x = (cos(angle) * radius.value).dp,
                        y = (sin(angle) * radius.value).dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = { onMinuteSelected(minute) },
                    shape = CircleShape,
                    color = if (minute == selectedMinute ||
                        (selectedMinute % 5 != 0 && minute == (selectedMinute / 5) * 5))
                        MaterialTheme.colorScheme.primary
                    else Color.Transparent
                ) {
                    Text(
                        text = String.format("%02d", minute),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (minute == selectedMinute)
                            FontWeight.Bold else FontWeight.Normal,
                        color = if (minute == selectedMinute)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Center dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Minute hand
        val handAngle = (selectedMinute - 15) * 6f
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(100.dp)
                .rotate(handAngle)
                .offset(y = (-50).dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

private fun updateTime(
    hour: Int,
    minute: Int,
    isAM: Boolean,
    onTimeSelected: (LocalTime) -> Unit
) {
    val hour24 = when {
        isAM && hour == 12 -> 0
        !isAM && hour == 12 -> 12
        !isAM -> hour + 12
        else -> hour
    }
    onTimeSelected(LocalTime.of(hour24, minute))
}