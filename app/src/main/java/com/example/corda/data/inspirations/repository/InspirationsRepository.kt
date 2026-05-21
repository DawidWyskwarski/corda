package com.example.corda.data.inspirations.repository

import com.example.corda.data.inspirations.local.dao.InspirationsDao
import com.example.corda.data.inspirations.media.InspirationMediaStore
import com.example.corda.data.inspirations.media.InspirationMediaThumbnailer
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class InspirationsRepository(
    private val dao: InspirationsDao,
    private val mediaStore: InspirationMediaStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun observeInspirations(): Flow<List<InspirationWithLabels>> =
        dao.observeInspirationsWithLabels()
            .flowOn(dispatcher)
            .catch { emit(emptyList()) }

    fun observeLabels(): Flow<List<LabelEntity>> =
        dao.observeLabels()
            .flowOn(dispatcher)
            .catch { emit(emptyList()) }

    suspend fun getInspiration(id: Long): InspirationWithLabels? = withContext(dispatcher) {
        dao.getInspirationWithLabelsById(id)
    }

    /**
     * Generates missing grid thumbnails for inspirations saved before previews existed.
     */
    suspend fun backfillMissingThumbnails(inspirations: List<InspirationWithLabels>) =
        withContext(dispatcher) {
            for (item in inspirations) {
                val entity = item.inspiration
                val mediaPath = entity.mediaPath ?: continue
                val mediaType = entity.mediaType ?: continue
                if (!entity.thumbnailPath.isNullOrBlank() && File(entity.thumbnailPath).exists()) {
                    continue
                }
                val thumbFile = mediaStore.thumbnailFileForOriginal(mediaPath)
                val thumb = InspirationMediaThumbnailer.createThumbnail(
                    sourcePath = mediaPath,
                    type = mediaType,
                    destFile = thumbFile,
                ).getOrNull() ?: continue
                dao.updateInspiration(
                    entity.copy(
                        thumbnailPath = thumb.path,
                        mediaAspectRatio = entity.mediaAspectRatio ?: thumb.aspectRatio,
                    ),
                )
            }
        }

    suspend fun saveInspiration(
        inspiration: InspirationEntity,
        labelNames: List<String>,
    ): Result<Long> = withContext(dispatcher) {
        runCatching {
            val labelIds = resolveLabelIds(labelNames)
            val previous = if (inspiration.inspirationId != 0L) {
                dao.getInspirationWithLabelsById(inspiration.inspirationId)?.inspiration
            } else {
                null
            }
            val savedId = if (inspiration.inspirationId == 0L) {
                dao.insertInspirationWithLabels(inspiration, labelIds)
            } else {
                dao.updateInspirationWithLabels(inspiration, labelIds)
                inspiration.inspirationId
            }
            val previousPath = previous?.mediaPath
            if (previousPath != null && previousPath != inspiration.mediaPath) {
                mediaStore.deleteManagedFiles(previousPath, previous.thumbnailPath)
            }
            savedId
        }
    }

    suspend fun deleteInspiration(id: Long): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val existing = dao.getInspirationWithLabelsById(id)
            val inspiration = existing?.inspiration
            mediaStore.deleteManagedFiles(
                inspiration?.mediaPath,
                inspiration?.thumbnailPath,
            )
            dao.deleteInspirationById(id)
        }
    }

    suspend fun createLabel(name: String): Result<Long> = withContext(dispatcher) {
        runCatching {
            val trimmed = name.trim()
            require(trimmed.isNotEmpty()) { "Label name must not be blank." }
            dao.getLabelByName(trimmed)?.labelId
                ?: dao.insertLabel(LabelEntity(name = trimmed))
        }
    }

    suspend fun renameLabel(labelId: Long, newName: String): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val trimmed = newName.trim()
            require(trimmed.isNotEmpty()) { "Label name must not be blank." }
            dao.updateLabelName(labelId, trimmed)
        }
    }

    suspend fun deleteLabel(labelId: Long): Result<Unit> = withContext(dispatcher) {
        runCatching { dao.deleteLabelById(labelId) }
    }

    suspend fun renameLabelByName(oldName: String, newName: String): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val label = dao.getLabelByName(oldName)
                ?: error("Label not found: $oldName")
            renameLabel(label.labelId, newName).getOrThrow()
        }
    }

    suspend fun deleteLabelByName(name: String): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val label = dao.getLabelByName(name)
                ?: error("Label not found: $name")
            deleteLabel(label.labelId).getOrThrow()
        }
    }

    private suspend fun resolveLabelIds(labelNames: List<String>): List<Long> =
        labelNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .map { name ->
                dao.getLabelByName(name)?.labelId
                    ?: dao.insertLabel(LabelEntity(name = name))
            }
}
