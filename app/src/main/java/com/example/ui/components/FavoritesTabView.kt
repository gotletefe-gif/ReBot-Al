package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun FavoritesTabView(
    favorites: List<RadioStation>,
    playerState: PlayerState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStationClick: (RadioStation) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onPlayShuffle: () -> Unit,
    onClearAllFavorites: () -> Unit,
    onNavigateToStations: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_tab_view")
    ) {
        // Search bar for favorites
        StationSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            matchedCount = favorites.size,
            placeholderText = "Favorilerimde ara...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Favorites Header & Room DB badge
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, CrimsonPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Storage,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ROOM DB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                            }
                        }

                        Text(
                            text = "${favorites.size} İstasyon Kayıtlı",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Yerel veritabanında çevrimdışı saklanan favorileriniz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                if (favorites.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPlayShuffle,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("shuffle_favorites_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Karışık Çal",
                                tint = AmberSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_all_favorites_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Favorileri Temizle",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        if (favorites.isEmpty()) {
            EmptyFavoritesState(
                isSearchActive = searchQuery.isNotBlank(),
                onClearSearch = { onSearchQueryChange("") },
                onNavigateToStations = onNavigateToStations,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("favorites_station_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = if (playerState.currentStation != null) 90.dp else 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { station ->
                    StationCard(
                        station = station.copy(isFavorite = true),
                        isCurrentStation = playerState.currentStation?.id == station.id,
                        playbackStatus = playerState.status,
                        onStationClick = {
                            if (playerState.currentStation?.id == station.id) {
                                onTogglePlayPause()
                            } else {
                                onStationClick(station)
                            }
                        },
                        onToggleFavorite = { onToggleFavorite(station) }
                    )
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Favorileri Temizle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Room veritabanındaki tüm favori radyo kayıtları silinecektir. Devam etmek istiyor musunuz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllFavorites()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_favorites_button")
                ) {
                    Text(
                        text = "Evet, Temizle",
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false }
                ) {
                    Text(
                        text = "İptal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun EmptyFavoritesState(
    isSearchActive: Boolean,
    onClearSearch: () -> Unit,
    onNavigateToStations: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(24.dp)
            .testTag("empty_favorites_state"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CrimsonPrimary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isSearchActive) "Aramanıza Uygun Favori Bulunamadı" else "Henüz Favori Radyo Eklenmedi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSearchActive) {
                        "Arama kriterinize uyan bir favori bulunamadı. Aramayı temizlemeyi deneyin."
                    } else {
                        "Sevdiğiniz radyo kanallarını hızlıca dinlemek için istasyon kartlarındaki kalp (❤️) butonuna dokunun. Favorileriniz yerel Room veritabanında güvenle saklanır."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isSearchActive) {
                    OutlinedButton(
                        onClick = onClearSearch,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CrimsonPrimary)
                    ) {
                        Text(
                            text = "Aramayı Temizle",
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Button(
                        onClick = onNavigateToStations,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("explore_stations_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Radio,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Radyoları Keşfet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
