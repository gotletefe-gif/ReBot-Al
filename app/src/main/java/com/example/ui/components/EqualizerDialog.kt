package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioQualityPreset
import com.example.model.EqualizerPreset
import com.example.model.EqualizerSettings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun EqualizerDialog(
    settings: EqualizerSettings,
    onToggleEnabled: (Boolean) -> Unit,
    onBassChange: (Int) -> Unit,
    onMidChange: (Int) -> Unit,
    onTrebleChange: (Int) -> Unit,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onSelectQuality: (AudioQualityPreset) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AmberSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Ekolayzer",
                            tint = AmberSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Ses & Ekolayzer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (settings.isEnabled) "Ekolayzer Aktif" else "Devre Dışı",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (settings.isEnabled) AmberSecondary else Color(0xFF94A3B8)
                        )
                    }
                }

                // Master On/Off Switch
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AmberSecondary,
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = ObsidianSurfaceVariant
                    ),
                    modifier = Modifier.testTag("equalizer_switch")
                )
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Section 1: Ses Kalitesi Seçimi (Audio Quality Selection)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Yayın Akış Kalitesi",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = settings.qualityPreset.estimatedBitrate,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberSecondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioQualityPreset.values().forEach { quality ->
                            val isSelected = settings.qualityPreset == quality
                            QualityOptionCard(
                                quality = quality,
                                isSelected = isSelected,
                                onClick = { onSelectQuality(quality) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section 2: Equalizer Presets Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hazır Ses Profilleri",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    val presetRowScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(presetRowScroll),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EqualizerPreset.values().forEach { preset ->
                            val isSelected = settings.activePreset == preset
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) AmberSecondary else ObsidianSurfaceVariant,
                                label = "chip_bg"
                            )
                            val chipContentColor by animateColorAsState(
                                targetValue = if (isSelected) Color.Black else Color.White,
                                label = "chip_fg"
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = chipBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AmberSecondary else ObsidianCardBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectPreset(preset) }
                                    .testTag("eq_preset_${preset.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.iconEmoji,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = chipContentColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Visual Graphic Equalizer Preview Bars
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurfaceVariant,
                    border = BorderStroke(1.dp, ObsidianCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Frekans Spektrumu",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = settings.activePreset.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberSecondary
                            )
                        }

                        // 5-bar visual graphic indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val bands = listOf(
                                "Bas 60Hz" to settings.bassDb,
                                "Düşük 250Hz" to ((settings.bassDb * 2 + settings.midDb) / 3),
                                "Orta 1kHz" to settings.midDb,
                                "Yüksek 4kHz" to ((settings.trebleDb * 2 + settings.midDb) / 3),
                                "Tiz 14kHz" to settings.trebleDb
                            )

                            bands.forEach { (label, dbVal) ->
                                SpectrumBar(
                                    label = label,
                                    dbVal = if (settings.isEnabled) dbVal else 0,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 4: Bass, Mid, Treble Sliders
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.testTag("frequency_sliders_section")
                ) {
                    // Bass Slider (-10 dB to +10 dB)
                    FrequencyBandSlider(
                        title = "Bas (Düşük Frekans)",
                        frequencyRange = "60 - 250 Hz",
                        currentDb = settings.bassDb,
                        isEnabled = settings.isEnabled,
                        onValueChange = onBassChange,
                        testTagPrefix = "bass"
                    )

                    // Mid/Vocal Slider (-10 dB to +10 dB)
                    FrequencyBandSlider(
                        title = "Orta Ton / Vokal",
                        frequencyRange = "500 Hz - 2 kHz",
                        currentDb = settings.midDb,
                        isEnabled = settings.isEnabled,
                        onValueChange = onMidChange,
                        testTagPrefix = "mid"
                    )

                    // Treble Slider (-10 dB to +10 dB)
                    FrequencyBandSlider(
                        title = "Tiz (Yüksek Frekans)",
                        frequencyRange = "4 kHz - 16 kHz",
                        currentDb = settings.trebleDb,
                        isEnabled = settings.isEnabled,
                        onValueChange = onTrebleChange,
                        testTagPrefix = "treble"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_equalizer_dialog")
            ) {
                Text(
                    text = "Tamam",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onReset,
                modifier = Modifier.testTag("reset_equalizer_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sıfırla",
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
private fun QualityOptionCard(
    quality: AudioQualityPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AmberSecondary.copy(alpha = 0.18f) else ObsidianSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) AmberSecondary else ObsidianCardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("quality_option_${quality.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) AmberSecondary else Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = quality.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quality.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AmberSecondary else Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpectrumBar(
    label: String,
    dbVal: Int,
    modifier: Modifier = Modifier
) {
    // dbVal ranges from -10 to +10. Map to height fraction 0.15f .. 1.0f
    val fraction = ((dbVal + 10f) / 20f).coerceIn(0.12f, 1.0f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "spectrum_fraction"
    )

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height((42 * animatedFraction).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    if (dbVal > 0) AmberSecondary else if (dbVal < 0) CrimsonPrimary else Color(0xFF64748B)
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${if (dbVal > 0) "+" else ""}$dbVal",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (dbVal != 0) AmberSecondary else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun FrequencyBandSlider(
    title: String,
    frequencyRange: String,
    currentDb: Int,
    isEnabled: Boolean,
    onValueChange: (Int) -> Unit,
    testTagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ObsidianSurfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, ObsidianCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color.White else Color(0xFF64748B)
                    )
                    Text(
                        text = frequencyRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }

                // DB Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (!isEnabled) {
                        Color.White.copy(alpha = 0.05f)
                    } else if (currentDb > 0) {
                        AmberSecondary.copy(alpha = 0.2f)
                    } else if (currentDb < 0) {
                        CrimsonPrimary.copy(alpha = 0.2f)
                    } else {
                        Color.White.copy(alpha = 0.08f)
                    }
                ) {
                    Text(
                        text = "${if (currentDb > 0) "+" else ""}$currentDb dB",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (!isEnabled) {
                            Color(0xFF64748B)
                        } else if (currentDb > 0) {
                            AmberSecondary
                        } else if (currentDb < 0) {
                            Color(0xFFF87171)
                        } else {
                            Color.White
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Slider(
                value = currentDb.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = -10f..10f,
                steps = 19, // steps every 1 dB
                enabled = isEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = if (isEnabled) AmberSecondary else Color(0xFF64748B),
                    activeTrackColor = if (isEnabled) AmberSecondary else Color(0xFF475569),
                    inactiveTrackColor = ObsidianCardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_slider")
            )
        }
    }
}
