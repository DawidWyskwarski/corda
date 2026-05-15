package com.example.corda.ui.screen.tuner.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import com.example.corda.data.tuner.repository.TunerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstrumentRow(
    val instrument: Instrument,
    val tuningCount: Int,
)

@HiltViewModel
class TunerSettingsViewModel @Inject constructor(
    private val repository: TunerRepository,
) : ViewModel() {

    val instruments: StateFlow<List<Instrument>> = repository
        .getInstruments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val tunings: StateFlow<List<TuningWithInstrumentAndSounds>> = repository
        .getTunings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val instrumentRows: StateFlow<List<InstrumentRow>> = combine(
        instruments,
        tunings
    ) { instrumentList, tuningList ->
        instrumentList.map { instrument ->
            InstrumentRow(
                instrument = instrument,
                tuningCount = tuningList.count { it.instrumentId == instrument.instrumentId }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _selectedInstrument = MutableStateFlow<String?>(null)
    val selectedInstrument: StateFlow<String?> = _selectedInstrument.asStateFlow()

    fun setSelectedInstrument(instrument: String?) {
        _selectedInstrument.value = instrument
    }

    fun deleteTuning(tuningId: Int) {
        viewModelScope.launch {
            repository.deleteTuningById(tuningId)
        }
    }

    val filteredTunings: StateFlow<List<TuningWithInstrumentAndSounds>> = combine(
        tunings,
        _searchQuery,
        _selectedInstrument
    ) { tuningList, query, instrument ->
        tuningList.filter { tuning ->
            val matchesInstrument = instrument == null ||
                    tuning.instrumentName == instrument
            val matchesQuery = query.isEmpty() ||
                    tuning.tuningName.contains(query, ignoreCase = true)
            matchesInstrument && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun createInstrument(name: String, stringCount: Int) {
        val trimmed = name.trim()
        if (trimmed.isBlank() || stringCount !in 2..24) return
        viewModelScope.launch {
            repository.insertInstrument(
                Instrument(name = trimmed, soundsCount = stringCount.toByte())
            )
        }
    }

    fun updateInstrument(instrument: Instrument, newName: String, newStringCount: Int) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || newStringCount !in 2..24) return

        val tuningCount = tunings.value.count { it.instrumentId == instrument.instrumentId }
        val updated = if (tuningCount > 0) {
            instrument.copy(name = trimmed)
        } else {
            instrument.copy(name = trimmed, soundsCount = newStringCount.toByte())
        }

        viewModelScope.launch {
            repository.updateInstrument(updated)
            if (_selectedInstrument.value == instrument.name && trimmed != instrument.name) {
                _selectedInstrument.value = trimmed
            }
        }
    }

    fun deleteInstrument(instrument: Instrument) {
        val tuningCount = tunings.value.count { it.instrumentId == instrument.instrumentId }
        if (tuningCount > 0) return
        viewModelScope.launch {
            repository.deleteInstrument(instrument)
            if (_selectedInstrument.value == instrument.name) {
                _selectedInstrument.value = null
            }
        }
    }
}
