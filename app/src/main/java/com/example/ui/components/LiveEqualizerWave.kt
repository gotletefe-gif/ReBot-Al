package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonPrimary

@Composable
fun LiveEqualizerWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    volume: Float = 1.0f,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 18.dp,
    minHeight: Dp = 4.dp,
    barColor: Color = CrimsonPrimary
) {
    val transition = rememberInfiniteTransition(label = "equalizer_anim")

    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val h2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val h4 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 390),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    val heights = listOf(h1, h2, h3, h4)
    val effectiveVol = if (isPlaying) volume.coerceIn(0f, 1f) else 0f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val scale = if (isPlaying && effectiveVol > 0.05f) {
                heights[i % heights.size] * effectiveVol
            } else {
                0.15f
            }
            val currentHeight = minHeight + (maxHeight - minHeight) * scale
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentHeight)
                    .background(
                        color = if (isPlaying && effectiveVol > 0.05f) barColor else barColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(barWidth / 2)
                    )
            )
        }
    }
}
