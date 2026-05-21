package com.example.corda.ui.screen.inspirations.components

import com.example.corda.data.inspirations.local.entities.LabelEntity

data class InspirationEditActions(
    val onNameChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onRemoveLabel: (LabelEntity) -> Unit,
    val onAddLabel: (LabelEntity) -> Unit,
    val onDelete: () -> Unit,
)
