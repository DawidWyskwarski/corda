package com.example.corda.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Custom icon button that prevents multiple clicks.
 *
 * @param modifier Modifier for the button
 * @param onClick Callback to invoke when the button is clicked
 */
@Composable
fun SingleClickIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(true) }

    IconButton(
        onClick = {
            // I could use IconButton enabled param, but I don't like how the button gets grayed out
            if (isEnabled) {
                isEnabled = false
                onClick()
            }
        }
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
    }
}
