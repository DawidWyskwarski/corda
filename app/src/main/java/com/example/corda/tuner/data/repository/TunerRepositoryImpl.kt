package com.example.corda.tuner.data.repository

import com.example.corda.tuner.data.local.dao.InstrumentDao
import com.example.corda.tuner.data.local.dao.MusicNoteDao
import com.example.corda.tuner.data.local.dao.TuningDao
import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.data.local.entities.Tuning
import com.example.corda.tuner.data.local.models.CurrentTuning
import com.example.corda.tuner.data.local.models.TuningDetails
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TunerRepositoryImpl(
    private val musicNoteDao: MusicNoteDao,
    private val instrumentDao: InstrumentDao,
    private val tuningDao: TuningDao,
    private val currentTimeMillis: () -> Long,
) : TunerRepository {

    @Inject
    constructor(
        musicNoteDao: MusicNoteDao,
        instrumentDao: InstrumentDao,
        tuningDao: TuningDao,
    ) : this(musicNoteDao, instrumentDao, tuningDao, { System.currentTimeMillis() })

    // === Sounds ===
    override suspend fun getAllSounds(): List<MusicNote> = musicNoteDao.getAllNotes()

    // === Instruments ===
    override fun getInstrumentsFlow(): Flow<List<Instrument>> = instrumentDao.getInstruments()

    override suspend fun insertInstrument(instrument: Instrument): Result<Unit> = runCatching {
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

    override suspend fun getTuning(tuningId: Int): TuningDetails? =
        tuningDao.getTuningDetails(tuningId)

    override fun getMostRecentTuning(): Flow<CurrentTuning?> = tuningDao.getMostRecentTuning()

    override suspend fun insertTuning(tuning: Tuning, musicNotes: List<MusicNote>): Result<Unit> =
        runCatching {
            tuningDao.insertTuningWithSounds(tuning, musicNotes)
        }

    override suspend fun updateTuningLastUsed(tuningId: Int): Result<Unit> = runCatching {
        tuningDao.updateTuningLastUsed(tuningId, currentTimeMillis())
    }

    override suspend fun updateTuning(tuning: Tuning, musicNotes: List<MusicNote>): Result<Unit> =
        runCatching {
            tuningDao.updateTuning(tuning, musicNotes)
        }

    override suspend fun deleteTuning(tuningId: Int): Result<Unit> = runCatching {
        tuningDao.deleteTuning(tuningId)
    }
}
