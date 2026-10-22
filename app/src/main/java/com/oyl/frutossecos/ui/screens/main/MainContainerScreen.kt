package com.OYL.frutossecos.ui.screens.main

import androidx.activity.compose.BackHandler
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

@Composable
fun MainContainerScreen(
    onCerrarSesion: () -> Unit,
    onIrAPerfil: () -> Unit = {},
    onIrACatalogo: () -> Unit = {},
    onIrACarrito: () -> Unit = {},
    onIrAEscaner: () -> Unit = {},
    onIrAGestionProductos: () -> Unit = {},
    onIrAGestionInventario: () -> Unit = {},
    onIrAPedidos: () -> Unit = {},
    onIrANotificaciones: () -> Unit = {}
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) }

    // Si el usuario está en el Mapa u otra pestaña y presiona Atrás en el teléfono, vuelve a Inicio
    BackHandler(enabled = pestañaSeleccionada != 0) {
        pestañaSeleccionada = 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
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

                NavigationBarItem(
                    selected = false,
                    onClick = onIrAEscaner,
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

                NavigationBarItem(
                    selected = false,
                    onClick = onIrAPedidos,
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

                NavigationBarItem(
                    selected = false,
                    onClick = onIrANotificaciones,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    },
                    label = { Text("Avisos") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = BrownMedium.copy(alpha = 0.6f),
                        unselectedTextColor = BrownMedium.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onIrAPerfil,
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
                        onCerrarSesion = onCerrarSesion,
                        onIrACatalogo = onIrACatalogo,
                        onIrACarrito = onIrACarrito,
                        onIrAGestionProductos = onIrAGestionProductos,
                        onIrAGestionInventario = onIrAGestionInventario
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
