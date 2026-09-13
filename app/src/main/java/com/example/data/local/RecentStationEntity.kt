package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RadioStation

@Entity(tableName = "recent_stations")
data class RecentStationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val streamUrl: String,
    val backupStreamUrl: String? = null,
    val frequency: String,
    val city: String = "Ulusal",
    val description: String = "",
    val accentColorHex: Long = 0xFFE50914,
    val iconEmoji: String = "📻",
    val isCustom: Boolean = false,
    val bitrate: String = "128 kbps",
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toRadioStation(isFavorite: Boolean = false): RadioStation {
        return RadioStation(
            id = id,
            name = name,
            category = category,
            streamUrl = streamUrl,
            backupStreamUrl = backupStreamUrl,
            frequency = frequency,
            city = city,
            description = description,
            accentColorHex = accentColorHex,
            iconEmoji = iconEmoji,
            isCustom = isCustom,
            isFavorite = isFavorite,
            bitrate = bitrate
        )
    }

    companion object {
        fun fromRadioStation(station: RadioStation, timestamp: Long = System.currentTimeMillis()): RecentStationEntity {
            return RecentStationEntity(
                id = station.id,
                name = station.name,
                category = station.category,
                streamUrl = station.streamUrl,
                backupStreamUrl = station.backupStreamUrl,
                frequency = station.frequency,
                city = station.city,
                description = station.description,
                accentColorHex = station.accentColorHex,
                iconEmoji = station.iconEmoji,
                isCustom = station.isCustom,
                bitrate = station.bitrate,
                playedAt = timestamp
            )
        }
    }
}
