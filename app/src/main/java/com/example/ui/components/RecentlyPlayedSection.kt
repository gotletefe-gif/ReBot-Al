package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun RecentlyPlayedSection(
    recentStations: List<RadioStation>,
    currentStation: RadioStation?,
    playbackStatus: PlaybackStatus,
    onStationClick: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentStations.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recently_played_section")
    ) {
        // Section Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = AmberSecondary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Son Dinlenenler",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberSecondary.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, AmberSecondary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Son ${recentStations.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Hızlı Erişim",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        // Horizontal Row of the last 5 stations
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("recently_played_list"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = recentStations.take(5),
                key = { "recent_${it.id}" }
            ) { station ->
                val isCurrent = currentStation?.id == station.id
                val isPlaying = isCurrent && playbackStatus == PlaybackStatus.PLAYING

                RecentStationCard(
                    station = station,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying,
                    onClick = { onStationClick(station) }
                )
            }
        }
    }
}

@Composable
private fun RecentStationCard(
    station: RadioStation,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(station.accentColorHex)
    val borderColor by animateColorAsState(
        targetValue = when {
            isPlaying -> LiveGreen
            isCurrent -> CrimsonPrimary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(200),
        label = "recent_border"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPlaying -> CrimsonPrimary.copy(alpha = 0.12f)
            isCurrent -> surfaceVariantColor
            else -> surfaceColor
        },
        animationSpec = tween(200),
        label = "recent_bg"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .width(150.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("recent_station_${station.id}"),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Icon / Emoji container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.85f),
                                accentColor.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    LiveEqualizerWave(
                        isPlaying = true,
                        barColor = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = station.iconEmoji,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Station Name & Frequency
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = station.frequency,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCurrent) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiveGreen)
                        )
                    }
                }
            }
        }
    }
}
