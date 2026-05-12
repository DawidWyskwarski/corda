package com.example.corda.data.tuner.repository

import com.example.corda.data.tuner.local.dao.TunerDao
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TunerRepository(
    private val dao: TunerDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // Instruments
    fun getInstruments(): Flow<List<Instrument>> =
        dao.getInstruments()
            .flowOn(dispatcher)
            .catch { _ -> emit(emptyList()) }

    suspend fun insertInstrument(instrument: Instrument): Result<Long> = withContext(dispatcher) {
        runCatching { dao.insertInstrument(instrument) }
    }

    suspend fun updateInstrument(vararg instrument: Instrument): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.updateInstrument(*instrument) }
    }

    suspend fun deleteInstrument(vararg instrument: Instrument): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.deleteInstrument(*instrument) }
    }

    // Tunings
    fun getTunings(): Flow<List<TuningWithInstrumentAndSounds>> =
        dao.getTunings()
            .flowOn(dispatcher)
            .catch { emit(emptyList()) }

    suspend fun insertTuning(tuning: Tuning): Result<Long> = withContext(dispatcher) {
        runCatching { dao.insertTuning(tuning) }
    }

    suspend fun updateTuning(vararg tuning: Tuning): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.updateTuning(*tuning) }
    }

    suspend fun updateTuningLastUsed(tuningId: Int, timestamp: Long): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.updateTuningLastUsed(tuningId, timestamp) }
    }

    suspend fun deleteTuning(vararg tuning: Tuning): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.deleteTuning(*tuning) }
    }

    suspend fun deleteTuningById(tuningId: Int): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.deleteTuningById(tuningId) }
    }

    // Sound
    suspend fun getReferencePitch(): Sound = withContext(dispatcher) {
        dao.getReferencePitch()
    }

    suspend fun getAllSounds(): List<Sound> = withContext(dispatcher) {
        dao.getAllSounds()
    }

    suspend fun updateSound(vararg sound: Sound): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.updateSound(*sound) }
    }

    // Tuning-Sound
    suspend fun insertTuningWithSounds(
        name: String,
        instrumentId: Int,
        sounds: List<Sound>
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            dao.insertTuningWithSounds(
                tuning = Tuning(name = name, instrumentId = instrumentId),
                sounds = sounds
            )
        }
    }

    suspend fun getTuningWithSoundsById(tuningId: Int): TuningWithInstrumentAndSounds? =
        withContext(dispatcher) {
            dao.getTuningWithSoundsById(tuningId)
        }

    suspend fun updateTuningWithSounds(
        tuningId: Int,
        name: String,
        sounds: List<Sound>
    ): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.updateTuningWithSounds(tuningId, name, sounds) }
    }
}
