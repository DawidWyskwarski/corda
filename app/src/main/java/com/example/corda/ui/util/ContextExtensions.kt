package com.example.corda.ui.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) { //TODO after changing the view model scope this will become uselesss
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
