package com.example.corda.ui.screen.tuner

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.models.TuningDetails
import com.example.corda.data.tuner.repository.TunerRepository
import com.example.corda.domain.tuner.TuningMode
import com.example.corda.domain.tuner.audio.PitchDetector
import com.example.corda.domain.tuner.audio.TonePlayer
import com.example.corda.domain.tuner.pitch.PitchHelpers
import com.example.corda.domain.tuner.pitch.PitchSmoother
import com.example.corda.domain.tuner.pitch.frequency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class TunerState(
    val note: MusicNote? = null,
    val frequency: Float? = null,
    val noteIndex: Int? = null,
    val centsOff: Float? = null,
)

@HiltViewModel
class TunerViewModel @Inject constructor(
    private val repository: TunerRepository,
    private val tunerStateManager: TunerStateManager,
    private val pitchDetector: PitchDetector,
    private val tonePlayer: TonePlayer,
) : ViewModel() {

    private val pitchSmoother = PitchSmoother()

    private var pitchCollectionJob: Job? = null
    private var inTuneFrameCount = 0

    val tuningMode: StateFlow<TuningMode> = tunerStateManager.tunerMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TuningMode.STANDARD
        )

    private val _isEarModeEnabled = MutableStateFlow(false)
    val isEarModeEnabled: StateFlow<Boolean> = _isEarModeEnabled.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTuning: StateFlow<TuningDetails?> = tuningMode
        .flatMapLatest { mode ->
            when (mode) {
                TuningMode.CHROMATIC -> flow {
                    emit(
                        TuningDetails(
                            tuningId = -1,
                            tuningName = "Chromatic",
                            instrumentId = -1,
                            instrumentName = "",
                            musicNotes = repository.getAllSounds(),
                            lastUsed = 1L,
                        )
                    )
                }
                TuningMode.STANDARD -> repository.getMostRecentTuning()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val baseFrequency: StateFlow<Int> = tunerStateManager.baseFrequency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 440
        )

    private val frequencies: StateFlow<List<Float>> = combine(
        selectedTuning,
        baseFrequency
    ) { tuning, baseHz ->
        tuning?.musicNotes?.map { it.frequency(baseHz) } ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _currentlyClickedIndex = MutableStateFlow<Int?>(null)
    val currentlyClickedIndex: StateFlow<Int?> = _currentlyClickedIndex.asStateFlow()

    private val _tunedIndices = MutableStateFlow(emptySet<Int>())
    val tunedIndices: StateFlow<Set<Int>> = _tunedIndices.asStateFlow()

    private val _tunerState = MutableStateFlow(TunerState())
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    fun toggleEarMode() {
        val enabled = !_isEarModeEnabled.value
        _isEarModeEnabled.value = enabled

        _currentlyClickedIndex.value = null
        tonePlayer.stop()

        if (enabled) {
            stopListening()
        }
    }

    fun onItemClicked(index: Int) {
        val newIndex = if (_currentlyClickedIndex.value == index) null else index
        _currentlyClickedIndex.value = newIndex

        if (_isEarModeEnabled.value) {
            playSelectedTone(newIndex)
        }
    }

    private fun playSelectedTone(index: Int?) {
        val freqs = frequencies.value
        if (index == null || index !in freqs.indices) {
            tonePlayer.stop()
            return
        }
        tonePlayer.play(freqs[index], viewModelScope)
    }

    fun startListening() {
        if (_isEarModeEnabled.value) return
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
    }

    fun stopListening() {
        if (!pitchDetector.isListening.value) return

        pitchCollectionJob?.cancel()
        pitchCollectionJob = null
        pitchDetector.stop()
        pitchSmoother.reset()
        inTuneFrameCount = 0
        _tunerState.value = TunerState()
    }

    private fun updateUiFromPitch(frequency: Float?) {
        val targets = frequencies.value
        if (frequency == null || targets.isEmpty()) {
            _tunerState.value = TunerState()
            inTuneFrameCount = 0
            return
        }

        val focusedIndex = _currentlyClickedIndex.value
        val bestNoteIndex: Int = if (focusedIndex != null && focusedIndex in targets.indices) {
            focusedIndex
        } else {
            PitchHelpers.findClosestNoteIndex(frequency, targets)
        }

        val bestNote = selectedTuning.value?.musicNotes?.getOrNull(bestNoteIndex)
        val centsOff = PitchHelpers.centsFromTarget(frequency, targets[bestNoteIndex])

        if (bestNote == _tunerState.value.note) {
            checkAndMarkTuned(bestNoteIndex, centsOff)
        }

        _tunerState.value = TunerState(
            note = bestNote,
            frequency = frequency,
            noteIndex = bestNoteIndex,
            centsOff = centsOff
        )
    }

    private fun checkAndMarkTuned(
        bestNoteIndex: Int,
        centsOff: Float
    ) {
        if (abs(centsOff) < IN_TUNE_THRESHOLD_CENTS) {
            inTuneFrameCount++

            if (inTuneFrameCount >= IN_TUNE_FRAMES_REQUIRED) {
                _tunedIndices.update { currentSet ->
                    currentSet + bestNoteIndex
                }
            }
        } else {
            inTuneFrameCount = 0
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()

        stopListening()
        tonePlayer.stop()
    }

    private companion object {
        const val IN_TUNE_THRESHOLD_CENTS = 5f
        const val IN_TUNE_FRAMES_REQUIRED = 64
    }
}
