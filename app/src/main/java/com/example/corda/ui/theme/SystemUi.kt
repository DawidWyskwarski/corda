package com.example.corda.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable

fun applyWindowTheme(activity: Activity, isDark: Boolean) {
    val background = if (isDark) backgroundDark else backgroundLight
    activity.window.setBackgroundDrawable(background.toArgb().toDrawable())
}
