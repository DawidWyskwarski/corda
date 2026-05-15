package com.example.corda.ui.screen.tuner.addtuning

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.repository.TunerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddEditTuningViewModel.Factory::class)
class AddEditTuningViewModel @AssistedInject constructor(
    private val repository: TunerRepository,
    @Assisted private val tuningId: Int?,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(tuningId: Int?): AddEditTuningViewModel
    }

    val isEditMode: Boolean = tuningId != null

    private val _instruments = MutableStateFlow<List<Instrument>>(emptyList())
    val instruments: StateFlow<List<Instrument>> = _instruments.asStateFlow()

    private val _allSounds = MutableStateFlow<List<Sound>>(emptyList())
    val allSounds: StateFlow<List<Sound>> = _allSounds.asStateFlow()

    private val _tuningName = MutableStateFlow("")
    val tuningName: StateFlow<String> = _tuningName.asStateFlow()

    private val _selectedInstrument = MutableStateFlow<Instrument?>(null)
    val selectedInstrument: StateFlow<Instrument?> = _selectedInstrument.asStateFlow()

    val stringSounds = mutableStateListOf<Sound>()

    private val _selectedStringIndex = MutableStateFlow<Int?>(null)
    val selectedStringIndex: StateFlow<Int?> = _selectedStringIndex.asStateFlow()

    val isSaveEnabled: StateFlow<Boolean> = combine(
        _tuningName,
        _selectedInstrument,
        snapshotFlow { stringSounds.toList() },
    ) { name, instrument, sounds ->
        name.isNotBlank() && instrument != null && sounds.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            _allSounds.value = repository.getAllSounds()
            _instruments.value = repository.getInstruments().first()

            if (isEditMode && tuningId != null) {
                loadExistingTuning(tuningId)
            }
        }
    }

    private suspend fun loadExistingTuning(id: Int) {
        val tuning = repository.getTuningWithSoundsById(id) ?: return
        val instrument = _instruments.value.find { it.instrumentId == tuning.instrumentId }

        _selectedInstrument.value = instrument
        _tuningName.value = tuning.tuningName

        stringSounds.clear()
        stringSounds.addAll(tuning.sounds)
    }

    fun setTuningName(name: String) {
        _tuningName.value = name
    }

    fun selectInstrument(instrument: Instrument) {
        if (isEditMode || instrument == _selectedInstrument.value) return
        _selectedStringIndex.value = null
        initDefaultSounds(instrument.soundsCount.toInt())
        _selectedInstrument.value = instrument
    }

    private fun initDefaultSounds(count: Int) {
        val sounds = _allSounds.value
        if (sounds.isEmpty()) return

        val defaultSound = sounds.find { it.name == "E" && it.octave == 2 } ?: sounds.first()
        stringSounds.clear()
        repeat(count) { stringSounds.add(defaultSound) }
    }

    fun selectString(index: Int?) {
        _selectedStringIndex.value = index
    }

    fun setNoteForSelectedString(sound: Sound) {
        val index = _selectedStringIndex.value ?: return
        if (index in stringSounds.indices) {
            stringSounds[index] = sound
        }
    }

    fun saveTuning() {
        val name = _tuningName.value.trim()
        val instrument = _selectedInstrument.value ?: return
        val sounds = stringSounds.toList()
        if (name.isBlank() || sounds.isEmpty()) return

        viewModelScope.launch {
            val result = if (isEditMode && tuningId != null) {
                repository.updateTuningWithSounds(tuningId, name, sounds)
            } else {
                repository.insertTuningWithSounds(name, instrument.instrumentId, sounds)
            }

            if (result.isSuccess) {
                _saved.value = true
            }
        }
    }
}
