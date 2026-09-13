package com.example.player

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.example.model.EqualizerSettings
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class RadioPlayerManager(private val context: Context) {

    private val TAG = "RadioPlayerManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playerState = MutableStateFlow(RadioPlaybackService.playerState.value)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var sleepCountDownTimer: CountDownTimer? = null
    private var lastVolumeBeforeMute: Float = 0.8f

    init {
        // Continuously synchronize state from the Media3 RadioPlaybackService
        RadioPlaybackService.playerState.onEach { serviceState ->
            _playerState.update { current ->
                serviceState.copy(
                    sleepTimerActive = current.sleepTimerActive,
                    sleepTimerMinutesRemaining = current.sleepTimerMinutesRemaining,
                    sleepTimerSecondsRemaining = current.sleepTimerSecondsRemaining,
                    sleepTimerTotalSeconds = current.sleepTimerTotalSeconds,
                    sleepTimerSelectedMinutes = current.sleepTimerSelectedMinutes
                )
            }
        }.launchIn(scope)
    }

    fun playStation(station: RadioStation) {
        Log.d(TAG, "playStation: ${station.name} (${station.streamUrl})")
        RadioPlaybackService.startPlay(context, station)
    }

    fun togglePlayPause() {
        val state = _playerState.value
        when (state.status) {
            PlaybackStatus.PLAYING -> pause()
            PlaybackStatus.PAUSED -> resume()
            PlaybackStatus.ERROR, PlaybackStatus.IDLE -> {
                state.currentStation?.let { playStation(it) }
            }
            PlaybackStatus.CONNECTING -> {
                stop()
            }
        }
    }

    fun pause() {
        RadioPlaybackService.pause(context)
    }

    fun resume() {
        val state = _playerState.value
        if (state.currentStation != null) {
            RadioPlaybackService.resume(context)
        }
    }

    fun stop() {
        RadioPlaybackService.stop(context)
        _playerState.update { it.copy(status = PlaybackStatus.IDLE) }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        if (clamped > 0f) {
            lastVolumeBeforeMute = clamped
        }
        RadioPlaybackService.setVolume(context, clamped)
        _playerState.update { it.copy(volume = clamped, isMuted = clamped == 0f) }
    }

    fun toggleMute() {
        val currentState = _playerState.value
        if (currentState.isMuted) {
            setVolume(if (lastVolumeBeforeMute > 0f) lastVolumeBeforeMute else 0.8f)
        } else {
            setVolume(0f)
        }
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return

        val totalMillis = minutes * 60 * 1000L
        val totalSeconds = minutes * 60L
        _playerState.update {
            it.copy(
                sleepTimerActive = true,
                sleepTimerMinutesRemaining = minutes,
                sleepTimerSecondsRemaining = totalSeconds,
                sleepTimerTotalSeconds = totalSeconds,
                sleepTimerSelectedMinutes = minutes
            )
        }

        sleepCountDownTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSecs = (millisUntilFinished / 1000L).coerceAtLeast(0L)
                val minsLeft = (totalSecs / 60L).toInt() + (if (totalSecs % 60L > 0L) 1 else 0)
                _playerState.update {
                    it.copy(
                        sleepTimerMinutesRemaining = minsLeft,
                        sleepTimerSecondsRemaining = totalSecs
                    )
                }
            }

            override fun onFinish() {
                stop()
                cancelSleepTimer()
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepCountDownTimer?.cancel()
        sleepCountDownTimer = null
        _playerState.update {
            it.copy(
                sleepTimerActive = false,
                sleepTimerMinutesRemaining = null,
                sleepTimerSecondsRemaining = null,
                sleepTimerTotalSeconds = null,
                sleepTimerSelectedMinutes = null
            )
        }
    }

    fun applyEqualizerSettings(settings: EqualizerSettings) {
        RadioPlaybackService.applyEqualizerSettings(settings)
    }

    fun release() {
        cancelSleepTimer()
        scope.cancel()
    }
}
