package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AudioQualityPreset
import com.example.model.EqualizerPreset
import com.example.model.EqualizerSettings
import com.example.model.RadioStation
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("radyo_turk_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "key_favorite_station_ids"
        private const val KEY_RECENTS = "key_recent_station_ids"
        private const val KEY_CUSTOM_STATIONS = "key_custom_stations"
        private const val KEY_LAST_PLAYED = "key_last_played_id"
        private const val KEY_EQ_ENABLED = "key_eq_enabled"
        private const val KEY_EQ_BASS = "key_eq_bass"
        private const val KEY_EQ_MID = "key_eq_mid"
        private const val KEY_EQ_TREBLE = "key_eq_treble"
        private const val KEY_EQ_PRESET = "key_eq_preset"
        private const val KEY_AUDIO_QUALITY = "key_audio_quality"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"
    }

    fun getThemeMode(): com.example.ui.theme.AppThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, com.example.ui.theme.AppThemeMode.SYSTEM.name)
        return try {
            com.example.ui.theme.AppThemeMode.valueOf(saved ?: com.example.ui.theme.AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            com.example.ui.theme.AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun isDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(stationId: String): Boolean {
        val favorites = getFavoriteIds().toMutableSet()
        val isNowFav = if (favorites.contains(stationId)) {
            favorites.remove(stationId)
            false
        } else {
            favorites.add(stationId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
        return isNowFav
    }

    fun addRecentStation(stationId: String) {
        val recents = getRecentIds().toMutableList()
        recents.remove(stationId)
        recents.add(0, stationId)
        if (recents.size > 20) {
            recents.removeAt(recents.size - 1)
        }
        prefs.edit().putString(KEY_RECENTS, recents.joinToString(",")).apply()
    }

    fun getRecentIds(): List<String> {
        val raw = prefs.getString(KEY_RECENTS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun setLastPlayedStationId(stationId: String) {
        prefs.edit().putString(KEY_LAST_PLAYED, stationId).apply()
    }

    fun getLastPlayedStationId(): String? {
        return prefs.getString(KEY_LAST_PLAYED, null)
    }

    fun saveCustomStation(station: RadioStation) {
        val current = getCustomStations().toMutableList()
        current.removeAll { it.id == station.id }
        current.add(0, station)

        val jsonArray = JSONArray()
        for (item in current) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("category", item.category)
            obj.put("streamUrl", item.streamUrl)
            obj.put("frequency", item.frequency)
            obj.put("city", item.city)
            obj.put("description", item.description)
            obj.put("accentColorHex", item.accentColorHex)
            obj.put("iconEmoji", item.iconEmoji)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_STATIONS, jsonArray.toString()).apply()
    }

    fun getCustomStations(): List<RadioStation> {
        val raw = prefs.getString(KEY_CUSTOM_STATIONS, null) ?: return emptyList()
        val list = mutableListOf<RadioStation>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RadioStation(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = obj.optString("category", "Özel"),
                        streamUrl = obj.getString("streamUrl"),
                        frequency = obj.optString("frequency", "Web"),
                        city = obj.optString("city", "Yerel"),
                        description = obj.optString("description", "Kullanıcı İstasyonu"),
                        accentColorHex = obj.optLong("accentColorHex", 0xFFE50914),
                        iconEmoji = obj.optString("iconEmoji", "📻"),
                        isCustom = true
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    fun getEqualizerSettings(): EqualizerSettings {
        val isEnabled = prefs.getBoolean(KEY_EQ_ENABLED, true)
        val bassDb = prefs.getInt(KEY_EQ_BASS, 0)
        val midDb = prefs.getInt(KEY_EQ_MID, 0)
        val trebleDb = prefs.getInt(KEY_EQ_TREBLE, 0)
        val presetName = prefs.getString(KEY_EQ_PRESET, EqualizerPreset.FLAT.name)
        val qualityName = prefs.getString(KEY_AUDIO_QUALITY, AudioQualityPreset.HIGH.name)

        val activePreset = try {
            EqualizerPreset.valueOf(presetName ?: EqualizerPreset.FLAT.name)
        } catch (_: Exception) {
            EqualizerPreset.FLAT
        }

        val qualityPreset = try {
            AudioQualityPreset.valueOf(qualityName ?: AudioQualityPreset.HIGH.name)
        } catch (_: Exception) {
            AudioQualityPreset.HIGH
        }

        return EqualizerSettings(
            isEnabled = isEnabled,
            bassDb = bassDb,
            midDb = midDb,
            trebleDb = trebleDb,
            activePreset = activePreset,
            qualityPreset = qualityPreset
        )
    }

    fun saveEqualizerSettings(settings: EqualizerSettings) {
        prefs.edit()
            .putBoolean(KEY_EQ_ENABLED, settings.isEnabled)
            .putInt(KEY_EQ_BASS, settings.bassDb)
            .putInt(KEY_EQ_MID, settings.midDb)
            .putInt(KEY_EQ_TREBLE, settings.trebleDb)
            .putString(KEY_EQ_PRESET, settings.activePreset.name)
            .putString(KEY_AUDIO_QUALITY, settings.qualityPreset.name)
            .apply()
    }
}
