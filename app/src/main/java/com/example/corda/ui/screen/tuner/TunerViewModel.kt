package com.example.corda.ui.screen.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import com.example.corda.data.tuner.repository.TunerRepository
import com.example.corda.domain.tuner.TuningMode
import com.example.corda.domain.tuner.audio.PitchDetector
import com.example.corda.domain.tuner.audio.TonePlayer
import com.example.corda.domain.tuner.pitch.PitchHelpers
import com.example.corda.domain.tuner.pitch.PitchResult
import com.example.corda.domain.tuner.pitch.PitchSmoother
import com.example.corda.domain.tuner.pitch.ResolvedPitch
import com.example.corda.domain.tuner.pitch.TunerModePitchResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class TunerUiState(
    val detectedNote: String? = null,
    val detectedFrequency: Float? = null,
    val centsOff: Float? = null,
    val isListening: Boolean = false,
    val focusedSoundIndex: Int? = null,
    val tunedSoundIndices: Set<Int> = emptySet(),
    val autoSelectedSoundIndex: Int? = null,
    val isEarModeEnabled: Boolean = false,
    /** Index of the string chip toggled on in STANDARD ear mode (null = none playing). */
    val earPlayingStandardIndex: Int? = null,
    /** Selected chromatic note in CHROMATIC ear mode (UI model, not persisted). */
    val earSelectedChromaticSound: Sound? = null,
    val isPlayingChromaticNote: Boolean = false,
)

@HiltViewModel
class TunerViewModel @Inject constructor(
    private val repository: TunerRepository,
    private val pitchDetector: PitchDetector,
    private val tonePlayer: TonePlayer,
) : ViewModel() {

    private val pitchSmoother = PitchSmoother()

    private var pitchCollectionJob: Job? = null
    private var listeningStateJob: Job? = null
    private var inTuneFrameCount = 0

    val tunings: StateFlow<List<TuningWithInstrumentAndSounds>> = repository
        .getTunings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _selectedTuning = MutableStateFlow<TuningWithInstrumentAndSounds?>(null)

    val selectedTuning: StateFlow<TuningWithInstrumentAndSounds?> = combine(
        _selectedTuning,
        tunings,
    ) { selected, tuningList ->
        val stillExists = selected != null && tuningList.any { it.tuningId == selected.tuningId }
        if (stillExists) selected else tuningList.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun selectTuning(tuning: TuningWithInstrumentAndSounds) {
        tonePlayer.stop()
        _selectedTuning.value = tuning
        _tunerUiState.value = TunerUiState()
    }

    fun updateSelectedTuningLastUsed() {
        val currentTuning = _selectedTuning.value ?: tunings.value.firstOrNull()
        currentTuning?.let { tuning ->
            viewModelScope.launch {
                repository.updateTuningLastUsed(tuning.tuningId, System.currentTimeMillis())
            }
        }
    }

    private val _selectedMode = MutableStateFlow(TuningMode.STANDARD)
    val selectedMode: StateFlow<TuningMode> = _selectedMode.asStateFlow()

    fun selectMode(mode: TuningMode) {
        tonePlayer.stop()
        _selectedMode.value = mode
        _tunerUiState.value = TunerUiState()
    }

    private val _tunerUiState = MutableStateFlow(TunerUiState())
    val tunerUiState: StateFlow<TunerUiState> = _tunerUiState.asStateFlow()

    /** Chromatic ladder as [Sound] rows for ear-mode carousel (MIDI id as [Sound.soundId]). */
    val chromaticSoundsForEar: List<Sound> get() = chromaticSounds

    fun onNoteChipClicked(index: Int?) {
        val current = _tunerUiState.value
        val newFocused = if (current.focusedSoundIndex == index) null else index
        inTuneFrameCount = 0
        _tunerUiState.value = current.copy(focusedSoundIndex = newFocused)
    }

    fun setEarModeEnabled(enabled: Boolean) {
        val cur = _tunerUiState.value
        if (cur.isEarModeEnabled == enabled) return

        if (enabled) {
            stopMicrophoneOnly()
            tonePlayer.stop()
            val defaultChromatic = defaultChromaticSound()
            _tunerUiState.value = cur.copy(
                isEarModeEnabled = true,
                detectedNote = null,
                detectedFrequency = null,
                centsOff = null,
                isListening = false,
                earPlayingStandardIndex = null,
                isPlayingChromaticNote = false,
                earSelectedChromaticSound = cur.earSelectedChromaticSound ?: defaultChromatic,
            )
        } else {
            tonePlayer.stop()
            _tunerUiState.value = cur.copy(
                isEarModeEnabled = false,
                earPlayingStandardIndex = null,
                isPlayingChromaticNote = false,
            )
            startListening()
        }
    }

    /** Tap a chip to start its reference tone; tap again (or another chip) to stop / switch. */
    fun onEarModeStandardNoteToggled(index: Int) {
        val tuning = selectedTuning.value ?: return
        val sounds = tuning.sounds
        if (index !in sounds.indices) return
        val cur = _tunerUiState.value
        if (cur.earPlayingStandardIndex == index) {
            tonePlayer.stop()
            _tunerUiState.value = cur.copy(earPlayingStandardIndex = null)
        } else {
            tonePlayer.play(sounds[index].frequency, viewModelScope)
            _tunerUiState.value = cur.copy(earPlayingStandardIndex = index)
        }
    }

    fun onEarChromaticSoundSelected(sound: Sound) {
        val cur = _tunerUiState.value
        _tunerUiState.value = cur.copy(earSelectedChromaticSound = sound)
        if (cur.isPlayingChromaticNote) {
            tonePlayer.play(sound.frequency, viewModelScope)
        }
    }

    fun toggleChromaticPlayback() {
        val cur = _tunerUiState.value
        val sound = cur.earSelectedChromaticSound ?: return
        if (cur.isPlayingChromaticNote) {
            tonePlayer.stop()
            _tunerUiState.value = _tunerUiState.value.copy(isPlayingChromaticNote = false)
        } else {
            tonePlayer.play(sound.frequency, viewModelScope)
            _tunerUiState.value = _tunerUiState.value.copy(isPlayingChromaticNote = true)
        }
    }

    fun startListening() {
        if (_tunerUiState.value.isEarModeEnabled) return
        if (pitchDetector.isListening.value) return

        pitchSmoother.reset()
        inTuneFrameCount = 0
        pitchDetector.start(viewModelScope)

        pitchCollectionJob = viewModelScope.launch {
            pitchDetector.pitchFlow.collect { rawFreq ->
                val smoothedFreq = pitchSmoother.process(rawFreq)
                updateUiFromPitch(smoothedFreq)
            }
        }

        listeningStateJob?.cancel()
        listeningStateJob = viewModelScope.launch {
            pitchDetector.isListening.collect { listening ->
                _tunerUiState.value = _tunerUiState.value.copy(isListening = listening)
            }
        }
    }

    fun stopListening() {
        pitchCollectionJob?.cancel()
        pitchCollectionJob = null
        listeningStateJob?.cancel()
        listeningStateJob = null
        pitchDetector.stop()
        pitchSmoother.reset()
        inTuneFrameCount = 0
        tonePlayer.stop()
        _tunerUiState.value = TunerUiState()
    }

    private fun stopMicrophoneOnly() {
        pitchCollectionJob?.cancel()
        pitchCollectionJob = null
        listeningStateJob?.cancel()
        listeningStateJob = null
        pitchDetector.stop()
        pitchSmoother.reset()
        inTuneFrameCount = 0
        val micOff = _tunerUiState.value
        _tunerUiState.value = micOff.copy(
            detectedNote = null,
            detectedFrequency = null,
            centsOff = null,
            isListening = false,
        )
    }

    private fun updateUiFromPitch(smoothedFreq: Float?) {
        if (_tunerUiState.value.isEarModeEnabled) return

        if (smoothedFreq == null) {
            _tunerUiState.value = _tunerUiState.value.copy(
                detectedNote = null,
                detectedFrequency = null,
                centsOff = null,
            )
            inTuneFrameCount = 0
            return
        }

        val current = _tunerUiState.value
        val tuning = selectedTuning.value

        val resolved = TunerModePitchResolver.resolve(
            smoothedFreq,
            _selectedMode.value,
            tuning,
            current.focusedSoundIndex,
        ) ?: return

        when (resolved) {
            is ResolvedPitch.Chromatic -> {
                val result = resolved.result
                _tunerUiState.value = current.copy(
                    detectedNote = PitchHelpers.compositeLabel(result.pitchClass, result.octave),
                    detectedFrequency = result.frequencyHz,
                    centsOff = result.centsOff,
                )
            }

            is ResolvedPitch.Standard -> {
                val result = resolved.result
                val sounds = tuning?.sounds.orEmpty()
                val focusIdx = current.focusedSoundIndex
                val soundIndices = sounds.indices
                val tuneIndex = when {
                    focusIdx != null && focusIdx in soundIndices -> focusIdx
                    resolved.matchingSoundIndex != null && resolved.matchingSoundIndex >= 0 ->
                        resolved.matchingSoundIndex
                    else -> null
                }
                val newTuned = if (tuneIndex != null) {
                    checkAndMarkTuned(result, tuneIndex, current.tunedSoundIndices)
                } else {
                    current.tunedSoundIndices
                }
                _tunerUiState.value = current.copy(
                    detectedNote = PitchHelpers.compositeLabel(result.pitchClass, result.octave),
                    detectedFrequency = result.frequencyHz,
                    centsOff = result.centsOff,
                    autoSelectedSoundIndex = if (focusIdx == null) resolved.matchingSoundIndex else null,
                    tunedSoundIndices = newTuned,
                )
            }
        }
    }

    private companion object {
        const val IN_TUNE_THRESHOLD_CENTS = 5f
        const val IN_TUNE_FRAMES_REQUIRED = 64

        private val chromaticSounds: List<Sound> by lazy { PitchHelpers.allSounds }

        private fun defaultChromaticSound(): Sound =
            chromaticSounds.firstOrNull { it.name == "A" && it.octave == 4 }
                ?: chromaticSounds.first()
    }

    private fun checkAndMarkTuned(
        result: PitchResult,
        index: Int,
        currentTuned: Set<Int>,
    ): Set<Int> {
        if (abs(result.centsOff) < IN_TUNE_THRESHOLD_CENTS) {
            inTuneFrameCount++
            if (inTuneFrameCount >= IN_TUNE_FRAMES_REQUIRED) {
                return currentTuned + index
            }
        } else {
            inTuneFrameCount = 0
        }
        return currentTuned
    }

    override fun onCleared() {
        pitchCollectionJob?.cancel()
        listeningStateJob?.cancel()
        pitchDetector.stop()
        tonePlayer.stop()
    }
}
