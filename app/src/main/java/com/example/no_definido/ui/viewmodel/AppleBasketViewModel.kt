package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            
            // Si el niño ganó, subimos los puntos a Firebase
            if (isFinished) {
                saveWinToFirebase()
            }
        }
    }

    private fun saveWinToFirebase() {
        // Ejecutamos la subida a internet en segundo plano para no trabar la pantalla
        viewModelScope.launch {
            val repo = FirebaseProgressRepository()
            
            // Primero intentamos descargar su progreso actual
            val currentProgress = repo.getProgress("jugador_invitado") ?: UserProgress()
            
            // Le sumamos las manzanas que acaba de ganar y 50 puntos
            val updatedProgress = currentProgress.copy(
                applesCollectedTotal = currentProgress.applesCollectedTotal + 5,
                totalScore = currentProgress.totalScore + 50,
                lastUpdated = System.currentTimeMillis()
            )
            
            // Lo enviamos como JSON a la nube de Firebase
            repo.saveProgress(updatedProgress)
        }
    }

    fun dismissRewardDialog() {
        _uiState.update { it.copy(showRewardDialog = false) }
    }

    fun resetGame() {
        _uiState.value = AppleBasketUiState()
    }
}
