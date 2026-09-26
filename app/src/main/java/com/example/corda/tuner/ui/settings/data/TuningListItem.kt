package com.example.corda.tuner.ui.settings.data

import androidx.compose.runtime.Composable
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.ui.helpers.InstrumentName

data class TuningListItem (
    val tuningId: Int,
    val tuningName: String,
    val instrumentId: Int,
    private val _instrumentName: InstrumentName,
    val notes: List<MusicNote>
) {
    val instrumentName: String
        @Composable
        get() = _instrumentName.get()
}
