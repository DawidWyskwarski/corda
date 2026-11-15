package com.example.corda.tuner.data.repository

import com.example.corda.tuner.data.local.dao.InstrumentDao
import com.example.corda.tuner.data.local.dao.MusicNoteDao
import com.example.corda.tuner.data.local.dao.TuningDao
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName
import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.data.local.entities.Tuning
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TunerRepositoryImplTest {

    private val musicNoteDao = mockk<MusicNoteDao>()
    private val instrumentDao = mockk<InstrumentDao>()
    private val tuningDao = mockk<TuningDao>()
    private val fixedNow = 23L

    private lateinit var repository: TunerRepositoryImpl

    private val sampleInstrument = Instrument(
        id = 1,
        customName = "Guitar",
        defaultName = DefaultInstrumentName.GUITAR,
        musicNotesCount = 6,
    )
    private val sampleTuning = Tuning(
        id = 10,
        name = "E Standard",
        instrumentId = 1,
        lastUsed = 0L,
    )
    private val sampleNotes = listOf(MusicNote(midiNum = 60, name = "C", octave = 4))

    @Before
    fun setUp() {
        repository = TunerRepositoryImpl(
            musicNoteDao,
            instrumentDao,
            tuningDao,
            currentTimeMillis = { fixedNow },
        )
    }

    @Test
    fun updateTuningLastUsed_forwardsInjectedTimestamp() = runTest {
        coEvery { tuningDao.updateTuningLastUsed(10, fixedNow) } just runs

        val result = repository.updateTuningLastUsed(10)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { tuningDao.updateTuningLastUsed(10, fixedNow) }
    }

    @Test
    fun writeMethods_wrapDaoExceptionsInResultFailure() = runTest {
        val boom = RuntimeException("boom")
        coEvery { instrumentDao.insertInstrument(any()) } throws boom
        coEvery { instrumentDao.updateInstrument(any()) } throws boom
        coEvery { instrumentDao.deleteInstrument(any()) } throws boom
        coEvery { tuningDao.insertTuningWithSounds(any(), any()) } throws boom
        coEvery { tuningDao.updateTuning(any(), any()) } throws boom
        coEvery { tuningDao.deleteTuning(any()) } throws boom
        coEvery { tuningDao.updateTuningLastUsed(any(), any()) } throws boom

        val failures = listOf(
            repository.insertInstrument(sampleInstrument),
            repository.updateInstrument(sampleInstrument),
            repository.deleteInstrument(1),
            repository.insertTuning(sampleTuning, sampleNotes),
            repository.updateTuning(sampleTuning, sampleNotes),
            repository.deleteTuning(10),
            repository.updateTuningLastUsed(10),
        )

        failures.forEach { result ->
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isSameInstanceAs(boom)
        }
    }
}
