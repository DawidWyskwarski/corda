package com.example.corda.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed

/**
 * Common button for back navigation.
 */
@Composable
fun NavigateBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = dropUnlessResumed { onClick() },
        modifier = modifier
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
    }
}
