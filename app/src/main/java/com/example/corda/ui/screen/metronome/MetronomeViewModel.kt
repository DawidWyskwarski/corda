package com.example.corda.ui.screen.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.domain.metronome.MetronomeAudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetronomeUiState(
    val bpm: Int = 130,
    val isRunning: Boolean = false,
    val currentBeat: Int = 1,
    val beatsPerBar: Int = 4,
    val mutingEnabled: Boolean = false,
    val playBars: Int = 1,
    val muteBars: Int = 1,
    val isMuted: Boolean = false,
    val beatTick: Long = 0L,
)

@HiltViewModel
class MetronomeViewModel @Inject constructor(
    private val audioPlayer: MetronomeAudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(MetronomeUiState())
    val state: StateFlow<MetronomeUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun setBpm(bpm: Int) {
        _state.update { it.copy(bpm = bpm.coerceIn(BPM_MIN, BPM_MAX)) }
    }

    fun setBeatsPerBar(beats: Int) {
        _state.update { it.copy(beatsPerBar = beats) }
    }

    fun setMutingEnabled(enabled: Boolean) {
        _state.update { it.copy(mutingEnabled = enabled) }
    }

    fun setPlayBars(bars: Int) {
        _state.update { it.copy(playBars = bars) }
    }

    fun setMuteBars(bars: Int) {
        _state.update { it.copy(muteBars = bars) }
    }

    fun toggleMetronome() {
        if (_state.value.isRunning) stop() else start()
    }

    private fun start() {
        _state.update { it.copy(isRunning = true, currentBeat = 1, isMuted = false, beatTick = 1L) }
        audioPlayer.playBeat(isAccent = true)
        startTimer()
    }

    private fun stop() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false, currentBeat = 1, isMuted = false) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var totalBeats = 1L
            while (true) {
                delay(60_000L / _state.value.bpm)
                totalBeats++
                val current = _state.value
                val newBeat = ((totalBeats - 1) % current.beatsPerBar + 1).toInt()
                val isMuted = if (current.mutingEnabled) {
                    val currentBarIndex = (totalBeats - 1) / current.beatsPerBar
                    val cycleLength = (current.playBars + current.muteBars).toLong()
                    val posInCycle = (currentBarIndex % cycleLength).toInt()
                    posInCycle >= current.playBars
                } else {
                    false
                }
                _state.update { it.copy(currentBeat = newBeat, isMuted = isMuted, beatTick = totalBeats) }
                if (!isMuted) {
                    audioPlayer.playBeat(isAccent = newBeat == 1)
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        audioPlayer.release()
    }

    companion object {
        const val BPM_MIN = 20
        const val BPM_MAX = 240
    }
}
