package com.example.no_definido.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.no_definido.R
import com.example.no_definido.ui.audio.rememberSoundHelper
import com.example.no_definido.ui.components.ButterflyColorsInRoomObject
import com.example.no_definido.ui.components.CalmecacAxolotlMascot
import com.example.no_definido.ui.components.TeddyAndHomeItemsInRoomObject
import com.example.no_definido.ui.components.WoodenTrainInRoomObject
import com.example.no_definido.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val soundHelper = rememberSoundHelper()

    var showTutorPanel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        soundHelper.speak("¡Bienvenido a Cal-mé-cac! Toca el botón grande para empezar a jugar.")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "playButtonPulse")
    val playScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playScale"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Fondo ilustrado del prado Mexica con pirámide y flores de cempasúchil
        Image(
            painter = painterResource(id = R.drawable.bg_train_meadow),
            contentDescription = "Paisaje Cal-mé-cac",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Capa suave para resaltar los elementos centrales
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // Botón discreto en la esquina superior derecha para Padres / Tutores (Login con Firebase)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xCC0F172A),
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .clickable {
                    soundHelper.playPop()
                    showTutorPanel = !showTutorPanel
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil Tutor",
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showTutorPanel) "Modo Niño" else "Tutor / Cuenta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF59D)
                )
            }
        }

        if (!showTutorPanel) {
            // =========================================================================
            // MODO NIÑO (POR DEFECTO): 100% VISUAL, SIN FORMULarios COMPLEJOS
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Emblema de Cal-mé-cac
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xEE3E2723),
                    border = BorderStroke(4.dp, Color(0xFFFFB300)),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🌞",
                            fontSize = 28.sp
                        )
                        Text(
                            text = "CAL-MÉ-CAC",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "🦅",
                            fontSize = 28.sp
                        )
                    }
                }

                // Centro: Mascota Ajolote Mexica + Botón Gigante de Jugar + Amigos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Amigo Izquierdo: Mascota Ajolote Mexica Ilustrada
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .graphicsLayer { translationY = floatY }
                            .clickable {
                                soundHelper.playPop()
                                soundHelper.speak("¡Hola! Soy el ajolote guardián del Calmécac. ¡Vamos a jugar!")
                                loginViewModel.playAsGuest { onLoginSuccess("ajolote_amigo") }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CalmecacAxolotlMascot(modifier = Modifier.fillMaxSize())
                    }

                    // BOTÓN CENTRAL GIGANTE DE PLAY (UN SOLO TOQUE PARA ENTRAR A LA SALA)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(165.dp)
                            .graphicsLayer {
                                scaleX = playScale
                                scaleY = playScale
                            }
                            .clickable {
                                soundHelper.playSuccess()
                                loginViewModel.playAsGuest(onLoginSuccess)
                            }
                    ) {
                        // Resplandor solar exterior
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD54F).copy(alpha = 0.75f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF43A047),
                            border = BorderStroke(6.dp, Color(0xFFFFF59D)),
                            shadowElevation = 18.dp,
                            modifier = Modifier.size(132.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Jugar",
                                    tint = Color.White,
                                    modifier = Modifier.size(84.dp)
                                )
                            }
                        }
                    }

                    // Amigo Derecho: Osito y Mariposa
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer { translationY = -floatY }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clickable {
                                    soundHelper.playPop()
                                    loginViewModel.playAsGuest(onLoginSuccess)
                                }
                        ) {
                            ButterflyColorsInRoomObject()
                        }
                        Box(
                            modifier = Modifier
                                .size(105.dp)
                                .clickable {
                                    soundHelper.playPop()
                                    loginViewModel.playAsGuest(onLoginSuccess)
                                }
                        ) {
                            TeddyAndHomeItemsInRoomObject()
                        }
                    }
                }

                // Tren decorativo inferior
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 74.dp)
                        .clickable {
                            soundHelper.playPop()
                            loginViewModel.playAsGuest(onLoginSuccess)
                        }
                ) {
                    WoodenTrainInRoomObject()
                }
            }
        } else {
            // =========================================================================
            // PANEL DE TUTOR / CUENTA FIREBASE (CUANDO EL ADULTO TOCA "TUTOR / CUENTA")
            // =========================================================================
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.96f),
                    border = BorderStroke(4.dp, Color(0xFFFFB300)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .width(420.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (!uiState.isRegisterMode) "Cuenta Cal-mé-cac" else "Registro de Jugador",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3E2723)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { loginViewModel.onUsernameChange(it) },
                            label = { Text("Nombre o Apodo del Niño", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFFFDE68A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.isRegisterMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { loginViewModel.onEmailChange(it) },
                                label = { Text("Correo del Tutor (Opcional)", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.pinOrPassword,
                            onValueChange = { loginViewModel.onPasswordChange(it) },
                            label = { Text("PIN o Contraseña", fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                loginViewModel.submit(onLoginSuccess)
                            }),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AnimatedVisibility(visible = uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(34.dp),
                                color = Color(0xFFF59E0B)
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF43A047),
                                border = BorderStroke(2.5.dp, Color(0xFF3E2723)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        focusManager.clearFocus()
                                        loginViewModel.submit(onLoginSuccess)
                                    }
                            ) {
                                Text(
                                    text = if (!uiState.isRegisterMode) "¡Entrar a Jugar!" else "¡Crear Cuenta!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }

                        TextButton(onClick = { loginViewModel.toggleMode() }) {
                            Text(
                                text = if (!uiState.isRegisterMode) "¿Nuevo jugador? Crear cuenta aquí"
                                else "¿Ya tienes cuenta? Iniciar sesión",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }
        }
    }
}
