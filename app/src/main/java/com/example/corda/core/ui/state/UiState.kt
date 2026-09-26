package com.example.corda.core.ui.state

import androidx.annotation.StringRes

sealed interface UiState<out T> {
    data object Loading: UiState<Nothing>
    data class Error(@StringRes val errorRes: Int): UiState<Nothing>
    data class Loaded<T>(val data: T): UiState<T>
}
