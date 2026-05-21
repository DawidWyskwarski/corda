package com.example.corda.data.inspirations.local

import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels

val InspirationWithLabels.labelNames: List<String>
    get() = labels.map { it.name }

fun emptyInspirationWithLabels(): InspirationWithLabels = InspirationWithLabels(
    inspiration = InspirationEntity(name = "", description = ""),
    labels = emptyList(),
)
