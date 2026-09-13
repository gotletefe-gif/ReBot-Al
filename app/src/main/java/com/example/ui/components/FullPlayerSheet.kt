package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurfaceVariant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    playerState: PlayerState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onStopClick: () -> Unit = {},
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onSetSleepTimer: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val station = playerState.currentStation ?: return
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val isConnecting = playerState.status == PlaybackStatus.CONNECTING
    val isError = playerState.status == PlaybackStatus.ERROR

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Dismiss down arrow, Title, and Sleep Timer button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("full_player_dismiss")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CANLI RADYO",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary
                    )
                    Text(
                        text = "Radyo Türk",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sleep Timer Icon / Badge
                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.testTag("full_player_sleep_timer")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Filled.NightsStay,
                            contentDescription = "Uyku Zamanlayıcısı",
                            tint = if (playerState.sleepTimerActive) AmberSecondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        if (playerState.sleepTimerActive && playerState.sleepTimerMinutesRemaining != null) {
                            Surface(
                                shape = CircleShape,
                                color = AmberSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "${playerState.sleepTimerMinutesRemaining}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Vinyl Disc / Radio Tuner with Dynamic Volume Soundwave Aura
            TurntableVolumeAura(
                isPlaying = isPlaying,
                volume = playerState.volume,
                isMuted = playerState.isMuted,
                station = station,
                modifier = Modifier.size(250.dp)
            ) {
                VinylTurntableDisc(
                    station = station,
                    isPlaying = isPlaying,
                    isConnecting = isConnecting,
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Station Information
            Text(
                text = station.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Frequency and Category Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = station.frequency,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = station.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = station.city,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (station.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = station.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Status Indicator or Error Alert
            Spacer(modifier = Modifier.height(10.dp))
            if (isError) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF7F1D1D).copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = playerState.errorMessage ?: "Bağlantı hatası",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Yeniden Dene",
                                tint = Color.White
                            )
                        }
                    }
                }
            } else if (isConnecting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = CrimsonPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Canlı yayına bağlanılıyor...",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonLight
                    )
                }
            } else if (isPlaying) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LiveGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CANLI YAYIN • ${station.bitrate} STEREO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LiveGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Audio Visualizer reacting to radio stream volume level
            DynamicAudioVisualizer(
                isPlaying = isPlaying,
                volume = playerState.volume,
                isMuted = playerState.isMuted,
                accentColor = Color(station.accentColorHex),
                modifier = Modifier.fillMaxWidth(0.95f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Slider Row
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = if (playerState.isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                        contentDescription = "Ses",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = if (playerState.isMuted) 0f else playerState.volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )

                Text(
                    text = "${((if (playerState.isMuted) 0f else playerState.volume) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Playback Controls Row (Favorite, Previous, Play/Pause, Next, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Favorite Button
                IconButton(
                    onClick = { onToggleFavorite(station) },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("full_player_favorite")
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (station.isFavorite) CrimsonPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Previous Station
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("full_player_previous")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Önceki",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Primary Play / Pause Button
                Surface(
                    shape = CircleShape,
                    color = CrimsonPrimary,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                ) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("full_player_play_pause")
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                // Stop Button
                IconButton(
                    onClick = onStopClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("full_player_stop")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        contentDescription = "Durdur",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Next Station
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("full_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Sonraki",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Share Station Button
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${station.name} (${station.frequency}) radyosunu Radyo Türk uygulamasında canlı dinliyorum!"
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Radyoyu Paylaş")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("full_player_share")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Paylaş",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inline Sleep Timer Section (15, 30, 45, 60 dk quick presets)
            InlineSleepTimerBar(
                isActive = playerState.sleepTimerActive,
                remainingSeconds = playerState.sleepTimerSecondsRemaining,
                remainingMinutes = playerState.sleepTimerMinutesRemaining,
                selectedMinutes = playerState.sleepTimerSelectedMinutes,
                onSetTimer = onSetSleepTimer,
                onCancelTimer = onCancelSleepTimer,
                onOpenFullDialog = onOpenSleepTimer,
                modifier = Modifier.fillMaxWidth(0.95f)
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun InlineSleepTimerBar(
    isActive: Boolean,
    remainingSeconds: Long?,
    remainingMinutes: Int?,
    selectedMinutes: Int?,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onOpenFullDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) AmberSecondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.testTag("inline_sleep_timer_bar")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable(onClick = onOpenFullDialog)
                ) {
                    Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = "Uyku Zamanlayıcısı",
                        tint = if (isActive) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Uyku Zamanlayıcısı",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) AmberSecondary else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isActive) {
                    val currentSecs = remainingSeconds ?: ((remainingMinutes ?: 1) * 60L)
                    val mins = currentSecs / 60L
                    val secs = currentSecs % 60L
                    val countdownStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberSecondary.copy(alpha = 0.2f),
                            modifier = Modifier.clickable(onClick = onOpenFullDialog)
                        ) {
                            Text(
                                text = countdownStr,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = AmberSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = onCancelTimer,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("cancel_inline_timer")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "İptal Et",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 15, 30, 45, 60 dk quick presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(15, 30, 45, 60)
                presets.forEach { minutes ->
                    val isSelected = isActive && selectedMinutes == minutes
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AmberSecondary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AmberSecondary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSetTimer(minutes) }
                            .testTag("inline_timer_preset_$minutes")
                    ) {
                        Text(
                            text = "$minutes dk",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VinylTurntableDisc(
    station: RadioStation,
    isPlaying: Boolean,
    isConnecting: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_disc_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Outer vinyl disc body with concentric circles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (isPlaying) rotation else 0f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E232E),
                            Color(0xFF11141A),
                            Color(0xFF07090C)
                        )
                    )
                )
                .border(2.dp, Color(0xFF2A3142), CircleShape)
        ) {
            // Simulated grooves
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .align(Alignment.Center)
                    .border(1.dp, Color(0x1AFFFFFF), CircleShape)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.Center)
                    .border(1.dp, Color(0x15FFFFFF), CircleShape)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(0.55f)
                    .align(Alignment.Center)
                    .border(1.dp, Color(0x1AFFFFFF), CircleShape)
            )
        }

        // Center Album/Station Art Crest
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(station.accentColorHex),
                            Color(0xFF8B0000)
                        )
                    )
                )
                .border(3.dp, Color(0xFFFFD166), CircleShape)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(34.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = station.iconEmoji,
                    fontSize = 44.sp
                )
            }
        }
    }
}
