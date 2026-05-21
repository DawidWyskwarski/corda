package com.example.corda.data.inspirations.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationLabelCrossRef
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface InspirationsDao {

    // Inspiration reads
    @Transaction
    @Query("SELECT * FROM Inspiration ORDER BY inspiration_id DESC")
    fun observeInspirationsWithLabels(): Flow<List<InspirationWithLabels>>

    @Transaction
    @Query("SELECT * FROM Inspiration WHERE inspiration_id = :id")
    suspend fun getInspirationWithLabelsById(id: Long): InspirationWithLabels?

    // Label reads
    @Query("SELECT * FROM Label ORDER BY name ASC")
    fun observeLabels(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM Label WHERE name = :name LIMIT 1")
    suspend fun getLabelByName(name: String): LabelEntity?

    @Query("SELECT * FROM Label WHERE label_id = :labelId")
    suspend fun getLabelById(labelId: Long): LabelEntity?

    // Inspiration writes
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInspiration(inspiration: InspirationEntity): Long

    @Update
    suspend fun updateInspiration(inspiration: InspirationEntity)

    @Query("DELETE FROM Inspiration WHERE inspiration_id = :inspirationId")
    suspend fun deleteInspirationById(inspirationId: Long)

    // Label writes
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLabel(label: LabelEntity): Long

    @Update
    suspend fun updateLabel(label: LabelEntity)

    @Query("DELETE FROM Label WHERE label_id = :labelId")
    suspend fun deleteLabelById(labelId: Long)

    @Query("UPDATE Label SET name = :newName WHERE label_id = :labelId")
    suspend fun updateLabelName(labelId: Long, newName: String)

    // Junction writes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<InspirationLabelCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: InspirationLabelCrossRef)

    @Query("DELETE FROM InspirationLabelCrossRef WHERE inspiration_id = :inspirationId")
    suspend fun deleteCrossRefsForInspiration(inspirationId: Long)

    @Transaction
    suspend fun insertInspirationWithLabels(
        inspiration: InspirationEntity,
        labelIds: List<Long>,
    ): Long {
        val inspirationId = insertInspiration(inspiration)
        labelIds.forEach { labelId ->
            insertCrossRef(
                InspirationLabelCrossRef(
                    inspirationId = inspirationId,
                    labelId = labelId,
                ),
            )
        }
        return inspirationId
    }

    @Transaction
    suspend fun updateInspirationWithLabels(
        inspiration: InspirationEntity,
        labelIds: List<Long>,
    ) {
        updateInspiration(inspiration)
        deleteCrossRefsForInspiration(inspiration.inspirationId)
        labelIds.forEach { labelId ->
            insertCrossRef(
                InspirationLabelCrossRef(
                    inspirationId = inspiration.inspirationId,
                    labelId = labelId,
                ),
            )
        }
    }

    @Transaction
    suspend fun replaceLabelsForInspiration(inspirationId: Long, labelIds: List<Long>) {
        deleteCrossRefsForInspiration(inspirationId)
        labelIds.forEach { labelId ->
            insertCrossRef(
                InspirationLabelCrossRef(
                    inspirationId = inspirationId,
                    labelId = labelId,
                ),
            )
        }
    }
}
