package com.example.no_definido.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.data.MockDataRepository
import com.example.no_definido.ui.screens.ActivityEngineScreen
import com.example.no_definido.ui.screens.AppleBasketGameScreen
import com.example.no_definido.ui.screens.AreaSelectionScreen
import com.example.no_definido.ui.screens.FirstPersonPanoramicRoomScreen
import com.example.no_definido.ui.screens.HomeScreen
import com.example.no_definido.ui.screens.InteractiveLivingRoomScreen
import com.example.no_definido.ui.viewmodel.ActivityViewModel
import com.example.no_definido.ui.viewmodel.AppleBasketViewModel

enum class ScreenState {
    HOME,
    LIVING_ROOM,
    FIRST_PERSON_ROOM,
    APPLE_GAME,
    AREA_SELECTION,
    ACTIVITY_GAME
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    activityViewModel: ActivityViewModel = viewModel(),
    appleBasketViewModel: AppleBasketViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(ScreenState.HOME) }

    when (currentScreen) {
        ScreenState.HOME -> {
            HomeScreen(
                onOpenLivingRoom = {
                    currentScreen = ScreenState.FIRST_PERSON_ROOM
                },
                onStartActivity = {
                    activityViewModel.restartActivity()
                    currentScreen = ScreenState.ACTIVITY_GAME
                },
                onExploreAreas = {
                    currentScreen = ScreenState.AREA_SELECTION
                },
                modifier = modifier
            )
        }

        ScreenState.FIRST_PERSON_ROOM -> {
            FirstPersonPanoramicRoomScreen(
                onOpenAppleGame = {
                    appleBasketViewModel.resetGame()
                    currentScreen = ScreenState.APPLE_GAME
                },
                onOpenObjectGame = {
                    activityViewModel.restartActivity()
                    currentScreen = ScreenState.ACTIVITY_GAME
                },
                onOpenAreaExplorer = {
                    currentScreen = ScreenState.AREA_SELECTION
                },
                onBack = {
                    currentScreen = ScreenState.HOME
                },
                modifier = modifier
            )
        }

        ScreenState.LIVING_ROOM -> {
            InteractiveLivingRoomScreen(
                onOpenAppleGame = {
                    appleBasketViewModel.resetGame()
                    currentScreen = ScreenState.APPLE_GAME
                },
                onOpenObjectGame = {
                    activityViewModel.restartActivity()
                    currentScreen = ScreenState.ACTIVITY_GAME
                },
                onOpenAreaExplorer = {
                    currentScreen = ScreenState.AREA_SELECTION
                },
                onBack = {
                    currentScreen = ScreenState.HOME
                },
                modifier = modifier
            )
        }

        ScreenState.APPLE_GAME -> {
            AppleBasketGameScreen(
                viewModel = appleBasketViewModel,
                onBack = {
                    currentScreen = ScreenState.FIRST_PERSON_ROOM
                },
                modifier = modifier
            )
        }

        ScreenState.AREA_SELECTION -> {
            AreaSelectionScreen(
                areas = MockDataRepository.sampleAreas,
                onAreaSelected = {
                    activityViewModel.restartActivity()
                    currentScreen = ScreenState.ACTIVITY_GAME
                },
                onBack = {
                    currentScreen = ScreenState.HOME
                },
                modifier = modifier
            )
        }

        ScreenState.ACTIVITY_GAME -> {
            ActivityEngineScreen(
                viewModel = activityViewModel,
                onBack = {
                    currentScreen = ScreenState.HOME
                },
                modifier = modifier
            )
        }
    }
}
