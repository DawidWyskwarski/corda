package com.example.corda.core.ui.system

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.drawable.toDrawable
import com.example.corda.core.ui.theme.backgroundDark
import com.example.corda.core.ui.theme.backgroundLight

fun applyWindowTheme(activity: Activity, isDark: Boolean) {
    val background = if (isDark) backgroundDark else backgroundLight
    activity.window.setBackgroundDrawable(background.toArgb().toDrawable())
}

/**
 * Dialogs and modal bottom sheets render in their own window, which doesn't inherit the
 * edge-to-edge setup `enableEdgeToEdge` applies to the activity window. Contrast enforcement is
 * therefore still on there, and the platform paints a scrim behind the transparent navigation bar.
 *
 * Call from inside the dialog or sheet content.
 */
@Composable
fun TransparentNavigationBarEffect() {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dialogWindow?.isNavigationBarContrastEnforced = false
        }
    }
}
