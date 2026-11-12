package com.example.corda.tuner.ui.settings.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.core.datastore.TunerDataStoreManager
import com.example.corda.core.tuner.TuningMode
import com.example.corda.core.ui.state.UiState
import com.example.corda.tuner.data.local.entities.INSTRUMENT_MUSIC_NOTES_COUNT_BRACKET
import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.data.local.models.TuningDetails
import com.example.corda.tuner.data.repository.TunerRepository
import com.example.corda.tuner.ui.helpers.resolveInstrumentName
import com.example.corda.tuner.ui.settings.data.TuningListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TunerSettingsViewModel @Inject constructor(
    private val repository: TunerRepository,
    private val tunerDataStoreManager: TunerDataStoreManager
) : ViewModel() {

    val selectedTuningMode: StateFlow<TuningMode?> = tunerDataStoreManager.tunerMode.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null
    )

    val instruments: StateFlow<List<Instrument>?> = repository.getInstrumentsFlow().stateIn(
        scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = null
    )

    private val tunings: StateFlow<List<TuningDetails>?> = repository.getAllTunings().stateIn(
        scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = null
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterInstrumentId = MutableStateFlow<Int?>(null)
    val filterInstrumentId: StateFlow<Int?> = _filterInstrumentId.asStateFlow()

    private val _selectedTuningId = MutableStateFlow<Int?>(null)
    val selectedTuningId: StateFlow<Int?> = _selectedTuningId.asStateFlow()

    val tuningsUiState: StateFlow<UiState<List<TuningListItem>>> = combine(
        tunings,
        _searchQuery,
        _filterInstrumentId
    ) { tunings, searchQuery, filterInstrumentId ->
        if (tunings == null) return@combine UiState.Loading

        UiState.Loaded(
            data = tunings.filter {
                (filterInstrumentId == null || filterInstrumentId == it.instrumentId) &&
                        (searchQuery.isEmpty() || it.tuningName.contains(
                            searchQuery,
                            ignoreCase = true
                        ))
            }.map {
                it.toTuningListItem()
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    init {
        viewModelScope.launch { //TODO: This will probably change when I move selected tuning to the DataStore
            tunings.collect { list ->
                val current = _selectedTuningId.value
                if (current == null || list?.none { it.tuningId == current } ?: false) {
                    _selectedTuningId.value = list?.firstOrNull()?.tuningId
                }
            }
        }
    }

    // State Operations
    fun setTuningMode(mode: TuningMode) {
        viewModelScope.launch {
            tunerDataStoreManager.setTunerMode(mode)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterInstrument(instrumentId: Int) {
        if (_filterInstrumentId.value == instrumentId) {
            _filterInstrumentId.value = null
            return
        }

        _filterInstrumentId.value = instrumentId
    }

    fun setSelectedTuning(tuningId: Int) {
        _selectedTuningId.value = tuningId
    }

    // Instrument Operations
    fun createInstrument(instrument: Instrument) {
        val trimmed = instrument.customName?.trim()
        val musicNotesCount = instrument.musicNotesCount

        if (trimmed.isNullOrBlank() ||
            musicNotesCount !in INSTRUMENT_MUSIC_NOTES_COUNT_BRACKET
        ) return

        viewModelScope.launch {
            repository.insertInstrument(
                instrument.copy(customName = trimmed)
            )
        }
    }

    fun updateInstrument(instrument: Instrument) {
        if (instrumentsNotLoaded()) return

        val trimmed = instrument.customName?.trim()?.ifBlank { null }
        val musicNotesCount = instrument.musicNotesCount

        if ((instrument.defaultName == null && trimmed == null) ||
            musicNotesCount !in INSTRUMENT_MUSIC_NOTES_COUNT_BRACKET
        ) return

        val stored = instruments.value?.firstOrNull { it.id == instrument.id } ?: return
        val safeCount = if (hasTunings(instrument.id)) stored.musicNotesCount else musicNotesCount

        viewModelScope.launch {
            repository.updateInstrument(
                instrument.copy(customName = trimmed, musicNotesCount = safeCount)
            )
        }
    }

    fun deleteInstrument(instrumentId: Int) {
        if (instrumentsNotLoaded() || hasTunings(instrumentId)) return

        viewModelScope.launch {
            repository.deleteInstrument(instrumentId)

            if (_filterInstrumentId.value == instrumentId) {
                _filterInstrumentId.value = null
            }
        }
    }

    fun hasTunings(instrumentId: Int): Boolean {
        return tunings.value?.any { it.instrumentId == instrumentId } ?: true
    }

    // Tuning Operations
    fun deleteTuning(tuningId: Int) {
        if (tuningsNotLoaded()) return

        viewModelScope.launch {
            repository.deleteTuning(tuningId)

            if (_selectedTuningId.value == tuningId) {
                _selectedTuningId.value = tunings.value!!.firstOrNull()?.tuningId
            }
        }
    }

    fun markSelectedTuningAsUsed() { //TODO: I will probably move selected Tuning Id to the dataStore and this will end up in the MainTunerScreen
        if (_selectedTuningId.value == null) return

        viewModelScope.launch {
            repository.updateTuningLastUsed(_selectedTuningId.value!!)
        }
    }

    // Helper Functions
    private fun instrumentsNotLoaded(): Boolean = instruments.value == null
    private fun tuningsNotLoaded(): Boolean = tunings.value == null
}

private fun TuningDetails.toTuningListItem() = TuningListItem(
    tuningId = tuningId,
    tuningName = tuningName,
    instrumentId = instrumentId,
    _instrumentName = resolveInstrumentName(instrumentCustomName, instrumentDefaultName),
    notes = musicNotes
)
