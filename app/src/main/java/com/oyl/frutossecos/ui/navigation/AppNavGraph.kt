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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.OYL.frutossecos.ui.screens.login.LoginScreen
import com.OYL.frutossecos.ui.screens.main.MainContainerScreen
import com.OYL.frutossecos.ui.screens.mapa.MapaScreen
import com.OYL.frutossecos.ui.screens.registro.RegistroScreen
import com.OYL.frutossecos.ui.screens.splash.SplashScreen

/**
 * Grafo de navegación principal de la aplicación OYL Frutos Secos.
 * Conecta Splash, Login, Registro y el Contenedor Principal (Dashboard + Mapa con barra inferior).
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
                    navController.navigate(NavRoutes.Registro.route)
                },
                onYaTengoCuenta = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onVerCatalogo = {
                    navController.navigate(NavRoutes.Dashboard.route)
                },
                onVerMapa = {
                    navController.navigate(NavRoutes.Mapa.route)
                }
            )
        }

        // ── Pantalla 2: Login ──────────────────────────────────────────────
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginExitoso = { _ ->
                    // Redirige al Dashboard / Contenedor principal con la barra inferior
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
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

        // ── Pantalla 3: Registro de Cliente ────────────────────────────────
        composable(NavRoutes.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { _ ->
                    // Redirige al Dashboard / Contenedor principal con la barra inferior
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Registro.route) { inclusive = true }
                    }
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // ── Pantalla 4: Contenedor Principal (Dashboard + Menú Inferior) ───
        composable(NavRoutes.Dashboard.route) {
            MainContainerScreen(
                onCerrarSesion = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Pantalla 5: Mapa de Tiendas y Puntos de Retiro ─────────────────
        composable(NavRoutes.Mapa.route) {
            MapaScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Rutas secundarias de respaldo ──────────────────────────────────
        composable(NavRoutes.Catalogo.route) {
            PantallaEnConstruccion(titulo = "Catálogo de Productos", onVolver = { navController.popBackStack() })
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
