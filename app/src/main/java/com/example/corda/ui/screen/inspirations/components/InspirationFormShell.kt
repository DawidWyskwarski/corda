package com.example.corda.ui.screen.inspirations.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.corda.data.inspirations.local.entities.MediaType

/**
 * Scrollable inspiration form layout with edge-to-edge hero and overlay navigation.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InspirationFormShell(
    isEditing: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    saveEnabled: Boolean = true,
    showHeroEditOverlay: Boolean = false,
    mediaPath: String? = null,
    mediaType: MediaType? = null,
    onEditMediaClick: (() -> Unit)? = null,
    onEditMediaLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding(),
            ) {
                InspirationHeroImage(
                    mediaPath = mediaPath,
                    mediaType = mediaType,
                    showEditOverlay = showHeroEditOverlay,
                    playVideoWhenVisible = !showHeroEditOverlay && mediaType != null,
                    onEditMediaClick = onEditMediaClick,
                    onEditMediaLongClick = onEditMediaLongClick,
                )
                content()
            }

            InspirationFormTopBar(
                isEditing = isEditing,
                onBack = onBack,
                onEdit = onEdit,
                onSave = onSave,
                saveEnabled = saveEnabled,
            )
        }
    }
}
