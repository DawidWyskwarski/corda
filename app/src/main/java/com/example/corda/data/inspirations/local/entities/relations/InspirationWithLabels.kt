package com.example.corda.data.inspirations.local.entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity

data class InspirationWithLabels(
    @Embedded
    val inspiration: InspirationEntity,
    @Relation(
        parentColumn = "inspiration_id",
        entityColumn = "label_id",
        associateBy = Junction(InspirationLabelCrossRef::class),
    )
    val labels: List<LabelEntity>,
)
