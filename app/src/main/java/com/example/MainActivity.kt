package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DefaultStations
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.ui.RadioTab
import com.example.ui.RadioViewModel
import com.example.ui.components.AddStationDialog
import com.example.ui.components.FavoritesTabView
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.LiveEqualizerWave
import com.example.ui.components.MiniPlayer
import com.example.ui.components.RadioNavTabBar
import com.example.ui.components.RecentlyPlayedSection
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.StationCard
import com.example.ui.components.StationSearchBar
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RadioViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()

            MyApplicationTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                RadioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RadioApp(viewModel: RadioViewModel = viewModel()) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val stations by viewModel.filteredStations.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()
    val filteredFavoriteStations by viewModel.filteredFavoriteStations.collectAsStateWithLifecycle()
    val recentStations by viewModel.recentStations.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showFullPlayer by viewModel.showFullPlayer.collectAsStateWithLifecycle()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsStateWithLifecycle()
    val showAddStationDialog by viewModel.showAddStationDialog.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val showThemeDialog by viewModel.showThemeDialog.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* notification permission result handled by OS */ }

        LaunchedEffect(Unit) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Top Header Bar
                RadioHeader(
                    onOpenAddStation = { viewModel.setShowAddStationDialog(true) },
                    onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
                    onOpenThemeSettings = { viewModel.setShowThemeDialog(true) },
                    themeMode = themeMode,
                    isTimerActive = playerState.sleepTimerActive,
                    timerMinutes = playerState.sleepTimerMinutesRemaining
                )

                // Dedicated Tab Bar: Tüm Radyolar / Favoriler (Room DB backed)
                RadioNavTabBar(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    favoriteCount = favoriteCount,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                if (currentTab == RadioTab.FAVORITES) {
                    // Dedicated Favorites View powered by Room Database
                    FavoritesTabView(
                        favorites = filteredFavoriteStations,
                        playerState = playerState,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onStationClick = { station ->
                            viewModel.playStation(station)
                        },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onToggleFavorite = { station ->
                            viewModel.toggleFavorite(station)
                        },
                        onPlayShuffle = { viewModel.playRandomFavorite() },
                        onClearAllFavorites = { viewModel.clearAllFavorites() },
                        onNavigateToStations = { viewModel.selectTab(RadioTab.STATIONS) }
                    )
                } else {
                    // Search Bar at the top of the radio list
                    StationSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        matchedCount = stations.size,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    // Recently Played Section (Tracks last 5 listened stations for quick access)
                    if (searchQuery.isBlank() && recentStations.isNotEmpty()) {
                        RecentlyPlayedSection(
                            recentStations = recentStations,
                            currentStation = playerState.currentStation,
                            playbackStatus = playerState.status,
                            onStationClick = { station ->
                                if (playerState.currentStation?.id == station.id) {
                                    viewModel.togglePlayPause()
                                } else {
                                    viewModel.playStation(station)
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Featured Hero Banner (Shown when not searching and on "Tümü" category)
                    if (searchQuery.isBlank() && selectedCategory == "Tümü") {
                        FeaturedStationBanner(
                            onPlayClick = {
                                val featured = DefaultStations.list.firstOrNull()
                                if (featured != null) {
                                    viewModel.playStation(featured)
                                }
                            },
                            isPlaying = playerState.currentStation?.id == "trt_fm" && playerState.status == PlaybackStatus.PLAYING
                        )
                    }

                    // Category Chips Selector
                    CategoryChipsRow(
                        categories = DefaultStations.categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            if (category == "Favoriler") {
                                viewModel.selectTab(RadioTab.FAVORITES)
                            } else {
                                viewModel.onCategorySelected(category)
                            }
                        }
                    )

                    // Station Count & Status label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Arama Sonuçları (\"$searchQuery\")"
                            } else {
                                "$selectedCategory Radyoları"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${stations.size} İstasyon",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stations List
                    if (stations.isEmpty()) {
                        EmptyStationsView(
                            category = selectedCategory,
                            query = searchQuery,
                            onResetFilter = {
                                viewModel.onCategorySelected("Tümü")
                                viewModel.onSearchQueryChanged("")
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("station_list"),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = if (playerState.currentStation != null) 90.dp else 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = stations,
                                key = { it.id }
                            ) { station ->
                                StationCard(
                                    station = station,
                                    isCurrentStation = playerState.currentStation?.id == station.id,
                                    playbackStatus = playerState.status,
                                    onStationClick = {
                                        if (playerState.currentStation?.id == station.id) {
                                            viewModel.togglePlayPause()
                                        } else {
                                            viewModel.playStation(station)
                                        }
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(station) }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Mini Player
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                MiniPlayer(
                    playerState = playerState,
                    onMiniPlayerClick = { viewModel.setShowFullPlayer(true) },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.playNextStation() },
                    onPreviousClick = { viewModel.playPreviousStation() }
                )
            }
        }
    }

    // Full Player Modal Bottom Sheet
    if (showFullPlayer && playerState.currentStation != null) {
        FullPlayerSheet(
            playerState = playerState,
            onDismiss = { viewModel.setShowFullPlayer(false) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onStopClick = { viewModel.stop() },
            onNextClick = { viewModel.playNextStation() },
            onPreviousClick = { viewModel.playPreviousStation() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
            onVolumeChange = { viewModel.setVolume(it) },
            onToggleMute = { viewModel.toggleMute() },
            onSetSleepTimer = { viewModel.startSleepTimer(it) },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() }
        )
    }

    // Sleep Timer Dialog (Supports 15, 30, 45, 60 min countdowns)
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            isActive = playerState.sleepTimerActive,
            remainingMinutes = playerState.sleepTimerMinutesRemaining,
            remainingSeconds = playerState.sleepTimerSecondsRemaining,
            totalSeconds = playerState.sleepTimerTotalSeconds,
            selectedMinutes = playerState.sleepTimerSelectedMinutes,
            onSetTimer = { viewModel.startSleepTimer(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { viewModel.setShowSleepTimerDialog(false) }
        )
    }

    // Add Custom Station Dialog
    if (showAddStationDialog) {
        AddStationDialog(
            onAddStation = { name, url, freq, cat ->
                viewModel.addCustomStation(name, url, freq, cat)
            },
            onDismiss = { viewModel.setShowAddStationDialog(false) }
        )
    }

    // Theme Selection Dialog (System Default, Light, Dark + Dynamic Material You color)
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = themeMode,
            dynamicColor = dynamicColor,
            onSelectMode = { viewModel.setThemeMode(it) },
            onToggleDynamicColor = { viewModel.setDynamicColor(it) },
            onDismiss = { viewModel.setShowThemeDialog(false) }
        )
    }
}

@Composable
fun RadioHeader(
    onOpenAddStation: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    themeMode: AppThemeMode,
    isTimerActive: Boolean,
    timerMinutes: Int?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CrimsonPrimary, CrimsonDark)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Radio,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Radyo Türk",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LiveGreen)
                    )
                }
                Text(
                    text = "Canlı Radyo Dinle",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Icons (Theme, Timer, Add)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Theme Mode Toggle / Settings
            IconButton(
                onClick = onOpenThemeSettings,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("header_theme_toggle")
            ) {
                val themeIcon = when (themeMode) {
                    AppThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                    AppThemeMode.LIGHT -> Icons.Filled.LightMode
                    AppThemeMode.DARK -> Icons.Filled.DarkMode
                }
                Icon(
                    imageVector = themeIcon,
                    contentDescription = "Tema Ayarları (${themeMode.title})",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Sleep Timer
            IconButton(
                onClick = onOpenSleepTimer,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("header_sleep_timer")
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = "Uyku Zamanlayıcısı",
                        tint = if (isTimerActive) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    if (isTimerActive && timerMinutes != null) {
                        Surface(
                            shape = CircleShape,
                            color = AmberSecondary,
                            modifier = Modifier
                                .size(13.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "$timerMinutes",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Add Custom Station
            IconButton(
                onClick = onOpenAddStation,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("header_add_station")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Radyo Ekle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedStationBanner(
    onPlayClick: () -> Unit,
    isPlaying: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPlayClick)
            .testTag("featured_banner"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.4f)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CrimsonDark,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CrimsonPrimary
                    ) {
                        Text(
                            text = "GÜNÜN ÖNE ÇIKANI",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TRT FM • 91.4 FM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Türkiye'nin Sesi • Kesintisiz Türkçe Müzik",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD8D8)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = CircleShape,
                    color = if (isPlaying) LiveGreen else CrimsonLight,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isPlaying) {
                            LiveEqualizerWave(
                                isPlaying = true,
                                barCount = 3,
                                barWidth = 3.dp,
                                maxHeight = 18.dp,
                                barColor = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Hızlı Başlat",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_chips_row"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = if (category == "Favoriler") {
                    {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else CrimsonPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrimsonPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("category_chip_$category")
            )
        }
    }
}

@Composable
fun EmptyStationsView(
    category: String,
    query: String,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (category == "Favoriler") Icons.Filled.Favorite else Icons.Filled.Radio,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (category == "Favoriler") {
                "Henüz favori radyo eklemediniz"
            } else if (query.isNotBlank()) {
                "\"$query\" ile eşleşen radyo bulunamadı"
            } else {
                "Bu kategoride radyo bulunamadı"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (category == "Favoriler") {
                "Beğendiğiniz radyoların yanındaki kalp simgesine dokunarak favorilerinize ekleyebilirsiniz."
            } else {
                "Filtreleri sıfırlayarak tüm canlı radyo istasyonlarını görüntüleyebilirsiniz."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onResetFilter,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("reset_filter_button")
        ) {
            Text("Tüm Radyoları Göster", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// Greeting helper for screenshot test backwards compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Radyo Türk - Canlı $name", modifier = modifier)
}
