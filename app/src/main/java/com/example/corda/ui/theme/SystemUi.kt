package com.example.corda.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SystemUiEffect(
    darkTheme: Boolean,
    backgroundColor: Color,
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        val window = (view.context as Activity).window

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

fun applyWindowTheme(activity: Activity, isDark: Boolean) {
    val background = if (isDark) backgroundDark else backgroundLight
    activity.window.setBackgroundDrawable(ColorDrawable(background.toArgb()))
}
