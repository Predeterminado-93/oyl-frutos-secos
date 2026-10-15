package com.OYL.frutossecos.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.OYL.frutossecos.ui.screens.splash.SplashScreen
import com.OYL.frutossecos.ui.screens.login.LoginScreen

/**
 * Grafo de navegación principal de la aplicación.
 * Cada pantalla se agrega aquí a medida que se implementa.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        // ── Pantalla 1: Splash / Landing ───────────────────────────────────
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onComenzar = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onYaTengoCuenta = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onVerCatalogo = {
                    navController.navigate(NavRoutes.Catalogo.route)
                },
                onVerMapa = {
                    navController.navigate(NavRoutes.Mapa.route)
                }
            )
        }

        // ── Pantalla 2: Login ──────────────────────────────────────────────
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginExitoso = { rol ->
                    if (rol == "ADMIN") {
                        navController.navigate(NavRoutes.AdminPanel.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.Catalogo.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                },
                onRegistrarse = {
                    navController.navigate(NavRoutes.Registro.route)
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // ── Pantallas en desarrollo (placeholders para prevenir crash de navegación) ──
        composable(NavRoutes.Catalogo.route) {
            PantallaEnConstruccion(titulo = "Catálogo de Productos", onVolver = { navController.popBackStack() })
        }
        composable(NavRoutes.Mapa.route) {
            PantallaEnConstruccion(titulo = "Ubicación y Mapa", onVolver = { navController.popBackStack() })
        }
        composable(NavRoutes.Registro.route) {
            PantallaEnConstruccion(titulo = "Registro de Usuario", onVolver = { navController.popBackStack() })
        }
        composable(NavRoutes.AdminPanel.route) {
            PantallaEnConstruccion(titulo = "Panel de Administración", onVolver = { navController.popBackStack() })
        }
    }
}

@Composable
private fun PantallaEnConstruccion(titulo: String, onVolver: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Esta pantalla se encuentra en desarrollo.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onVolver) {
                Text("Volver")
            }
        }
    }
}
