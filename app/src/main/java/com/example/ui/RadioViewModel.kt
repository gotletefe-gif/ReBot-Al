package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultStations
import com.example.data.PreferencesManager
import com.example.data.local.FavoriteStationsRepository
import com.example.data.local.RadioDatabase
import com.example.data.local.RecentStationEntity
import com.example.model.AudioQualityPreset
import com.example.model.EqualizerPreset
import com.example.model.EqualizerSettings
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.example.player.RadioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RadioTab {
    STATIONS,
    FAVORITES
}

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RadioDatabase.getDatabase(application)
    private val favoritesRepository = FavoriteStationsRepository(database.favoriteStationDao())
    private val recentStationDao = database.recentStationDao()
    private val prefsManager = PreferencesManager(application)
    private val playerManager = RadioPlayerManager(application)

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    private val _currentTab = MutableStateFlow(RadioTab.STATIONS)
    val currentTab: StateFlow<RadioTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Observes favorite stations from Room local database
    val favoriteStations: StateFlow<List<RadioStation>> = favoritesRepository.allFavorites
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Observes favorite IDs from Room local database
    val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, prefsManager.getFavoriteIds())

    // Observes favorite count from Room
    val favoriteCount: StateFlow<Int> = favoritesRepository.favoriteCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, prefsManager.getFavoriteIds().size)

    // Observes the last 5 recently played stations from Room local database
    val recentStations: StateFlow<List<RadioStation>> = combine(
        recentStationDao.getRecentStations(),
        favoriteIds
    ) { recents, favs ->
        recents.take(5).map { it.toRadioStation(isFavorite = favs.contains(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _customStations = MutableStateFlow(prefsManager.getCustomStations())
    val customStations = _customStations.asStateFlow()

    private val _showFullPlayer = MutableStateFlow(false)
    val showFullPlayer = _showFullPlayer.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog = _showSleepTimerDialog.asStateFlow()

    private val _showAddStationDialog = MutableStateFlow(false)
    val showAddStationDialog = _showAddStationDialog.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(prefsManager.getEqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private val _showEqualizerDialog = MutableStateFlow(false)
    val showEqualizerDialog = _showEqualizerDialog.asStateFlow()

    private val _themeMode = MutableStateFlow(prefsManager.getThemeMode())
    val themeMode: StateFlow<com.example.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefsManager.isDynamicColor())
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    // All available stations combined
    private val _allStations = MutableStateFlow<List<RadioStation>>(emptyList())

    init {
        loadStations()
        playerManager.applyEqualizerSettings(_equalizerSettings.value)

        // Sync legacy SharedPreferences favorites to Room local database if any
        viewModelScope.launch {
            val legacyFavs = prefsManager.getFavoriteIds()
            if (legacyFavs.isNotEmpty()) {
                val allStationsMap = (DefaultStations.list + prefsManager.getCustomStations()).associateBy { it.id }
                for (favId in legacyFavs) {
                    val station = allStationsMap[favId]
                    if (station != null) {
                        favoritesRepository.addFavorite(station)
                    }
                }
            }
        }

        // Sync recent stations from preferences or last played to Room if empty
        viewModelScope.launch {
            if (recentStationDao.getRecentCount() == 0) {
                val legacyRecentIds = prefsManager.getRecentIds()
                val allStationsMap = (DefaultStations.list + prefsManager.getCustomStations()).associateBy { it.id }
                var timestamp = System.currentTimeMillis() - 60000
                for (id in legacyRecentIds.reversed()) {
                    val st = allStationsMap[id]
                    if (st != null) {
                        recentStationDao.insertRecent(RecentStationEntity.fromRadioStation(st, timestamp))
                        timestamp += 1000
                    }
                }
                val lastPlayed = prefsManager.getLastPlayedStationId()
                if (lastPlayed != null && recentStationDao.getRecentCount() == 0) {
                    val st = allStationsMap[lastPlayed]
                    if (st != null) {
                        recentStationDao.insertRecent(RecentStationEntity.fromRadioStation(st, System.currentTimeMillis()))
                    }
                }
            }
        }

        // Restore last played station as initial display if present
        val lastPlayedId = prefsManager.getLastPlayedStationId()
        if (lastPlayedId != null) {
            val station = _allStations.value.find { it.id == lastPlayedId }
            if (station != null) {
                // Initialize current station without autoplay
            }
        }
    }

    private fun loadStations() {
        val favs = prefsManager.getFavoriteIds()
        val custom = prefsManager.getCustomStations()
        val combined = (custom + DefaultStations.list).distinctBy { it.id }.map { station ->
            station.copy(isFavorite = favs.contains(station.id))
        }
        _allStations.value = combined
    }

    val filteredStations: StateFlow<List<RadioStation>> = combine(
        _allStations,
        _searchQuery,
        _selectedCategory,
        favoriteIds
    ) { stations, query, category, favs ->
        stations.map { it.copy(isFavorite = favs.contains(it.id)) }.filter { station ->
            val matchesCategory = when (category) {
                "Tümü" -> true
                "Favoriler" -> favs.contains(station.id)
                else -> station.category.equals(category, ignoreCase = true)
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val cleanQuery = query.trim()
                station.name.matchesSearch(cleanQuery) ||
                    station.frequency.matchesSearch(cleanQuery) ||
                    station.city.matchesSearch(cleanQuery) ||
                    station.category.matchesSearch(cleanQuery) ||
                    station.description.matchesSearch(cleanQuery)
            }

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _allStations.value)

    val filteredFavoriteStations: StateFlow<List<RadioStation>> = combine(
        favoriteStations,
        _searchQuery
    ) { favs, query ->
        if (query.isBlank()) {
            favs
        } else {
            val cleanQuery = query.trim()
            favs.filter { station ->
                station.name.matchesSearch(cleanQuery) ||
                    station.frequency.matchesSearch(cleanQuery) ||
                    station.city.matchesSearch(cleanQuery) ||
                    station.category.matchesSearch(cleanQuery) ||
                    station.description.matchesSearch(cleanQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectTab(tab: RadioTab) {
        _currentTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun playStation(station: RadioStation) {
        playerManager.playStation(station)
        prefsManager.addRecentStation(station.id)
        prefsManager.setLastPlayedStationId(station.id)
        viewModelScope.launch {
            recentStationDao.insertRecent(
                RecentStationEntity.fromRadioStation(station, System.currentTimeMillis())
            )
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun pause() {
        playerManager.pause()
    }

    fun resume() {
        playerManager.resume()
    }

    fun stop() {
        playerManager.stop()
    }

    fun playNextStation() {
        val currentList = if (_currentTab.value == RadioTab.FAVORITES) {
            filteredFavoriteStations.value.ifEmpty { favoriteStations.value }
        } else {
            filteredStations.value.ifEmpty { _allStations.value }
        }
        if (currentList.isEmpty()) return

        val current = playerState.value.currentStation
        val currentIndex = currentList.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex in 0 until currentList.lastIndex) currentIndex + 1 else 0
        playStation(currentList[nextIndex])
    }

    fun playPreviousStation() {
        val currentList = if (_currentTab.value == RadioTab.FAVORITES) {
            filteredFavoriteStations.value.ifEmpty { favoriteStations.value }
        } else {
            filteredStations.value.ifEmpty { _allStations.value }
        }
        if (currentList.isEmpty()) return

        val current = playerState.value.currentStation
        val currentIndex = currentList.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else currentList.lastIndex
        playStation(currentList[prevIndex])
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            val currentlyFav = favoriteIds.value.contains(station.id)
            favoritesRepository.toggleFavorite(station, currentlyFav)
            prefsManager.toggleFavorite(station.id)
            _allStations.update { list ->
                list.map { if (it.id == station.id) it.copy(isFavorite = !currentlyFav) else it }
            }
        }
    }

    fun playRandomFavorite() {
        val favs = favoriteStations.value
        if (favs.isNotEmpty()) {
            playStation(favs.random())
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            favoritesRepository.clearAllFavorites()
            val oldFavs = prefsManager.getFavoriteIds()
            for (id in oldFavs) {
                prefsManager.toggleFavorite(id)
            }
            _allStations.update { list ->
                list.map { it.copy(isFavorite = false) }
            }
        }
    }

    fun addCustomStation(name: String, streamUrl: String, frequency: String, category: String) {
        if (name.isBlank() || streamUrl.isBlank()) return
        val newStation = RadioStation(
            id = "custom_${System.currentTimeMillis()}",
            name = name.trim(),
            category = category.ifBlank { "Özel" },
            streamUrl = streamUrl.trim(),
            frequency = if (frequency.isNotBlank()) frequency.trim() else "Web",
            city = "Özel İstasyon",
            description = "Kullanıcı Tarafından Eklendi",
            accentColorHex = 0xFFDC2626,
            iconEmoji = "📻",
            isCustom = true,
            isFavorite = false
        )
        prefsManager.saveCustomStation(newStation)
        _customStations.value = prefsManager.getCustomStations()
        loadStations()
        playStation(newStation)
    }

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
    }

    fun toggleMute() {
        playerManager.toggleMute()
    }

    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
        _showSleepTimerDialog.value = false
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
        _showSleepTimerDialog.value = false
    }

    fun setShowFullPlayer(show: Boolean) {
        _showFullPlayer.value = show
    }

    fun setShowSleepTimerDialog(show: Boolean) {
        _showSleepTimerDialog.value = show
    }

    fun setShowAddStationDialog(show: Boolean) {
        _showAddStationDialog.value = show
    }

    fun setShowEqualizerDialog(show: Boolean) {
        _showEqualizerDialog.value = show
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        val updated = _equalizerSettings.value.copy(isEnabled = enabled)
        updateEqualizerInternal(updated)
    }

    fun setBass(bassDb: Int) {
        val clamped = bassDb.coerceIn(-10, 10)
        val updated = _equalizerSettings.value.copy(
            bassDb = clamped,
            activePreset = EqualizerPreset.CUSTOM
        )
        updateEqualizerInternal(updated)
    }

    fun setMid(midDb: Int) {
        val clamped = midDb.coerceIn(-10, 10)
        val updated = _equalizerSettings.value.copy(
            midDb = clamped,
            activePreset = EqualizerPreset.CUSTOM
        )
        updateEqualizerInternal(updated)
    }

    fun setTreble(trebleDb: Int) {
        val clamped = trebleDb.coerceIn(-10, 10)
        val updated = _equalizerSettings.value.copy(
            trebleDb = clamped,
            activePreset = EqualizerPreset.CUSTOM
        )
        updateEqualizerInternal(updated)
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        val updated = if (preset == EqualizerPreset.CUSTOM) {
            _equalizerSettings.value.copy(activePreset = EqualizerPreset.CUSTOM)
        } else {
            _equalizerSettings.value.copy(
                bassDb = preset.bassDb,
                midDb = preset.midDb,
                trebleDb = preset.trebleDb,
                activePreset = preset
            )
        }
        updateEqualizerInternal(updated)
    }

    fun selectQualityPreset(quality: AudioQualityPreset) {
        val updated = _equalizerSettings.value.copy(qualityPreset = quality)
        updateEqualizerInternal(updated)
    }

    fun resetEqualizer() {
        val updated = EqualizerSettings(
            isEnabled = true,
            bassDb = 0,
            midDb = 0,
            trebleDb = 0,
            activePreset = EqualizerPreset.FLAT,
            qualityPreset = _equalizerSettings.value.qualityPreset
        )
        updateEqualizerInternal(updated)
    }

    private fun updateEqualizerInternal(settings: EqualizerSettings) {
        _equalizerSettings.value = settings
        prefsManager.saveEqualizerSettings(settings)
        playerManager.applyEqualizerSettings(settings)
    }

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        _themeMode.value = mode
        prefsManager.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
        prefsManager.setDynamicColor(enabled)
    }

    fun setShowThemeDialog(show: Boolean) {
        _showThemeDialog.value = show
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

private fun String.matchesSearch(query: String): Boolean {
    if (this.contains(query, ignoreCase = true)) return true
    val normalizedTarget = this.normalizeTurkish()
    val normalizedQuery = query.normalizeTurkish()
    return normalizedTarget.contains(normalizedQuery, ignoreCase = true)
}

private fun String.normalizeTurkish(): String {
    return this.lowercase()
        .replace('ı', 'i')
        .replace('İ', 'i')
        .replace('ğ', 'g')
        .replace('Ğ', 'g')
        .replace('ü', 'u')
        .replace('Ü', 'u')
        .replace('ş', 's')
        .replace('Ş', 's')
        .replace('ö', 'o')
        .replace('Ö', 'o')
        .replace('ç', 'c')
        .replace('Ç', 'c')
}
