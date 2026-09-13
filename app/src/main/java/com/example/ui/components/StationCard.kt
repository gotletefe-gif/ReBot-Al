package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun StationCard(
    station: RadioStation,
    isCurrentStation: Boolean,
    playbackStatus: PlaybackStatus,
    onStationClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying = isCurrentStation && playbackStatus == PlaybackStatus.PLAYING
    val isConnecting = isCurrentStation && playbackStatus == PlaybackStatus.CONNECTING

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isPlaying -> CrimsonPrimary
            isCurrentStation -> AmberSecondary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "card_border"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val cardBackgroundBrush = if (isCurrentStation) {
        Brush.horizontalGradient(
            colors = listOf(
                surfaceVariantColor,
                Color(station.accentColorHex).copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                surfaceColor,
                surfaceVariantColor.copy(alpha = 0.6f)
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onStationClick)
            .testTag("station_card_${station.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorderColor),
        color = Color.Transparent,
        tonalElevation = if (isCurrentStation) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .background(cardBackgroundBrush)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Station Icon Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(station.accentColorHex).copy(alpha = 0.35f),
                                    Color(station.accentColorHex).copy(alpha = 0.12f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (isPlaying) CrimsonPrimary else Color(station.accentColorHex).copy(alpha = 0.3f),
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = CrimsonPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else if (isPlaying) {
                        LiveEqualizerWave(
                            isPlaying = true,
                            barCount = 4,
                            barWidth = 3.dp,
                            maxHeight = 22.dp,
                            barColor = CrimsonPrimary
                        )
                    } else {
                        Text(
                            text = station.iconEmoji,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Station Info Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (isPlaying) {
                            Spacer(modifier = Modifier.width(8.dp))
                            PulsingLiveBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Frequency Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonPrimary.copy(alpha = 0.18f),
                            border = BorderStroke(0.5.dp, CrimsonPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = station.frequency,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CrimsonPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = station.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // City Pill
                        Text(
                            text = "• ${station.city}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (station.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = station.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Actions: Favorite & Play/Pause Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("favorite_button_${station.id}")
                    ) {
                        Icon(
                            imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (station.isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                            tint = if (station.isFavorite) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onStationClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying) CrimsonPrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("play_pause_${station.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                            tint = if (isPlaying) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun PulsingLiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = CrimsonPrimary.copy(alpha = 0.2f),
        border = BorderStroke(0.5.dp, CrimsonPrimary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(LiveGreen.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "CANLI",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
