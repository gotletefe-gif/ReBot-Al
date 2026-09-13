package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.PlayerState
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface

@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onMiniPlayerClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val station = playerState.currentStation
    val isVisible = station != null
    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val isConnecting = playerState.status == PlaybackStatus.CONNECTING

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (station == null) return@AnimatedVisibility

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onMiniPlayerClick)
                .testTag("mini_player"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color(station.accentColorHex).copy(alpha = 0.18f)
                            )
                        )
                    )
            ) {
                // Top thin indicator line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            if (isPlaying) CrimsonPrimary else MaterialTheme.colorScheme.outline
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Station Avatar / Equalizer
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(station.accentColorHex).copy(alpha = 0.5f),
                                        Color(station.accentColorHex).copy(alpha = 0.2f)
                                    )
                                )
                            )
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CrimsonPrimary,
                                strokeWidth = 2.dp
                            )
                        } else if (isPlaying) {
                            LiveEqualizerWave(
                                isPlaying = true,
                                volume = if (playerState.isMuted) 0f else playerState.volume,
                                barCount = 3,
                                barWidth = 3.dp,
                                maxHeight = 18.dp,
                                barColor = CrimsonPrimary
                            )
                        } else {
                            Text(
                                text = station.iconEmoji,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title and status
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LiveGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CANLI YAYIN • ${station.frequency}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = LiveGreen
                                )
                            } else if (isConnecting) {
                                Text(
                                    text = "Yayın bağlanıyor...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = CrimsonPrimary
                                )
                            } else if (playerState.status == PlaybackStatus.ERROR) {
                                Text(
                                    text = "Yeniden bağlanılıyor...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444)
                                )
                            } else {
                                Text(
                                    text = "Duraklatıldı • ${station.frequency}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (playerState.sleepTimerActive && playerState.sleepTimerMinutesRemaining != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• 🌙 ${playerState.sleepTimerMinutesRemaining} dk",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberSecondary
                                )
                            }
                        }
                    }

                    // Transport controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPreviousClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_previous")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Önceki Radyo",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CrimsonPrimary)
                                .testTag("mini_player_play_pause")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = onNextClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_next")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Sonraki Radyo",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
