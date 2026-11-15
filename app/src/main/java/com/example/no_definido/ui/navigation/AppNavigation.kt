package com.example.no_definido.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.screens.ActivityEngineScreen
import com.example.no_definido.ui.screens.AppleBasketGameScreen
import com.example.no_definido.ui.screens.BubblePopGameScreen
import com.example.no_definido.ui.screens.ColorFlowerGardenScreen
import com.example.no_definido.ui.screens.FirstPersonPanoramicRoomScreen
import com.example.no_definido.ui.screens.InteractiveClockGameScreen
import com.example.no_definido.ui.screens.KitchenSortingGameScreen
import com.example.no_definido.ui.screens.LoginScreen
import com.example.no_definido.ui.screens.MemoryMatchGameScreen
import com.example.no_definido.ui.screens.NumberTrainGameScreen
import com.example.no_definido.ui.viewmodel.ActivityViewModel
import com.example.no_definido.ui.viewmodel.AppleBasketViewModel
import com.example.no_definido.ui.viewmodel.BubblePopViewModel
import com.example.no_definido.ui.viewmodel.ColorFlowerGardenViewModel
import com.example.no_definido.ui.viewmodel.HomeViewModel
import com.example.no_definido.ui.viewmodel.InteractiveClockViewModel
import com.example.no_definido.ui.viewmodel.KitchenSortingViewModel
import com.example.no_definido.ui.viewmodel.MemoryMatchViewModel
import com.example.no_definido.ui.viewmodel.NumberTrainViewModel

enum class ScreenState {
    LOGIN,
    FIRST_PERSON_ROOM, // Sala Principal 100% Interactiva y Deslizable (Estilo FNAF 3)
    APPLE_GAME,        // 1. Cesta de Manzanas (Contar en el huerto)
    ACTIVITY_GAME,     // 2. Mi Casa en Orden (4 habitaciones amuebladas)
    CLOCK_GAME,        // 3. El Reloj del Sol y la Luna (Aprender las horas)
    KITCHEN_GAME,      // 4. Cocina Ordenada (Refrigerador vs. Estantería)
    TRAIN_GAME,        // 5. El Tren de los Números (Contar cantidades)
    GARDEN_GAME,       // 6. Jardín de Colores y Formas
    MEMORY_GAME,       // 7. Memorama del Calmécac (Libros Mágicos)
    BUBBLE_GAME        // 8. Burbujas Mágicas (Pelota de Playa y Burbujas)
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    activityViewModel: ActivityViewModel = viewModel(),
    appleBasketViewModel: AppleBasketViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    interactiveClockViewModel: InteractiveClockViewModel = viewModel(),
    kitchenSortingViewModel: KitchenSortingViewModel = viewModel(),
    numberTrainViewModel: NumberTrainViewModel = viewModel(),
    colorFlowerGardenViewModel: ColorFlowerGardenViewModel = viewModel(),
    memoryMatchViewModel: MemoryMatchViewModel = viewModel(),
    bubblePopViewModel: BubblePopViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(ScreenState.LOGIN) }
    var currentUserId by remember { mutableStateOf("jugador_invitado") }
    val soundHelper = rememberSoundHelper()

    fun navigateBackToRoom() {
        soundHelper.stop()
        homeViewModel.fetchProgress(currentUserId)
        currentScreen = ScreenState.FIRST_PERSON_ROOM
    }

    // Manejo nativo del botón/gesto Atrás de Android para volver siempre a la sala sin cerrar la app
    BackHandler(enabled = currentScreen != ScreenState.LOGIN && currentScreen != ScreenState.FIRST_PERSON_ROOM) {
        navigateBackToRoom()
    }

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 350),
        label = "screenCrossfade"
    ) { screen ->
        when (screen) {
            ScreenState.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = { userId ->
                        currentUserId = userId
                        homeViewModel.setUserId(userId)
                        appleBasketViewModel.setActiveUserId(userId)
                        activityViewModel.setActiveUserId(userId)
                        interactiveClockViewModel.setActiveUserId(userId)
                        kitchenSortingViewModel.setActiveUserId(userId)
                        numberTrainViewModel.setActiveUserId(userId)
                        colorFlowerGardenViewModel.setActiveUserId(userId)
                        memoryMatchViewModel.setActiveUserId(userId)
                        bubblePopViewModel.setActiveUserId(userId)
                        currentScreen = ScreenState.FIRST_PERSON_ROOM
                    },
                    modifier = modifier
                )
            }

            ScreenState.FIRST_PERSON_ROOM -> {
                FirstPersonPanoramicRoomScreen(
                    onOpenAppleGame = {
                        appleBasketViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.APPLE_GAME
                    },
                    onOpenObjectGame = {
                        activityViewModel.restartActivity(randomize = true)
                        currentScreen = ScreenState.ACTIVITY_GAME
                    },
                    onOpenClockGame = {
                        interactiveClockViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.CLOCK_GAME
                    },
                    onOpenKitchenGame = {
                        kitchenSortingViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.KITCHEN_GAME
                    },
                    onOpenTrainGame = {
                        numberTrainViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.TRAIN_GAME
                    },
                    onOpenGardenGame = {
                        colorFlowerGardenViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.GARDEN_GAME
                    },
                    onOpenMemoryGame = {
                        memoryMatchViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.MEMORY_GAME
                    },
                    onOpenBubbleGame = {
                        bubblePopViewModel.resetGame(randomize = true)
                        currentScreen = ScreenState.BUBBLE_GAME
                    },
                    onLogout = {
                        soundHelper.stop()
                        currentScreen = ScreenState.LOGIN
                    },
                    homeViewModel = homeViewModel,
                    modifier = modifier
                )
            }

            ScreenState.APPLE_GAME -> {
                AppleBasketGameScreen(
                    viewModel = appleBasketViewModel,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.ACTIVITY_GAME -> {
                ActivityEngineScreen(
                    viewModel = activityViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.CLOCK_GAME -> {
                InteractiveClockGameScreen(
                    viewModel = interactiveClockViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.KITCHEN_GAME -> {
                KitchenSortingGameScreen(
                    viewModel = kitchenSortingViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.TRAIN_GAME -> {
                NumberTrainGameScreen(
                    viewModel = numberTrainViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.GARDEN_GAME -> {
                ColorFlowerGardenScreen(
                    viewModel = colorFlowerGardenViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.MEMORY_GAME -> {
                MemoryMatchGameScreen(
                    viewModel = memoryMatchViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }

            ScreenState.BUBBLE_GAME -> {
                BubblePopGameScreen(
                    viewModel = bubblePopViewModel,
                    activeUserId = currentUserId,
                    onBack = { navigateBackToRoom() },
                    modifier = modifier
                )
            }
        }
    }
}
