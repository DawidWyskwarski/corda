package com.example.corda.tuner.ui.main.screen

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.R
import com.example.corda.core.datastore.TunerDataStoreManager
import com.example.corda.core.tuner.TuningMode
import com.example.corda.core.ui.state.UiState
import com.example.corda.tuner.domain.audio.PitchDetector
import com.example.corda.tuner.domain.audio.TonePlayer
import com.example.corda.tuner.domain.pitch.PitchHelpers
import com.example.corda.tuner.domain.pitch.PitchSmoother
import com.example.corda.tuner.domain.pitch.frequency
import com.example.corda.tuner.data.local.models.CurrentTuning
import com.example.corda.tuner.data.repository.TunerRepository
import com.example.corda.tuner.ui.helpers.resolveInstrumentName
import com.example.corda.tuner.ui.main.data.TuningTarget
import com.example.corda.tuner.ui.main.data.TunerReading
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class MainTunerViewModel @Inject constructor(
    private val repository: TunerRepository,
    private val tunerDataStoreManager: TunerDataStoreManager,
    private val pitchDetector: PitchDetector,
    private val tonePlayer: TonePlayer,
) : ViewModel() {

    private val pitchSmoother = PitchSmoother()

    private var pitchCollectionJob: Job? = null
    private var inTuneFrameCount = 0

    private val _isEarModeEnabled = MutableStateFlow(false)
    val isEarModeEnabled: StateFlow<Boolean> = _isEarModeEnabled.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tuningTargetState: StateFlow<UiState<TuningTarget>> = tunerDataStoreManager.tunerMode
        .flatMapLatest { mode ->
            when ( mode ) {
                TuningMode.STANDARD -> repository.getMostRecentTuning()
                    .map { it?.toLoaded() ?: UiState.Error(R.string.no_tunings) } //TODO: change res id later

                TuningMode.CHROMATIC -> flow { emit(
                    UiState.Loaded<TuningTarget>(
                        TuningTarget.Chromatic(
                            repository.getAllSounds()
                        )
                    )
                ) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    private val baseFrequency: StateFlow<Int> = tunerDataStoreManager.baseFrequency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 440
        )

    private val frequencies: StateFlow<List<Float>> = combine(
        tuningTargetState,
        baseFrequency
    ) { state, baseHz ->
        when (state) {
            is UiState.Loaded -> {
                state.data.notes.map { it.frequency(baseHz) }
            }
            else -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _currentlyClickedIndex = MutableStateFlow<Int?>(null)
    val currentlyClickedIndex: StateFlow<Int?> = _currentlyClickedIndex.asStateFlow()

    private val _tunedIndices = MutableStateFlow(emptySet<Int>())
    val tunedIndices: StateFlow<Set<Int>> = _tunedIndices.asStateFlow()

    private val _tunerReading = MutableStateFlow(TunerReading())
    val tunerReading: StateFlow<TunerReading> = _tunerReading.asStateFlow()

    fun toggleEarMode() {
        val enabled = !_isEarModeEnabled.value
        _isEarModeEnabled.value = enabled

        _currentlyClickedIndex.value = null
        tonePlayer.stop()

        if (enabled) {
            stopListening()
            _tunedIndices.value = emptySet()
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
        _tunerReading.value = TunerReading()
    }

    private fun updateUiFromPitch(frequency: Float?) {
        val loaded = (tuningTargetState.value as? UiState.Loaded)?.data ?: return

        val targets = frequencies.value
        if (frequency == null || targets.isEmpty()) {
            _tunerReading.value = TunerReading()
            inTuneFrameCount = 0
            return
        }

        val focusedIndex = _currentlyClickedIndex.value
        val bestNoteIndex: Int = if (focusedIndex != null && focusedIndex in targets.indices) {
            focusedIndex
        } else {
            PitchHelpers.findClosestNoteIndex(frequency, targets)
        }

        val bestNote = loaded.notes.getOrNull(bestNoteIndex) ?: return

        val centsOff = PitchHelpers.centsFromTarget(frequency, targets[bestNoteIndex])

        if (bestNote == _tunerReading.value.note) {
            checkAndMarkTuned(bestNoteIndex, centsOff)
        }

        _tunerReading.value = TunerReading(
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

private fun CurrentTuning.toLoaded() = UiState.Loaded<TuningTarget>(
    TuningTarget.Standard(
        tuningName = tuningName,
        instrumentName = resolveInstrumentName(instrumentCustomName, instrumentDefaultName),
        notes = notes,
    )
)
