package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.ProgressRepository
import com.example.no_definido.data.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val email: String = "",
    val pinOrPassword: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LoginViewModel(
    private val repository: ProgressRepository = FirebaseProgressRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(pinOrPassword = value, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun submit(onSuccess: (userId: String) -> Unit) {
        val state = _uiState.value
        val cleanedUsername = state.username.trim()

        if (cleanedUsername.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "¡Por favor escribe tu nombre de usuario!") }
            return
        }

        val safeUserId = cleanedUsername.lowercase().replace(" ", "_")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                if (state.isRegisterMode) {
                    val existing = repository.getProgress(safeUserId)
                    if (existing != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "El usuario ya existe. ¡Inicia sesión o elige otro nombre!"
                            )
                        }
                        return@launch
                    }

                    val newProgress = UserProgress(
                        userId = safeUserId,
                        applesCollectedTotal = 0,
                        objectGamesCompleted = 0,
                        totalScore = 0,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.saveProgress(newProgress)
                    _uiState.update { it.copy(isLoading = false, successMessage = "¡Cuenta creada!") }
                    onSuccess(safeUserId)
                } else {
                    val progress = repository.getProgress(safeUserId)
                    if (progress == null) {
                        val initialProgress = UserProgress(userId = safeUserId)
                        repository.saveProgress(initialProgress)
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(safeUserId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de red: ${e.localizedMessage ?: "Revisa tu conexión"}"
                    )
                }
            }
        }
    }

    fun playAsGuest(onSuccess: (userId: String) -> Unit) {
        onSuccess("jugador_invitado")
    }
}
