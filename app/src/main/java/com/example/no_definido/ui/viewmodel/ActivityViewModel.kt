package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.no_definido.data.ActivityStep
import com.example.no_definido.data.HomeArea
import com.example.no_definido.data.MockDataRepository
import com.example.no_definido.ui.components.DropZoneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ActivityUiState(
    val steps: List<ActivityStep> = emptyList(),
    val areas: List<HomeArea> = emptyList(),
    val currentStepIndex: Int = 0,
    val selectedAreaStates: Map<String, DropZoneState> = emptyMap(),
    val isCurrentStepCompleted: Boolean = false,
    val isActivityCompleted: Boolean = false,
    val showRewardDialog: Boolean = false,
    val attempts: Int = 0,
    val score: Int = 0
) {
    val currentStep: ActivityStep?
        get() = steps.getOrNull(currentStepIndex)

    val totalSteps: Int
        get() = steps.size
}

class ActivityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadActivityData()
    }

    fun loadActivityData() {
        val steps = MockDataRepository.getSampleActivitySteps()
        val areas = MockDataRepository.sampleAreas
        _uiState.value = ActivityUiState(
            steps = steps,
            areas = areas,
            currentStepIndex = 0,
            selectedAreaStates = areas.associate { it.id to DropZoneState.DEFAULT }
        )
    }

    fun selectAreaTarget(areaId: String) {
        val currentState = _uiState.value
        val step = currentState.currentStep ?: return
        if (currentState.isCurrentStepCompleted) return

        val isCorrect = areaId == step.correctAreaId

        val updatedMap = currentState.selectedAreaStates.toMutableMap()
        if (isCorrect) {
            updatedMap[areaId] = DropZoneState.CORRECT
            _uiState.update { state ->
                state.copy(
                    selectedAreaStates = updatedMap,
                    isCurrentStepCompleted = true,
                    score = state.score + 10,
                    showRewardDialog = true
                )
            }
        } else {
            updatedMap[areaId] = DropZoneState.INCORRECT
            _uiState.update { state ->
                state.copy(
                    selectedAreaStates = updatedMap,
                    attempts = state.attempts + 1
                )
            }
        }
    }

    fun dismissRewardDialog() {
        _uiState.update { it.copy(showRewardDialog = false) }
        nextStep()
    }

    fun nextStep() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentStepIndex + 1

        if (nextIndex < currentState.totalSteps) {
            _uiState.update { state ->
                state.copy(
                    currentStepIndex = nextIndex,
                    isCurrentStepCompleted = false,
                    selectedAreaStates = state.areas.associate { it.id to DropZoneState.DEFAULT }
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    isActivityCompleted = true
                )
            }
        }
    }

    fun restartActivity() {
        loadActivityData()
    }
}
