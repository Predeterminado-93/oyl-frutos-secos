package com.OYL.frutossecos.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.OYL.frutossecos.ui.screens.dashboard.DashboardScreen
import com.OYL.frutossecos.ui.screens.mapa.MapaScreen
import com.OYL.frutossecos.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Contenedor principal con Barra de Navegación Inferior (Bottom Navigation Bar).
 *
 * Muestra el menú inferior solicitado:
 *   1. Inicio (Dashboard según rol con la imagen de frutos secos arriba)
 *   2. Mapa (Mapa de Tiendas y Puntos de Retiro)
 *   3. Escanear (en la barra, reservado para desarrollo)
 *   4. Pedidos (en la barra, reservado para desarrollo)
 *   5. Perfil (en la barra, reservado para desarrollo)
 */
@Composable
fun MainContainerScreen(
    onCerrarSesion: () -> Unit
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) } // 0: Inicio, 1: Mapa
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // 1. Inicio
                NavigationBarItem(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = {
                        Text(
                            text = "Inicio",
                            fontWeight = if (pestañaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrownDark,
                        selectedTextColor = BrownDark,
                        indicatorColor = AmberGold,
                        unselectedIconColor = BrownMedium.copy(alpha = 0.6f),
                        unselectedTextColor = BrownMedium.copy(alpha = 0.6f)
                    )
                )

                // 2. Mapa
                NavigationBarItem(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Mapa"
                        )
                    },
                    label = {
                        Text(
                            text = "Mapa",
                            fontWeight = if (pestañaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrownDark,
                        selectedTextColor = BrownDark,
                        indicatorColor = AmberGold,
                        unselectedIconColor = BrownMedium.copy(alpha = 0.6f),
                        unselectedTextColor = BrownMedium.copy(alpha = 0.6f)
                    )
                )

                // 3. Escanear
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Escáner QR: Pantalla en preparación para la siguiente fase.")
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear"
                        )
                    },
                    label = { Text("Escanear") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = BrownMedium.copy(alpha = 0.6f),
                        unselectedTextColor = BrownMedium.copy(alpha = 0.6f)
                    )
                )

                // 4. Pedidos
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Módulo de Pedidos: En desarrollo para la siguiente entrega.")
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Pedidos"
                        )
                    },
                    label = { Text("Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = BrownMedium.copy(alpha = 0.6f),
                        unselectedTextColor = BrownMedium.copy(alpha = 0.6f)
                    )
                )

                // 5. Perfil
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Módulo de Perfil: Usa el botón en la cabecera para cerrar sesión.")
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = BrownMedium.copy(alpha = 0.6f),
                        unselectedTextColor = BrownMedium.copy(alpha = 0.6f)
                    )
                )
            }
        },
        containerColor = CreamWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pestañaSeleccionada) {
                0 -> {
                    DashboardScreen(
                        onNavegarAMapa = { pestañaSeleccionada = 1 },
                        onCerrarSesion = onCerrarSesion
                    )
                }
                1 -> {
                    MapaScreen(
                        onVolver = { pestañaSeleccionada = 0 }
                    )
                }
            }
        }
    }
}
