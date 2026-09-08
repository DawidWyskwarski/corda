package com.example.corda.data.tuner.repository

import com.example.corda.data.tuner.local.dao.InstrumentDao
import com.example.corda.data.tuner.local.dao.MusicNoteDao
import com.example.corda.data.tuner.local.dao.TuningDao
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.models.TuningDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TunerRepositoryImpl @Inject constructor(
    private val musicNoteDao: MusicNoteDao,
    private val instrumentDao: InstrumentDao,
    private val tuningDao: TuningDao,
) : TunerRepository {
    // === Sounds ===
    override suspend fun getAllSounds(): List<MusicNote> = musicNoteDao.getAllNotes()

    // === Instruments ===
    override fun getInstrumentsFlow(): Flow<List<Instrument>> = instrumentDao.getInstruments()

    override suspend fun insertInstrument(instrument: Instrument): Result<Long> = runCatching {
        instrumentDao.insertInstrument(instrument)
    }

    override suspend fun updateInstrument(instrument: Instrument): Result<Unit> = runCatching {
        instrumentDao.updateInstrument(instrument)
    }

    override suspend fun deleteInstrument(instrumentId: Int): Result<Unit> = runCatching {
        instrumentDao.deleteInstrument(instrumentId)
    }

    // === Tunings ===
    override fun getAllTunings(): Flow<List<TuningDetails>> = tuningDao.getTunings()

    override suspend fun getTuning(tuningId: Int): TuningDetails? = tuningDao.getTuningDetails(tuningId)

    override fun getMostRecentTuning(): Flow<TuningDetails?> = tuningDao.getMostRecentTuning()

    override suspend fun insertTuning(tuning: Tuning, musicNotes: List<MusicNote>): Result<Unit> = runCatching {
        tuningDao.insertTuningWithSounds(tuning, musicNotes)
    }

    override suspend fun updateTuningLastUsed(tuningId: Int): Result<Unit> = runCatching {
        tuningDao.updateTuningLastUsed(tuningId, System.currentTimeMillis())
    }

    override suspend fun updateTuning(tuning: Tuning, musicNotes: List<MusicNote>): Result<Unit> = runCatching {
        tuningDao.updateTuning(tuning, musicNotes)
    }

    override suspend fun deleteTuning(tuningId: Int): Result<Unit> = runCatching {
        tuningDao.deleteTuning(tuningId)
    }

}
