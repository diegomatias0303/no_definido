package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.ProgressRepository
import com.example.no_definido.data.UserProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ProgressRepository = FirebaseProgressRepository(),
    private val customScope: CoroutineScope? = null
) : ViewModel() {

    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: StateFlow<UserProgress?> = _userProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentUserId: String = "jugador_invitado"

    init {
        fetchProgress(currentUserId)
    }

    fun setUserId(userId: String) {
        currentUserId = userId
        fetchProgress(userId)
    }

    fun fetchProgress(userId: String = currentUserId) {
        val scope = customScope ?: try {
            viewModelScope
        } catch (_: Throwable) {
            null
        }
        if (scope == null) {
            _userProgress.value = UserProgress(userId = userId)
            _isLoading.value = false
            return
        }
        try {
            scope.launch {
                _isLoading.value = true
                val progress = repository.getProgress(userId)
                _userProgress.value = progress ?: UserProgress(userId = userId)
                _isLoading.value = false
            }
        } catch (_: Throwable) {
            _userProgress.value = UserProgress(userId = userId)
            _isLoading.value = false
        }
    }
}
