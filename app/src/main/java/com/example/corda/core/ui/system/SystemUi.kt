package com.example.corda.core.ui.system

import android.app.Activity
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable
import com.example.corda.core.ui.theme.backgroundDark
import com.example.corda.core.ui.theme.backgroundLight

fun applyWindowTheme(activity: Activity, isDark: Boolean) {
    val background = if (isDark) backgroundDark else backgroundLight
    activity.window.setBackgroundDrawable(background.toArgb().toDrawable())
}
