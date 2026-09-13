package com.example.model

enum class AudioQualityPreset(
    val title: String,
    val description: String,
    val badge: String,
    val estimatedBitrate: String
) {
    HIGH(
        title = "Yüksek Kalite (HD)",
        description = "Maksimum berraklık, geniş dinamik aralık ve zengin ses deneyimi",
        badge = "HD",
        estimatedBitrate = "192-320 kbps"
    ),
    STANDARD(
        title = "Standart Dengeli",
        description = "Optimum netlik ve stabil veri akışı dengesi",
        badge = "128k",
        estimatedBitrate = "128 kbps"
    ),
    DATA_SAVER(
        title = "Veri Tasarrufu",
        description = "Mobil veri kotasını koruyan kesintisiz düşük bant genişliği",
        badge = "Tasarruf",
        estimatedBitrate = "64 kbps"
    )
}

enum class EqualizerPreset(
    val title: String,
    val iconEmoji: String,
    val bassDb: Int,
    val midDb: Int,
    val trebleDb: Int,
    val description: String
) {
    FLAT(
        title = "Düz (Doğal)",
        iconEmoji = "⚖️",
        bassDb = 0,
        midDb = 0,
        trebleDb = 0,
        description = "Radyo istasyonunun orijinal stüdyo yayını"
    ),
    BASS_BOOST(
        title = "Bas Güçlendirici",
        iconEmoji = "🔊",
        bassDb = 6,
        midDb = 1,
        trebleDb = -1,
        description = "Güçlü ritimler ve derin düşük frekanslar"
    ),
    TREBLE_BOOST(
        title = "Tiz & Parlaklık",
        iconEmoji = "✨",
        bassDb = -1,
        midDb = 1,
        trebleDb = 6,
        description = "Net ziller, berrak enstrümanlar ve parlak ses"
    ),
    VOCAL(
        title = "Vokal & Konuşma",
        iconEmoji = "🎙️",
        bassDb = -2,
        midDb = 5,
        trebleDb = 2,
        description = "Haber, spor anlatımı, podcast ve insan sesi netliği"
    ),
    POP_ROCK(
        title = "Pop & Rock",
        iconEmoji = "🎸",
        bassDb = 5,
        midDb = 0,
        trebleDb = 4,
        description = "Vurucu baslar ve parlak gitarlar ile enerjik ses"
    ),
    ACOUSTIC(
        title = "Akustik & Klasik",
        iconEmoji = "🎻",
        bassDb = 2,
        midDb = 3,
        trebleDb = 3,
        description = "Zengin orta tonlar ve akustik enstrüman dengesi"
    ),
    CUSTOM(
        title = "Özel Ayar",
        iconEmoji = "🎚️",
        bassDb = 0,
        midDb = 0,
        trebleDb = 0,
        description = "Kullanıcı tarafından kişiselleştirilmiş frekanslar"
    )
}

data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val bassDb: Int = 0, // -10 .. +10 dB
    val midDb: Int = 0,  // -10 .. +10 dB
    val trebleDb: Int = 0, // -10 .. +10 dB
    val activePreset: EqualizerPreset = EqualizerPreset.FLAT,
    val qualityPreset: AudioQualityPreset = AudioQualityPreset.HIGH
) {
    val isCustomized: Boolean
        get() = bassDb != 0 || midDb != 0 || trebleDb != 0 || activePreset != EqualizerPreset.FLAT
}
