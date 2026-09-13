package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.example.R
import com.example.model.EqualizerSettings
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RadioPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentEqSettings: EqualizerSettings = EqualizerSettings()

    private var isUsingBackupStream = false
    private var activeStation: RadioStation? = null

    companion object {
        private const val TAG = "RadioPlaybackService"
        const val CHANNEL_ID = "radio_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_TOGGLE = "com.example.action.TOGGLE"
        const val ACTION_SET_VOLUME = "com.example.action.SET_VOLUME"

        const val EXTRA_STATION_ID = "extra_station_id"
        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_BACKUP_STREAM_URL = "extra_backup_stream_url"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_FREQUENCY = "extra_frequency"
        const val EXTRA_CITY = "extra_city"
        const val EXTRA_VOLUME = "extra_volume"

        private val _playerState = MutableStateFlow(PlayerState())
        val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

        private var instance: RadioPlaybackService? = null

        fun startPlay(context: Context, station: RadioStation) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_STATION_ID, station.id)
                putExtra(EXTRA_STATION_NAME, station.name)
                putExtra(EXTRA_STREAM_URL, station.streamUrl)
                putExtra(EXTRA_BACKUP_STREAM_URL, station.backupStreamUrl)
                putExtra(EXTRA_CATEGORY, station.category)
                putExtra(EXTRA_FREQUENCY, station.frequency)
                putExtra(EXTRA_CITY, station.city)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PLAY
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun togglePlayPause(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_TOGGLE
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setVolume(context: Context, volume: Float) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_SET_VOLUME
                putExtra(EXTRA_VOLUME, volume)
            }
            context.startService(intent)
        }

        fun applyEqualizerSettings(settings: EqualizerSettings) {
            instance?.applyEqualizerInternal(settings)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        initExoPlayer()
        initMediaSession()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.radio_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.radio_notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    @OptIn(UnstableApi::class)
    private fun initExoPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("RadyoTurk/1.0 (Linux; Android)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        val exo = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()

        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState, playWhenReady=${exo.playWhenReady}")
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.update { it.copy(status = PlaybackStatus.CONNECTING) }
                        updateForegroundNotification()
                    }
                    Player.STATE_READY -> {
                        val isPlaying = exo.playWhenReady && exo.isPlaying
                        val status = if (isPlaying) PlaybackStatus.PLAYING else if (exo.playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                        _playerState.update { it.copy(status = status, errorMessage = null) }
                        applyEqualizerInternal(currentEqSettings)
                        updateForegroundNotification()
                    }
                    Player.STATE_ENDED -> {
                        _playerState.update { it.copy(status = PlaybackStatus.IDLE) }
                        updateForegroundNotification()
                    }
                    Player.STATE_IDLE -> {
                        // Handled in error or stopped
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                if (isPlaying) {
                    _playerState.update { it.copy(status = PlaybackStatus.PLAYING, errorMessage = null) }
                    applyEqualizerInternal(currentEqSettings)
                } else {
                    if (exo.playbackState == Player.STATE_BUFFERING) {
                        _playerState.update { it.copy(status = PlaybackStatus.CONNECTING) }
                    } else if (exo.playbackState == Player.STATE_READY) {
                        _playerState.update { it.copy(status = PlaybackStatus.PAUSED) }
                    }
                }
                updateForegroundNotification()
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "onPlayerError: ${error.message}", error)
                val station = activeStation
                if (!isUsingBackupStream && station?.backupStreamUrl != null) {
                    Log.d(TAG, "Primary stream failed, attempting backup stream")
                    isUsingBackupStream = true
                    playStreamInternal(station.backupStreamUrl, station)
                } else {
                    _playerState.update {
                        it.copy(
                            status = PlaybackStatus.ERROR,
                            errorMessage = "Yayın bağlantısı kurulamadı: ${error.localizedMessage ?: "Bağlantı hatası"}"
                        )
                    }
                    updateForegroundNotification()
                }
            }
        })

        player = exo
    }

    @OptIn(UnstableApi::class)
    private fun initMediaSession() {
        val exo = player ?: return

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopButton = CommandButton.Builder()
            .setDisplayName(getString(R.string.action_stop))
            .setIconResId(R.drawable.ic_stop)
            .setSessionCommand(SessionCommand(ACTION_STOP, Bundle.EMPTY))
            .build()

        mediaSession = MediaSession.Builder(this, exo)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCustomLayout(listOf(stopButton))
            .setCallback(object : MediaSession.Callback {
                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    if (customCommand.customAction == ACTION_STOP) {
                        stopPlayback()
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            })
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra(EXTRA_STREAM_URL)
                val id = intent.getStringExtra(EXTRA_STATION_ID)
                if (url != null && id != null) {
                    val station = RadioStation(
                        id = id,
                        name = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Radyo",
                        streamUrl = url,
                        backupStreamUrl = intent.getStringExtra(EXTRA_BACKUP_STREAM_URL),
                        category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Müzik",
                        frequency = intent.getStringExtra(EXTRA_FREQUENCY) ?: "Canlı",
                        city = intent.getStringExtra(EXTRA_CITY) ?: "Ulusal",
                        description = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
                    )
                    activeStation = station
                    isUsingBackupStream = false
                    playStreamInternal(url, station)
                } else {
                    // Resume existing playback
                    resumePlayback()
                }
            }
            ACTION_PAUSE -> {
                pausePlayback()
            }
            ACTION_STOP -> {
                stopPlayback()
            }
            ACTION_TOGGLE -> {
                val exo = player
                if (exo != null) {
                    if (exo.isPlaying) {
                        pausePlayback()
                    } else {
                        resumePlayback()
                    }
                }
            }
            ACTION_SET_VOLUME -> {
                val vol = intent.getFloatExtra(EXTRA_VOLUME, 1.0f).coerceIn(0f, 1f)
                player?.volume = vol
                _playerState.update { it.copy(volume = vol, isMuted = vol == 0f) }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun playStreamInternal(url: String, station: RadioStation) {
        val exo = player ?: return

        _playerState.update {
            it.copy(
                currentStation = station,
                status = PlaybackStatus.CONNECTING,
                errorMessage = null
            )
        }

        updateForegroundNotification()

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(station.name)
            .setArtist("Radyo Türk")
            .setAlbumTitle(station.category)
            .setDisplayTitle(station.name)
            .build()

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setMediaId(station.id)
            .setMediaMetadata(mediaMetadata)

        if (url.contains(".m3u8", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        val mediaItem = mediaItemBuilder.build()

        try {
            exo.stop()
            exo.clearMediaItems()
            exo.setMediaItem(mediaItem)
            exo.prepare()
            exo.playWhenReady = true
        } catch (e: Exception) {
            Log.e(TAG, "Error playing stream URL: $url", e)
            _playerState.update {
                it.copy(
                    status = PlaybackStatus.ERROR,
                    errorMessage = "Yayın başlatılamadı: ${e.localizedMessage}"
                )
            }
            updateForegroundNotification()
        }
    }

    private fun pausePlayback() {
        player?.pause()
        _playerState.update { it.copy(status = PlaybackStatus.PAUSED) }
        updateForegroundNotification()
    }

    private fun resumePlayback() {
        val exo = player ?: return
        if (exo.playbackState == Player.STATE_IDLE && activeStation != null) {
            activeStation?.let { playStreamInternal(it.streamUrl, it) }
        } else {
            exo.play()
            _playerState.update { it.copy(status = PlaybackStatus.PLAYING) }
            updateForegroundNotification()
        }
    }

    private fun stopPlayback() {
        releaseAudioEffects()
        player?.stop()
        player?.clearMediaItems()
        _playerState.update { it.copy(status = PlaybackStatus.IDLE) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @OptIn(UnstableApi::class)
    private fun updateForegroundNotification() {
        val station = activeStation ?: _playerState.value.currentStation ?: return
        val session = mediaSession ?: return
        val isPlaying = player?.isPlaying == true

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action Play/Pause
        val playPauseIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = if (isPlaying) {
            PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        // Action Stop
        val stopIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_radio)
            .setContentTitle(station.name)
            .setContentText(
                when (_playerState.value.status) {
                    PlaybackStatus.CONNECTING -> "Bağlanıyor... • ${station.category}"
                    PlaybackStatus.PLAYING -> "Canlı Yayın • ${station.category}"
                    PlaybackStatus.PAUSED -> "Duraklatıldı • ${station.category}"
                    PlaybackStatus.ERROR -> "Hata • ${station.category}"
                    else -> station.category
                }
            )
            .setSubText("Radyo Türk")
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1)
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow,
                if (isPlaying) getString(R.string.action_pause) else getString(R.string.action_play),
                playPausePendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
                getString(R.string.action_stop),
                stopPendingIntent
            )

        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun applyEqualizerInternal(settings: EqualizerSettings) {
        currentEqSettings = settings
        try {
            val exo = player ?: return
            val sessionId = exo.audioSessionId
            if (sessionId <= 0 || sessionId == C.AUDIO_SESSION_ID_UNSET) return

            if (equalizer == null) {
                equalizer = Equalizer(0, sessionId)
            }
            if (bassBoost == null) {
                bassBoost = BassBoost(0, sessionId)
            }

            equalizer?.let { eq ->
                eq.enabled = settings.isEnabled
                if (settings.isEnabled) {
                    val numBands = eq.numberOfBands
                    val range = eq.bandLevelRange
                    val minLevel = range[0]
                    val maxLevel = range[1]

                    val bassMillibels = (settings.bassDb * 100).toShort().coerceIn(minLevel, maxLevel)
                    val midMillibels = (settings.midDb * 100).toShort().coerceIn(minLevel, maxLevel)
                    val trebleMillibels = (settings.trebleDb * 100).toShort().coerceIn(minLevel, maxLevel)

                    if (numBands >= 5) {
                        eq.setBandLevel(0, bassMillibels)
                        eq.setBandLevel(1, (bassMillibels * 0.7f).toInt().toShort().coerceIn(minLevel, maxLevel))
                        eq.setBandLevel(2, midMillibels)
                        eq.setBandLevel(3, (trebleMillibels * 0.7f).toInt().toShort().coerceIn(minLevel, maxLevel))
                        eq.setBandLevel(4, trebleMillibels)
                    } else if (numBands > 0) {
                        for (i in 0 until numBands) {
                            val level = when {
                                i == 0 -> bassMillibels
                                i == numBands - 1 -> trebleMillibels
                                else -> midMillibels
                            }
                            eq.setBandLevel(i.toShort(), level)
                        }
                    }
                }
            }

            bassBoost?.let { bb ->
                bb.enabled = settings.isEnabled
                if (settings.isEnabled && bb.strengthSupported) {
                    val strength = (settings.bassDb.coerceAtLeast(0) * 100).toShort().coerceIn(0, 1000)
                    bb.setStrength(strength)
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Equalizer / BassBoost not supported or failed: ${t.message}")
        }
    }

    private fun releaseAudioEffects() {
        try {
            equalizer?.release()
        } catch (_: Throwable) {}
        equalizer = null

        try {
            bassBoost?.release()
        } catch (_: Throwable) {}
        bassBoost = null
    }

    override fun onDestroy() {
        releaseAudioEffects()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        instance = null
        super.onDestroy()
    }
}
