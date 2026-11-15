package com.example.no_definido.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.no_definido.data.ActivityStep
import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.HomeArea
import com.example.no_definido.data.HouseRoomId
import com.example.no_definido.data.HouseSortingRound
import com.example.no_definido.data.MockDataRepository
import com.example.no_definido.data.ProgressRepository
import com.example.no_definido.data.UserProgress
import com.example.no_definido.data.VisualHouseItem
import com.example.no_definido.ui.components.DropZoneState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DropResult {
    data class Correct(
        val item: VisualHouseItem,
        val room: HouseRoomId,
        val isRoundFinished: Boolean,
        val isGameFinished: Boolean
    ) : DropResult()

    data class Incorrect(
        val item: VisualHouseItem,
        val attemptedRoom: HouseRoomId,
        val expectedRoom: HouseRoomId
    ) : DropResult()

    object Ignored : DropResult()
}

data class ActivityUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val starsEarned: Int = 0,
    val currentRoundData: HouseSortingRound? = null,
    val currentItemIndex: Int = 0,
    val placedItemsByRoom: Map<HouseRoomId, List<String>> = HouseRoomId.entries.associateWith { emptyList() },
    val isRoundCompleted: Boolean = false,
    val isActivityCompleted: Boolean = false,
    val celebrationActive: Boolean = false,
    val showRewardDialog: Boolean = false,
    val score: Int = 0,
    val activeUserId: String = "jugador_invitado",
    val steps: List<ActivityStep> = emptyList(),
    val areas: List<HomeArea> = emptyList(),
    val currentStepIndex: Int = 0,
    val selectedAreaStates: Map<String, DropZoneState> = emptyMap(),
    val isCurrentStepCompleted: Boolean = false,
    val attempts: Int = 0
) {
    val currentItem: VisualHouseItem?
        get() = currentRoundData?.items?.getOrNull(currentItemIndex)

    val activeRooms: Set<HouseRoomId>
        get() = currentRoundData?.activeRooms ?: HouseRoomId.entries.toSet()

    val totalItemsInRound: Int
        get() = currentRoundData?.items?.size ?: 0

    val currentStep: ActivityStep?
        get() = steps.getOrNull(currentStepIndex)

    val totalSteps: Int
        get() = steps.size
}

class ActivityViewModel(
    private val progressRepo: ProgressRepository? = null,
    private val customScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private var rounds: List<HouseSortingRound> = MockDataRepository.getHouseSortingRounds(randomize = false)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    // Mini-Game ViewModel Contract
    val totalRounds: Int = 3
    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _starsEarned = MutableStateFlow(0)
    val starsEarned: StateFlow<Int> = _starsEarned.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    private fun createInitialState(): ActivityUiState {
        val r1 = rounds.firstOrNull() ?: MockDataRepository.getHouseSortingRounds()[0]
        return ActivityUiState(
            currentRound = 1,
            totalRounds = 3,
            starsEarned = 0,
            currentRoundData = r1,
            currentItemIndex = 0,
            placedItemsByRoom = HouseRoomId.entries.associateWith { emptyList() },
            isRoundCompleted = false,
            isActivityCompleted = false,
            celebrationActive = false,
            showRewardDialog = false,
            score = 0
        )
    }

    fun setActiveUserId(userId: String) {
        _uiState.update { it.copy(activeUserId = userId) }
    }

    fun onItemDroppedInRoom(roomChosen: HouseRoomId): DropResult {
        val state = _uiState.value
        if (state.isRoundCompleted || state.isActivityCompleted) return DropResult.Ignored
        val item = state.currentItem ?: return DropResult.Ignored

        if (roomChosen == item.targetRoom) {
            val currentPlaced = state.placedItemsByRoom[roomChosen]?.toMutableList() ?: mutableListOf()
            currentPlaced.add(item.id)
            val updatedMap = state.placedItemsByRoom.toMutableMap()
            updatedMap[roomChosen] = currentPlaced

            val nextIndex = state.currentItemIndex + 1
            val isRoundFinished = nextIndex >= state.totalItemsInRound
            val isFinalRound = state.currentRound >= totalRounds
            val isGameFinished = isRoundFinished && isFinalRound
            val updatedStars = if (isRoundFinished) maxOf(state.starsEarned, state.currentRound) else state.starsEarned

            _starsEarned.value = updatedStars
            _isCompleted.value = isGameFinished
            _celebrationActive.value = isGameFinished

            _uiState.update {
                it.copy(
                    placedItemsByRoom = updatedMap,
                    currentItemIndex = nextIndex,
                    starsEarned = updatedStars,
                    isRoundCompleted = isRoundFinished,
                    isActivityCompleted = isGameFinished,
                    celebrationActive = isGameFinished,
                    showRewardDialog = isGameFinished,
                    score = it.score + 20
                )
            }

            if (isGameFinished) {
                saveWinProgress()
            }

            return DropResult.Correct(
                item = item,
                room = roomChosen,
                isRoundFinished = isRoundFinished,
                isGameFinished = isGameFinished
            )
        } else {
            _uiState.update { it.copy(attempts = it.attempts + 1) }
            return DropResult.Incorrect(
                item = item,
                attemptedRoom = roomChosen,
                expectedRoom = item.targetRoom
            )
        }
    }

    fun nextRound(): Boolean {
        val state = _uiState.value
        if (state.currentRound >= totalRounds) return false
        val nextRoundNum = state.currentRound + 1
        val roundData = rounds.getOrNull(nextRoundNum - 1) ?: return false

        _currentRound.value = nextRoundNum
        _isCompleted.value = false
        _celebrationActive.value = false

        _uiState.update {
            it.copy(
                currentRound = nextRoundNum,
                currentRoundData = roundData,
                currentItemIndex = 0,
                placedItemsByRoom = HouseRoomId.entries.associateWith { emptyList() },
                isRoundCompleted = false,
                isActivityCompleted = false,
                celebrationActive = false,
                showRewardDialog = false
            )
        }
        return true
    }

    private fun saveWinProgress() {
        val scope = customScope ?: try {
            viewModelScope
        } catch (_: Throwable) {
            null
        } ?: return

        try {
            scope.launch(Dispatchers.IO) {
                try {
                    val repo = progressRepo ?: FirebaseProgressRepository()
                    val cur = repo.getProgress(_uiState.value.activeUserId) ?: UserProgress(userId = _uiState.value.activeUserId)
                    repo.saveProgress(
                        cur.copy(
                            objectGamesCompleted = cur.objectGamesCompleted + 1,
                            totalScore = cur.totalScore + 60,
                            totalStars = cur.totalStars + 3,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } catch (_: Throwable) {
                    // Modo offline
                }
            }
        } catch (_: Throwable) {
            // Manejo seguro en entorno JVM sin despachador Main
        }
    }

    fun dismissRewardDialog() {
        _celebrationActive.value = false
        _uiState.update { it.copy(showRewardDialog = false, celebrationActive = false) }
    }

    fun dismissCelebration() {
        dismissRewardDialog()
    }

    fun resetGame(randomize: Boolean = false) {
        val userId = _uiState.value.activeUserId
        rounds = MockDataRepository.getHouseSortingRounds(randomize = randomize)
        _currentRound.value = 1
        _starsEarned.value = 0
        _isCompleted.value = false
        _celebrationActive.value = false
        _uiState.value = createInitialState().copy(activeUserId = userId)
    }

    fun restartActivity(randomize: Boolean = false) {
        resetGame(randomize = randomize)
    }
}
