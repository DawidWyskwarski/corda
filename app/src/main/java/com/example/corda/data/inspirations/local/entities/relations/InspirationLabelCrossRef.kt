package com.example.corda.data.inspirations.local.entities.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity

@Entity(
    tableName = "InspirationLabelCrossRef",
    primaryKeys = ["inspiration_id", "label_id"],
    indices = [Index(value = ["label_id"])],
    foreignKeys = [
        ForeignKey(
            entity = InspirationEntity::class,
            parentColumns = ["inspiration_id"],
            childColumns = ["inspiration_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LabelEntity::class,
            parentColumns = ["label_id"],
            childColumns = ["label_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class InspirationLabelCrossRef(
    @ColumnInfo(name = "inspiration_id")
    val inspirationId: Long,
    @ColumnInfo(name = "label_id")
    val labelId: Long,
)
