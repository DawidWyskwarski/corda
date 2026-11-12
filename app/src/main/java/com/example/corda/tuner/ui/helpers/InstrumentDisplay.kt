package com.example.corda.tuner.ui.helpers

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.corda.R
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName.BASS
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName.GUITAR
import com.example.corda.tuner.data.local.entities.Instrument

sealed interface InstrumentName {
    data class Custom(private val name: String) : InstrumentName {
        @Composable
        override fun get(): String = name
    }

    data class BuildIn(@StringRes private val resId: Int) : InstrumentName {
        @Composable
        override fun get(): String = stringResource(resId)
    }

    object Default : InstrumentName {
        @Composable
        override fun get(): String = ""
    }

    @Composable
    fun get(): String
}

@Composable
fun Instrument.displayNameOnly(): String = resolveInstrumentName(customName, defaultName).get()

@Composable
fun Instrument.displayNameWithNoteCount(): String =
    "${displayNameOnly()} (${pluralStringResource(R.plurals.tuner_instrument_string_count, musicNotesCount.toInt(), musicNotesCount.toInt())})"

fun resolveInstrumentName(customName: String?, defaultName: DefaultInstrumentName?): InstrumentName {
    return customName?.let { InstrumentName.Custom(it) }
        ?: defaultName?.let {

            val id = when (it) {
                GUITAR -> R.string.tuner_instrument_builtin_guitar_name
                BASS -> R.string.tuner_instrument_builtin_bass_name
            }

            InstrumentName.BuildIn(id)
        }
        ?: InstrumentName.Default
}
