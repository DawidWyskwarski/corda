package com.example.corda.ui.screen.tuner.addtuning

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.entities.Tuning
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
import kotlinx.coroutines.flow.firstOrNull
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

    val instruments: StateFlow<List<Instrument>> = repository
        .getInstrumentsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _allNotes = MutableStateFlow<List<MusicNote>>(emptyList())
    val allNotes: StateFlow<List<MusicNote>> = _allNotes.asStateFlow()

    private val _tuningName = MutableStateFlow("")
    val tuningName: StateFlow<String> = _tuningName.asStateFlow()

    private val _selectedInstrumentId = MutableStateFlow<Int?>(null)
    val selectedInstrument: StateFlow<Instrument?> = combine(
        _selectedInstrumentId,
        instruments
    ) { instrumentId, instruments ->
        instruments.firstOrNull { it.id == instrumentId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _stringMusicNotes = MutableStateFlow<List<MusicNote>>(emptyList())
    val stringMusicNotes: StateFlow<List<MusicNote>> = _stringMusicNotes.asStateFlow()

    private val _selectedStringIndex = MutableStateFlow<Int?>(null)
    val selectedStringIndex: StateFlow<Int?> = _selectedStringIndex.asStateFlow()

    val isSaveEnabled: StateFlow<Boolean> = combine(
        _tuningName,
        _selectedInstrumentId,
        _stringMusicNotes,
    ) { name, instrumentId, musicNotes ->
        name.isNotBlank() && instrumentId != null && musicNotes.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            _allNotes.value = repository.getAllSounds()

            if (isEditMode) {
                loadEditedTuning(tuningId!!)
            }
        }
    }

    private suspend fun loadEditedTuning(id: Int) {
        val tuning = repository.getTuning(id) ?: return

        _selectedInstrumentId.value = tuning.instrumentId
        _tuningName.value = tuning.tuningName

        _stringMusicNotes.value = tuning.musicNotes
    }

    fun setTuningName(name: String) {
        _tuningName.value = name
    }

    fun selectInstrument(instrumentId: Int) {
        if (isEditMode || _selectedInstrumentId.value == instrumentId) return

        _selectedStringIndex.value = null

        val stringCount = instruments.value.firstOrNull { it.id == instrumentId }?.musicNotesCount ?: 0
        initDefaultSounds(stringCount)

        _selectedInstrumentId.value = instrumentId
    }

    private fun initDefaultSounds(count: Byte) {
        val notes = allNotes.value
        if (notes.isEmpty()) return

        // This corresponds to A4
        val defaultSound = notes.find { it.midiNum == 69 } ?: notes.first()

        _stringMusicNotes.value = List(count.toInt()) { defaultSound }
    }

    fun selectString(index: Int?) {
        _selectedStringIndex.value = index
    }

    fun setNoteForSelectedString(musicNoteIndex: Int) {
        val index = _selectedStringIndex.value ?: return
        val notes = _stringMusicNotes.value

        if (index in notes.indices) {
            _stringMusicNotes.value = notes.toMutableList().also { it[index] = _allNotes.value[musicNoteIndex] }
        }
    }

    fun saveTuning() {
        val name = _tuningName.value.trim()
        val instrumentId = _selectedInstrumentId.value ?: return
        val notes = _stringMusicNotes.value

        if (name.isBlank() || notes.isEmpty()) return

        val tuning = Tuning(
            id = tuningId ?: 0,
            name = name,
            instrumentId = instrumentId,
        )

        viewModelScope.launch {
            val result = if (isEditMode) {
                repository.updateTuning(
                    tuning = tuning,
                    musicNotes = notes
                )
            } else {
                repository.insertTuning(
                    tuning = tuning,
                    musicNotes = notes
                )
            }

            if (result.isSuccess) {
                _saved.value = true
            }
        }
    }
}
