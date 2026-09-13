package com.example.model

data class RadioStation(
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
    val isFavorite: Boolean = false,
    val bitrate: String = "128 kbps"
)

enum class PlaybackStatus {
    IDLE,
    CONNECTING,
    PLAYING,
    PAUSED,
    ERROR
}

data class PlayerState(
    val currentStation: RadioStation? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val errorMessage: String? = null,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val sleepTimerMinutesRemaining: Int? = null,
    val sleepTimerSecondsRemaining: Long? = null,
    val sleepTimerTotalSeconds: Long? = null,
    val sleepTimerSelectedMinutes: Int? = null,
    val sleepTimerActive: Boolean = false
)
