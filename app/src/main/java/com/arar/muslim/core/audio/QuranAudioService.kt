package com.arar.muslim.core.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PlaybackStatus {
    object Idle : PlaybackStatus
    data class Playing(val surahId: Int, val ayahNumber: Int) : PlaybackStatus
    object Paused : PlaybackStatus
    data class Error(val message: String) : PlaybackStatus
}

interface QuranAudioService {
    val playbackState: StateFlow<PlaybackStatus>
    fun loadAyahAudio(surahNumber: Int, ayahNumber: Int, localUri: String?)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
}

/**
 * Implementasi awal berupa No-Op service.
 * Saat fase update audio tiba, tinggal menyediakan implementasi berbasis Media3 / ExoPlayer
 * tanpa merombak layer Domain atau Room DAO.
 */
class NoOpQuranAudioService : QuranAudioService {
    private val _playbackState = MutableStateFlow<PlaybackStatus>(PlaybackStatus.Idle)
    override val playbackState: StateFlow<PlaybackStatus> = _playbackState.asStateFlow()

    override fun loadAyahAudio(surahNumber: Int, ayahNumber: Int, localUri: String?) {
        // Persiapan pemutaran audio
    }

    override fun play() {
        // No-op
    }

    override fun pause() {
        _playbackState.value = PlaybackStatus.Paused
    }

    override fun stop() {
        _playbackState.value = PlaybackStatus.Idle
    }

    override fun seekTo(positionMs: Long) {
        // No-op
    }
}
