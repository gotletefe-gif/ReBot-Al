package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurfaceVariant
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerMode(val title: String, val icon: ImageVector) {
    SPECTRUM("Spektrum", Icons.Filled.GraphicEq),
    WAVEFORM("Dalga", Icons.Filled.Waves),
    STEREO("Stereo", Icons.Filled.SurroundSound)
}

/**
 * Dynamic Audio Visualizer that actively reacts to:
 * 1. Radio stream volume level (0.0f to 1.0f)
 * 2. Mute state (scales smoothly to idle baseline)
 * 3. Playback status (active animation when playing, quiet when paused/stopped)
 */
@Composable
fun DynamicAudioVisualizer(
    isPlaying: Boolean,
    volume: Float,
    isMuted: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(VisualizerMode.SPECTRUM) }

    // Calculate effective volume multiplier
    val targetVolume = if (isMuted || !isPlaying) 0f else volume.coerceIn(0f, 1f)
    val animatedEffectiveVolume by animateFloatAsState(
        targetValue = targetVolume,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "effective_volume_anim"
    )

    // Animated continuous phase oscillators for harmonic simulation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer_phase")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSurfaceVariant.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, ObsidianCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_audio_visualizer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Mode Switcher & Live Volume Response Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Live Stream Volume Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (animatedEffectiveVolume > 0.05f) LiveGreen else Color(0xFF64748B)
                            )
                    )
                    val volumePercent = (animatedEffectiveVolume * 100).toInt()
                    val volumeDbText = if (animatedEffectiveVolume <= 0.01f) {
                        "SES: KAPALI"
                    } else {
                        val approximateDb = ((1f - animatedEffectiveVolume) * -36).toInt()
                        "SES: %$volumePercent (${approximateDb} dB)"
                    }

                    Text(
                        text = volumeDbText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (animatedEffectiveVolume > 0.05f) LiveGreen else Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                }

                // Mode Selector Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VisualizerMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent,
                            label = "mode_tab_bg"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) accentColor else Color(0xFF94A3B8),
                            label = "mode_tab_color"
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = backgroundColor,
                            border = if (isSelected) BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMode = mode }
                                .testTag("visualizer_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = mode.icon,
                                    contentDescription = mode.title,
                                    tint = contentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = mode.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Dynamic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedMode) {
                    VisualizerMode.SPECTRUM -> {
                        SpectrumBarsCanvas(
                            volumeMultiplier = animatedEffectiveVolume,
                            isPlaying = isPlaying,
                            accentColor = accentColor,
                            phase1 = phase1,
                            phase2 = phase2,
                            phase3 = phase3,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    VisualizerMode.WAVEFORM -> {
                        WaveformOscilloscopeCanvas(
                            volumeMultiplier = animatedEffectiveVolume,
                            isPlaying = isPlaying,
                            accentColor = accentColor,
                            phase1 = phase1,
                            phase2 = phase2,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    VisualizerMode.STEREO -> {
                        StereoMirrorSpectrumCanvas(
                            volumeMultiplier = animatedEffectiveVolume,
                            isPlaying = isPlaying,
                            accentColor = accentColor,
                            phase1 = phase1,
                            phase2 = phase2,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Frequency Labels (Sub-bass, Bass, Mid, High, Treble)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("32Hz", "125Hz", "500Hz", "2kHz", "8kHz", "16kHz").forEach { band ->
                    Text(
                        text = band,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

/**
 * 28 Multi-Band Frequency Spectrum Bars with reactive volume heights and floating peak caps
 */
@Composable
private fun SpectrumBarsCanvas(
    volumeMultiplier: Float,
    isPlaying: Boolean,
    accentColor: Color,
    phase1: Float,
    phase2: Float,
    phase3: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 28

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barSpacing = 3.dp.toPx()
        val totalSpacing = (barCount - 1) * barSpacing
        val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

        val minHeight = 4.dp.toPx()
        val maxHeight = canvasHeight - 8.dp.toPx()

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount

            // Harmonic combination simulating real dynamic frequency spectrum
            val waveA = sin(phase1 + normalizedIndex * 4.2f)
            val waveB = cos(phase2 + normalizedIndex * 6.8f)
            val waveC = sin(phase3 + normalizedIndex * 2.1f)
            val combinedWave = (abs(waveA * 0.45f + waveB * 0.35f + waveC * 0.2f)).toFloat()

            // Shape the curve so bass and vocal frequencies have natural audio energy contour
            val frequencyContour = if (normalizedIndex < 0.25f) {
                0.75f + (normalizedIndex * 1.2f)
            } else if (normalizedIndex < 0.65f) {
                1.0f - ((normalizedIndex - 0.25f) * 0.5f)
            } else {
                0.6f + ((normalizedIndex - 0.65f) * 0.6f)
            }

            val dynamicHeightFraction = (combinedWave * frequencyContour).coerceIn(0.12f, 1.0f)

            // Scale dynamically by radio stream volume
            val finalHeight = if (isPlaying && volumeMultiplier > 0.02f) {
                minHeight + (maxHeight - minHeight) * dynamicHeightFraction * volumeMultiplier
            } else {
                minHeight
            }

            val x = i * (barWidth + barSpacing)
            val y = canvasHeight - finalHeight

            // Color gradient for the bar (Station Accent -> Amber -> Crimson)
            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    accentColor,
                    AmberSecondary,
                    CrimsonPrimary
                ),
                startY = y,
                endY = canvasHeight
            )

            // Draw rounded equalizer bar
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, finalHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
                alpha = if (isPlaying && volumeMultiplier > 0.02f) 0.95f else 0.35f
            )

            // Draw floating peak cap above the bar when active
            if (isPlaying && volumeMultiplier > 0.1f) {
                val peakGap = 3.dp.toPx()
                val peakY = (y - peakGap).coerceAtLeast(1.dp.toPx())
                drawCircle(
                    color = AmberLight,
                    radius = (barWidth / 2).coerceAtMost(2.5.dp.toPx()),
                    center = Offset(x + barWidth / 2, peakY),
                    alpha = (volumeMultiplier * 0.9f).coerceIn(0.2f, 1f)
                )
            }
        }
    }
}

/**
 * Smooth Fluid Waveform Oscilloscope reacting to volume amplitude
 */
@Composable
private fun WaveformOscilloscopeCanvas(
    volumeMultiplier: Float,
    isPlaying: Boolean,
    accentColor: Color,
    phase1: Float,
    phase2: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val effectiveAmp = if (isPlaying) {
            (height * 0.42f) * volumeMultiplier
        } else {
            2.dp.toPx()
        }

        val step = 4f
        val path = Path()
        val fillPath = Path()

        var first = true
        fillPath.moveTo(0f, centerY)

        var x = 0f
        while (x <= width) {
            val progress = x / width
            // Modulation envelope so wave nicely tapers at left and right edges
            val envelope = sin(progress * PI.toFloat())

            val yOffset = (
                sin(progress * 12f + phase1) * 0.65f +
                cos(progress * 22f - phase2) * 0.35f
            ) * effectiveAmp * envelope

            val y = centerY + yOffset

            if (first) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            x += step
        }

        fillPath.lineTo(width, centerY)
        fillPath.close()

        // Gradient filled translucent glow underneath the wave
        val fillGradient = Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.35f * volumeMultiplier),
                CrimsonPrimary.copy(alpha = 0.05f)
            ),
            startY = 0f,
            endY = height
        )
        drawPath(path = fillPath, brush = fillGradient)

        // Glowing stroke line
        val strokeGradient = Brush.horizontalGradient(
            colors = listOf(
                CrimsonPrimary,
                accentColor,
                AmberSecondary,
                CrimsonPrimary
            )
        )
        drawPath(
            path = path,
            brush = strokeGradient,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Symmetrical Dual Stereo Mirror Spectrum mirroring from center outwards
 */
@Composable
private fun StereoMirrorSpectrumCanvas(
    volumeMultiplier: Float,
    isPlaying: Boolean,
    accentColor: Color,
    phase1: Float,
    phase2: Float,
    modifier: Modifier = Modifier
) {
    val halfBars = 16

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val centerX = width / 2f

        val barSpacing = 2.5.dp.toPx()
        val availableHalfWidth = centerX - 4.dp.toPx()
        val barWidth = ((availableHalfWidth - (halfBars * barSpacing)) / halfBars).coerceAtLeast(2.dp.toPx())
        val maxAmplitude = height * 0.44f

        for (i in 0 until halfBars) {
            val norm = i.toFloat() / halfBars
            val wave = (sin(phase1 + norm * 5.5f) * 0.6f + cos(phase2 + norm * 3.8f) * 0.4f).toFloat()
            val amp = if (isPlaying && volumeMultiplier > 0.02f) {
                (3.dp.toPx() + (maxAmplitude - 3.dp.toPx()) * abs(wave) * volumeMultiplier)
            } else {
                3.dp.toPx()
            }

            val alphaVal = if (isPlaying && volumeMultiplier > 0.02f) 0.9f else 0.35f

            // Color gradient
            val color = if (norm < 0.4f) accentColor else if (norm < 0.75f) AmberSecondary else CrimsonLight

            // Right side bar
            val rightX = centerX + (i * (barWidth + barSpacing)) + 2.dp.toPx()
            drawRoundRect(
                color = color,
                topLeft = Offset(rightX, centerY - amp),
                size = Size(barWidth, amp * 2),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
                alpha = alphaVal
            )

            // Left side mirrored bar
            val leftX = centerX - ((i + 1) * (barWidth + barSpacing)) - 2.dp.toPx()
            drawRoundRect(
                color = color,
                topLeft = Offset(leftX, centerY - amp),
                size = Size(barWidth, amp * 2),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
                alpha = alphaVal
            )
        }

        // Center stereo divider dot
        drawCircle(
            color = if (isPlaying && volumeMultiplier > 0.05f) LiveGreen else Color.Gray,
            radius = 3.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Animated dynamic pulsing audio wave aura around the vinyl turntable disc
 * whose radius and intensity expand in real-time with stream volume level!
 */
@Composable
fun TurntableVolumeAura(
    isPlaying: Boolean,
    volume: Float,
    isMuted: Boolean,
    station: RadioStation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val targetVol = if (isMuted || !isPlaying) 0f else volume.coerceIn(0f, 1f)
    val animatedVol by animateFloatAsState(
        targetValue = targetVol,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "turntable_vol_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring_anim")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Outer pulsing energy rings reacting to volume level
        if (isPlaying && animatedVol > 0.05f) {
            // Outermost ripple
            val ring2Scale = 1.0f + (pulseScale2 - 1.0f) * animatedVol
            val ring2Alpha = (pulseAlpha2 * animatedVol).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize(ring2Scale)
                    .clip(CircleShape)
                    .border(
                        width = (2.dp * animatedVol).coerceAtLeast(1.dp),
                        color = Color(station.accentColorHex).copy(alpha = ring2Alpha),
                        shape = CircleShape
                    )
            )

            // Inner ripple
            val ring1Scale = 1.0f + (pulseScale1 - 1.0f) * animatedVol
            val ring1Alpha = (pulseAlpha1 * animatedVol).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize(ring1Scale)
                    .clip(CircleShape)
                    .border(
                        width = (3.dp * animatedVol).coerceAtLeast(1.dp),
                        color = AmberSecondary.copy(alpha = ring1Alpha),
                        shape = CircleShape
                    )
            )

            // Ambient background glow aura
            Box(
                modifier = Modifier
                    .fillMaxSize(1.15f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(station.accentColorHex).copy(alpha = 0.25f * animatedVol),
                                CrimsonPrimary.copy(alpha = 0.12f * animatedVol),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Inner turntable disc content
        content()
    }
}
