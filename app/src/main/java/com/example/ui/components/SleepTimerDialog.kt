package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import java.util.Locale

data class SleepTimerPreset(
    val minutes: Int,
    val label: String,
    val subtitle: String
)

val SLEEP_TIMER_PRESETS = listOf(
    SleepTimerPreset(15, "15 dk", "15 Dakika"),
    SleepTimerPreset(30, "30 dk", "Yarım Saat"),
    SleepTimerPreset(45, "45 dk", "45 Dakika"),
    SleepTimerPreset(60, "60 dk", "1 Saat")
)

@Composable
fun SleepTimerDialog(
    isActive: Boolean,
    remainingMinutes: Int?,
    remainingSeconds: Long? = null,
    totalSeconds: Long? = null,
    selectedMinutes: Int? = null,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AmberSecondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = "Uyku Zamanlayıcısı",
                        tint = AmberSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Uyku Zamanlayıcısı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Yayın otomatik durdurma",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Süre bittiğinde radyo yayını kesintisiz ve sessizce otomatik olarak durdurulur.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    lineHeight = 20.sp
                )

                // Active Countdown Status Card
                if (isActive) {
                    val currentRemainingSecs = remainingSeconds ?: ((remainingMinutes ?: 1) * 60L)
                    val currentTotalSecs = totalSeconds ?: ((selectedMinutes ?: remainingMinutes ?: 1) * 60L).coerceAtLeast(1L)
                    val progressFraction = (currentRemainingSecs.toFloat() / currentTotalSecs.toFloat()).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        label = "timer_progress"
                    )

                    val minsPart = (currentRemainingSecs / 60L)
                    val secsPart = (currentRemainingSecs % 60L)
                    val timeFormatted = String.format(Locale.US, "%02d:%02d", minsPart, secsPart)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AmberSecondary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_timer_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Timer,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Geri Sayım Aktif",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberSecondary
                                    )
                                }

                                if (selectedMinutes != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AmberSecondary.copy(alpha = 0.25f),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = "$selectedMinutes dk mod",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AmberSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Large Digital Timer Display
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                letterSpacing = 2.sp,
                                modifier = Modifier.testTag("timer_remaining_display")
                            )

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AmberSecondary,
                                trackColor = AmberSecondary.copy(alpha = 0.2f)
                            )

                            // Cancel Button
                            OutlinedButton(
                                onClick = onCancelTimer,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF4444)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("cancel_timer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TimerOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Zamanlayıcıyı İptal Et",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }

                // Preset Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isActive) "Süreyi Değiştir:" else "Otomatik Durdurma Süresi Seçin:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // 2x2 Grid of 15, 30, 45, 60 minutes
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SLEEP_TIMER_PRESETS.chunked(2).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val isCurrentPreset = isActive && selectedMinutes == preset.minutes
                                    PresetButton(
                                        preset = preset,
                                        isSelected = isCurrentPreset,
                                        onClick = { onSetTimer(preset.minutes) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_timer_dialog")
            ) {
                Text(
                    text = "Kapat",
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun PresetButton(
    preset: SleepTimerPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) AmberSecondary.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AmberSecondary else ObsidianCardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("timer_preset_${preset.minutes}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = preset.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AmberSecondary else Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = preset.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) AmberSecondary.copy(alpha = 0.8f) else Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}
