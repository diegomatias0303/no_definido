package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppleBasketUiState(
    val targetCount: Int = 5,
    val currentCount: Int = 0,
    val applesRemaining: Int = 5,
    val isCompleted: Boolean = false,
    val showRewardDialog: Boolean = false
)

class AppleBasketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppleBasketUiState())
    val uiState: StateFlow<AppleBasketUiState> = _uiState.asStateFlow()

    fun addApple() {
        val state = _uiState.value
        if (state.currentCount < state.targetCount) {
            val newCount = state.currentCount + 1
            val isFinished = newCount == state.targetCount
            _uiState.update {
                it.copy(
                    currentCount = newCount,
                    applesRemaining = state.targetCount - newCount,
                    isCompleted = isFinished,
                    showRewardDialog = isFinished
                )
            }
        }
    }

    fun dismissRewardDialog() {
        _uiState.update { it.copy(showRewardDialog = false) }
    }

    fun resetGame() {
        _uiState.value = AppleBasketUiState()
    }
}
