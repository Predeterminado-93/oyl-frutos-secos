package com.OYL.frutossecos.ui.navigation

/**
 * Define todas las rutas de navegación de la aplicación.
 * Se usa sealed class para type-safety y evitar strings sueltos.
 */
sealed class NavRoutes(val route: String) {
    // ── Pantallas principales ──────────────────────────────────────────────
    data object Splash       : NavRoutes("splash")
    data object Login        : NavRoutes("login")
    data object Registro     : NavRoutes("registro")
    data object Dashboard    : NavRoutes("dashboard")

    // ── Cliente ────────────────────────────────────────────────────────────
    data object Catalogo     : NavRoutes("catalogo")
    data object DetalleProducto : NavRoutes("detalle/{productoId}") {
        fun createRoute(productoId: Int) = "detalle/$productoId"
    }
    data object Carrito      : NavRoutes("carrito")
    data object ConfirmarPedido : NavRoutes("confirmar_pedido")
    data object MisPedidos   : NavRoutes("mis_pedidos")
    data object Mapa         : NavRoutes("mapa")
    data object EscanearQR   : NavRoutes("escanear_qr")
    data object Perfil       : NavRoutes("perfil")

    // ── Admin ──────────────────────────────────────────────────────────────
    data object AdminPanel   : NavRoutes("admin_panel")
    data object GestionProductos : NavRoutes("admin_productos")
    data object EditarProducto : NavRoutes("admin_editar/{productoId}") {
        fun createRoute(productoId: Int) = "admin_editar/$productoId"
    }
    data object GestionPedidos : NavRoutes("admin_pedidos")
    data object AlertasStock : NavRoutes("admin_alertas")
}
