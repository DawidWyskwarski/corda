package com.example.corda.ui.screen.inspirations.detail

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.ui.screen.inspirations.InspirationsViewModel
import com.example.corda.ui.screen.inspirations.components.InspirationFormContent
import com.example.corda.ui.screen.inspirations.components.InspirationFormShell

/**
 * Inspiration detail screen — read-only view of a single inspiration.
 *
 * @param id the inspiration id to display
 * @param onBack navigate back
 * @param onEdit navigate to the edit screen for this inspiration
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspirationDetailScreen(
    id: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspirationsViewModel = hiltViewModel(),
) {
    val inspiration by viewModel.detailInspiration.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.loadInspiration(id)
    }

    val item = inspiration ?: return
    val entity = item.inspiration

    InspirationFormShell(
        modifier = modifier,
        isEditing = false,
        onBack = onBack,
        onEdit = { onEdit(entity.inspirationId) },
        mediaPath = entity.mediaPath,
        mediaType = entity.mediaType,
    ) {
        InspirationFormContent(
            inspiration = item,
            isEditing = false,
        )
    }
}
