
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
import com.OYL.frutossecos.ui.screens.carrito.CarritoScreen
import com.OYL.frutossecos.ui.screens.catalogo.CatalogoScreen
import com.OYL.frutossecos.ui.screens.confirmacion.ConfirmacionScreen
import com.OYL.frutossecos.ui.screens.detalle.DetalleScreen
import com.OYL.frutossecos.ui.screens.escanerqr.EscanerQrScreen
import com.OYL.frutossecos.ui.screens.login.LoginScreen
import com.OYL.frutossecos.ui.screens.main.MainContainerScreen
import com.OYL.frutossecos.ui.screens.mapa.MapaScreen
import com.OYL.frutossecos.ui.screens.registro.RegistroScreen
import com.OYL.frutossecos.ui.screens.splash.SplashScreen
import com.OYL.frutossecos.ui.screens.gestionproductos.GestionProductosScreen
import com.OYL.frutossecos.ui.screens.gestioninventario.GestionInventarioScreen
import com.OYL.frutossecos.ui.screens.pedidos.PedidosScreen
import com.OYL.frutossecos.ui.screens.notificaciones.NotificacionesScreen
import com.OYL.frutossecos.ui.screens.perfil.PerfilScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onComenzar = {
                    navController.navigate(NavRoutes.Registro.route)
                },
                onYaTengoCuenta = {
                    navController.navigate(NavRoutes.Login.route)
                },
                onVerCatalogo = {
                    navController.navigate(NavRoutes.Dashboard.route)
                },
                onVerMapa = {
                    navController.navigate(NavRoutes.Mapa.route)
                }
            )
        }

        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginExitoso = { _ ->
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onRegistrarse = {
                    navController.navigate(NavRoutes.Registro.route)
                },
                onVolver = {
                    if (!navController.popBackStack()) {
                        navController.navigate(NavRoutes.Splash.route)
                    }
                }
            )
        }

        composable(NavRoutes.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { _ ->
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Registro.route) { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.navigate(NavRoutes.Login.route)
                },
                onVolver = {
                    if (!navController.popBackStack()) {
                        navController.navigate(NavRoutes.Splash.route)
                    }
                }
            )
        }

        composable(NavRoutes.Dashboard.route) {
            MainContainerScreen(
                onCerrarSesion = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                },
                onIrACatalogo = {
                    navController.navigate(NavRoutes.Catalogo.route)
                },
                onIrACarrito = {
                    navController.navigate(NavRoutes.Carrito.route)
                },
                onIrAEscaner = {
                    navController.navigate(NavRoutes.EscanearQR.route)
                },
                onIrAGestionProductos = {
                    navController.navigate(NavRoutes.GestionProductos.route)
                },
                onIrAGestionInventario = {
                    navController.navigate(NavRoutes.AlertasStock.route)
                },
                onIrAPedidos = {
                    navController.navigate(NavRoutes.MisPedidos.route)
                },
                onIrANotificaciones = {
                    navController.navigate(NavRoutes.Notificaciones.route)
                },
                onIrAPerfil = {
                    navController.navigate(NavRoutes.Perfil.route)
                }
            )
        }

        composable(NavRoutes.Mapa.route) {
            MapaScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Catalogo.route) {
            CatalogoScreen(
                onVolver = { navController.popBackStack() },
                onVerDetalle = { productoId ->
                    navController.navigate(
                        NavRoutes.DetalleProducto.createRoute(productoId)
                    )
                },
                onEditarProducto = { productoId ->
                    navController.navigate(
                        NavRoutes.EditarProducto.createRoute(productoId)
                    )
                },
                onIrAlCarrito = {
                    navController.navigate(NavRoutes.Carrito.route)
                }
            )
        }

        composable(
            route = NavRoutes.DetalleProducto.route,
            arguments = listOf(
                navArgument("productoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0

            DetalleScreen(
                productoId = productoId,
                onVolver = { navController.popBackStack() },
                onEditar = { id ->
                    navController.navigate(
                        NavRoutes.EditarProducto.createRoute(id)
                    )
                },
                onIrAlCarrito = {
                    navController.navigate(NavRoutes.Carrito.route)
                }
            )
        }

        composable(NavRoutes.EscanearQR.route) {
            EscanerQrScreen(
                onVolver = { navController.popBackStack() },
                onVerProducto = { productoId ->
                    navController.navigate(
                        NavRoutes.DetalleProducto.createRoute(productoId)
                    )
                }
            )
        }

        composable(NavRoutes.Carrito.route) {
            CarritoScreen(
                onVolver = { navController.popBackStack() },
                onContinuarCompra = {
                    navController.navigate(NavRoutes.ConfirmarPedido.route)
                },
                onVerCatalogo = {
                    navController.navigate(NavRoutes.Catalogo.route)
                }
            )
        }

        composable(NavRoutes.ConfirmarPedido.route) {
            ConfirmacionScreen(
                onVolver = { navController.popBackStack() },
                onVerMisPedidos = {
                    navController.navigate(NavRoutes.MisPedidos.route) {
                        popUpTo(NavRoutes.Carrito.route) { inclusive = true }
                    }
                },
                onSeguirComprando = {
                    navController.navigate(NavRoutes.Catalogo.route) {
                        popUpTo(NavRoutes.Carrito.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.MisPedidos.route) {
            PedidosScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.GestionProductos.route) {
            GestionProductosScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EditarProducto.route,
            arguments = listOf(
                navArgument("productoId") { type = NavType.IntType }
            )
        ) {
            PantallaEnConstruccion(
                titulo = "Editar Producto",
                onVolver = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.AdminPanel.route) {
            PantallaEnConstruccion(
                titulo = "Panel de Administración",
                onVolver = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.AlertasStock.route) {
            GestionInventarioScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Notificaciones.route) {
            NotificacionesScreen(
                onAbrirDestino = { destino ->
                    when (destino) {
                        "mis_pedidos" -> {
                            navController.navigate(NavRoutes.MisPedidos.route)
                        }
                        "gestion_inventario" -> {
                            navController.navigate(NavRoutes.AlertasStock.route)
                        }
                        else -> Unit
                    }
                }
            )
        }

        composable(NavRoutes.Perfil.route) {
            PerfilScreen(
                onVolver = {
                    navController.popBackStack()
                },
                onCerrarSesion = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun PantallaEnConstruccion(
    titulo: String,
    onVolver: () -> Unit
) {
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
