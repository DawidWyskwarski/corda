package com.example.corda.ui.screen.tuner.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.models.TuningDetails
import com.example.corda.data.tuner.repository.TunerRepository
import com.example.corda.domain.tuner.TuningMode
import com.example.corda.ui.screen.tuner.TunerStateManager
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
    private val tunerStateManager: TunerStateManager
) : ViewModel() {

    val selectedTuningMode: StateFlow<TuningMode> = tunerStateManager.tunerMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TuningMode.STANDARD
        )

    val instruments: StateFlow<List<Instrument>> = repository
        .getInstrumentsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val tunings: StateFlow<List<TuningDetails>> = repository
        .getAllTunings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterInstrumentId = MutableStateFlow<Int?>(null)
    val filterInstrument: StateFlow<Instrument?> = combine(
        _filterInstrumentId,
        instruments
    ) { instrumentId, instruments ->
        instruments.firstOrNull { it.id == instrumentId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _selectedTuningId = MutableStateFlow<Int?>(null)
    val selectedTuningId: StateFlow<Int?> = _selectedTuningId.asStateFlow()

    val filteredTunings: StateFlow<List<TuningDetails>> = combine(
        tunings,
        _searchQuery,
        _filterInstrumentId
    ) { tuningList, query, instrumentId ->
        tuningList.filter { tuning ->
            val matchesInstrument = instrumentId == null || tuning.instrumentId == instrumentId
            val matchesQuery = query.isEmpty() || tuning.tuningName.contains(query, ignoreCase = true)

            matchesInstrument && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            tunings.collect { list ->
                val current = _selectedTuningId.value
                if (current == null || list.none { it.tuningId == current }) {
                    _selectedTuningId.value = list.firstOrNull()?.tuningId
                }
            }
        }
    }

    fun setTuningMode(mode: TuningMode) { 
        viewModelScope.launch {
            tunerStateManager.setTunerMode(mode)
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setFilterInstrument(instrumentId: Int) {
        if (_filterInstrumentId.value == instrumentId) {
            _filterInstrumentId.value = null
            return
        }

        _filterInstrumentId.value = instrumentId
    }

    fun createInstrument(instrument: Instrument) {
        val trimmed = instrument.name.trim()
        val musicNotesCount = instrument.musicNotesCount

        if (trimmed.isBlank() || musicNotesCount !in 2..24) return

        viewModelScope.launch {
            repository.insertInstrument(
                instrument.copy(name = trimmed)
            )
        }
    }

    fun updateInstrument(instrument: Instrument) {
        val trimmed = instrument.name.trim()
        val musicNotesCount = instrument.musicNotesCount

        if (trimmed.isBlank() || musicNotesCount !in 2..24) return

        val stored = instruments.value.firstOrNull { it.id == instrument.id } ?: return
        val safeCount = if (hasTunings(instrument.id)) stored.musicNotesCount else musicNotesCount

        viewModelScope.launch {
            repository.updateInstrument(
                instrument.copy(name = trimmed, musicNotesCount = safeCount)
            )
        }
    }

    fun deleteInstrument(instrumentId: Int) {
        if (hasTunings(instrumentId))
            return

        viewModelScope.launch {
            repository.deleteInstrument(instrumentId)

            if (_filterInstrumentId.value == instrumentId) {
                _filterInstrumentId.value = null
            }
        }
    }

    fun hasTunings(instrumentId: Int): Boolean {
        return tunings.value.any { it.instrumentId == instrumentId }
    }

    fun setSelectedTuning(tuningId: Int) {
        _selectedTuningId.value = tuningId
    }

    fun deleteTuning(tuningId: Int) {
        viewModelScope.launch {
            repository.deleteTuning(tuningId)

            if (_selectedTuningId.value == tuningId) {
                _selectedTuningId.value = tunings.value.firstOrNull()?.tuningId
            }
        }
    }

    fun markSelectedTuningAsUsed() {
        if (_selectedTuningId.value == null) return

        viewModelScope.launch {
            repository.updateTuningLastUsed(_selectedTuningId.value!!)
        }
    }
}
