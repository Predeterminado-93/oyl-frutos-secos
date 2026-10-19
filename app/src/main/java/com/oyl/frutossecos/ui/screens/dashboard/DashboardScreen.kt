package com.OYL.frutossecos.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.OYL.frutossecos.R
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.ui.theme.*

/**
 * Pantalla 4 — Pantalla Principal / Dashboard
 *
 * Roles: Administrador, Cliente.
 * Ofrece una vista resumida según el rol autenticado:
 *   - Cliente: Productos destacados, promociones de temporada, estado de pedidos y mapa de retiro.
 *   - Admin: Métricas en vivo, pedidos pendientes de atención, alertas de stock bajo y accesos al catálogo.
 */
@Composable
fun DashboardScreen(
    onNavegarAMapa: () -> Unit,
    onIrACatalogo: () -> Unit = {},
    onIrACarrito: () -> Unit = {},
    onCerrarSesion: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = {
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas salir de tu cuenta (${uiState.usuario?.alias})?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrownMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = BrownDark
                    )
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar", color = BrownMedium)
                }
            },
            containerColor = CreamWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Cabecera de Bienvenida con Rol ─────────────────────────────────
        item {
            DashboardHeader(
                nombre = uiState.usuario?.nombre ?: "Usuario",
                alias = uiState.usuario?.alias ?: "",
                esAdmin = uiState.esAdmin,
                onCerrarSesionClick = { mostrarDialogoCerrarSesion = true }
            )
        }

        // ── Banner con Imagen Generada por IA ──────────────────────────────
        item {
            HeroBannerImage(onVerOfertas = onNavegarAMapa)
        }
        // ── Accesos rápidos: Catálogo y Carrito ────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onIrACatalogo,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = BrownDark
                    )
                ) {
                    Text("🛍️ Catálogo", fontWeight = FontWeight.Bold)
                }
                if (!uiState.esAdmin) {
                    OutlinedButton(
                        onClick = onIrACarrito,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AmberGold)
                    ) {
                        Text("🛒 Mi carrito", color = BrownDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }


        // ── VISTA SEGÚN EL ROL ─────────────────────────────────────────────
        if (uiState.esAdmin) {
            // PANEL DE ADMINISTRACIÓN
            item {
                AdminDashboardContent(
                    pedidosPendientes = uiState.pedidosPendientesAdmin,
                    productosStockBajo = uiState.productosStockBajo,
                    onVerMapa = onNavegarAMapa
                )
            }
        } else {
            // PANEL DE CLIENTE
            item {
                ClienteDashboardContent(
                    pedidosRecientes = uiState.pedidosRecientes,
                    productosDestacados = uiState.productosDestacados,
                    onVerMapa = onNavegarAMapa
                )
            }
        }
    }
}

/**
 * Cabecera superior con saludo, badge de rol y botón de cerrar sesión.
 */
@Composable
private fun DashboardHeader(
    nombre: String,
    alias: String,
    esAdmin: Boolean,
    onCerrarSesionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "¡Hola, $nombre!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrownDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (esAdmin) AmberGold else WarmTan.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (esAdmin) "🛡️ ADMINISTRADOR" else "👤 CLIENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                if (alias.isNotBlank()) {
                    Text(
                        text = "@$alias",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AmberGold,
                modifier = Modifier.size(42.dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (esAdmin) "👑" else "🌰",
                        fontSize = 18.sp
                    )
                }
            }

            IconButton(onClick = onCerrarSesionClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    tint = BrownMedium
                )
            }
        }
    }
}

/**
 * Hero Banner principal con la imagen generada por IA y estética OYL.
 */
@Composable
private fun HeroBannerImage(onVerOfertas: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen generada por IA
            Image(
                painter = painterResource(id = R.drawable.banner_frutos_secos),
                contentDescription = "Frutos secos naturales OYL",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente oscuro suave para legibilidad del texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BrownDark.copy(alpha = 0.75f),
                                BrownDark.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Contenido sobre la imagen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberGold
                ) {
                    Text(
                        text = "⭐ COSECHA FRESCA Y NATURAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Frutos Secos, Semillas y Deshidratados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CreamWhite
                )

                Text(
                    text = "Retira tus pedidos sin costo de despacho en nuestros puntos habilitados",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmTan,
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Contenido del Dashboard para Rol CLIENTE.
 */
@Composable
private fun ClienteDashboardContent(
    pedidosRecientes: List<PedidoEntity>,
    productosDestacados: List<ProductoEntity>,
    onVerMapa: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Tarjeta de Acceso a Puntos de Retiro ───────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(onClick = onVerMapa),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GoldenLight.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, AmberGold)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BrownDark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tiendas y Puntos de Retiro OYL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = "Conoce las ubicaciones en Santiago, horarios y direcciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AmberGold
                )
            }
        }

        // ── Estado de Pedido Reciente (si tiene) ────────────────────────────
        val ultimoPedido = pedidosRecientes.firstOrNull()
        if (ultimoPedido != null) {
            Text(
                text = "Tu Pedido en Curso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrownDark,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AmberGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "📦", fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pedido #${ultimoPedido.id}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrownDark
                        )
                        Text(
                            text = ultimoPedido.itemsResumen,
                            style = MaterialTheme.typography.bodySmall,
                            color = BrownMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📍 ${ultimoPedido.puntoRetiroNombre}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrownMedium.copy(alpha = 0.7f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (ultimoPedido.estado) {
                            "EN_PREPARACION" -> AmberGold
                            "LISTO_RETIRO" -> OliveGreen
                            else -> WarmTan
                        }
                    ) {
                        Text(
                            text = ultimoPedido.estado.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (ultimoPedido.estado == "LISTO_RETIRO") CreamWhite else BrownDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ── Productos Destacados ───────────────────────────────────────────
        Text(
            text = "Productos Destacados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(productosDestacados, key = { it.id }) { producto ->
                ProductoDestacadoCard(producto = producto)
            }
        }

        // ── Promociones de Temporada ───────────────────────────────────────
        Text(
            text = "Promociones OYL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GoldenLight.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, AmberGold)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎁", fontSize = 32.sp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "15% Descuento en Mix de Semillas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = "Válido retirando en Tienda Matriz Providencia o Metro Los Leones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium
                    )
                }
            }
        }
    }
}

/**
 * Contenido del Dashboard para Rol ADMINISTRADOR.
 */
@Composable
private fun AdminDashboardContent(
    pedidosPendientes: List<PedidoEntity>,
    productosStockBajo: List<ProductoEntity>,
    onVerMapa: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Resumen Operativo en Tiempo Real",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
        )

        // ── Tarjetas Métricas KPI ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                titulo = "Pedidos Pendientes",
                valor = "${pedidosPendientes.size}",
                alerta = pedidosPendientes.isNotEmpty(),
                icono = Icons.Default.HourglassTop,
                colorAlerta = AmberGold,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                titulo = "Stock Crítico",
                valor = "${productosStockBajo.size}",
                alerta = productosStockBajo.isNotEmpty(),
                icono = Icons.Default.Warning,
                colorAlerta = ErrorRed,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                titulo = "Puntos Retiro",
                valor = "4",
                alerta = false,
                icono = Icons.Default.Store,
                colorAlerta = OliveGreen,
                modifier = Modifier.weight(1f),
                onClick = onVerMapa
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Alertas de Stock Bajo ──────────────────────────────────────────
        Text(
            text = "⚠️ Alertas de Stock Bajo (≤ 5 unidades)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        if (productosStockBajo.isEmpty()) {
            Text(
                text = "Todo el inventario cuenta con stock suficiente.",
                style = MaterialTheme.typography.bodyMedium,
                color = OliveGreen,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            productosStockBajo.forEach { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ErrorRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "⚠️", fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrownDark
                            )
                            Text(
                                text = "Categoría: ${producto.categoria}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrownMedium.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Solo ${producto.stock} un.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Pedidos Pendientes de Atención ─────────────────────────────────
        Text(
            text = "📋 Pedidos por Despachar / Retirar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        pedidosPendientes.forEach { pedido ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pedido #${pedido.id} • Cliente: @${pedido.usuarioAlias}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrownDark
                        )
                        Text(
                            text = pedido.itemsResumen,
                            style = MaterialTheme.typography.bodySmall,
                            color = BrownMedium
                        )
                        Text(
                            text = "Retiro en: ${pedido.puntoRetiroNombre}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OliveGreen
                        )
                    }
                    Text(
                        text = "$${pedido.total.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    titulo: String,
    valor: String,
    alerta: Boolean,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorAlerta: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (alerta) colorAlerta else WarmTan.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (alerta) colorAlerta else BrownMedium,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (alerta) colorAlerta else BrownDark
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = BrownMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProductoDestacadoCard(producto: ProductoEntity) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.4f))
    ) {
        Column {
            AsyncImage(
                model = producto.fotoUrl,
                contentDescription = producto.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(GoldenLight.copy(alpha = 0.2f))
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = producto.categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrownMedium.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${producto.precio.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberGold
                    )
                    Text(
                        text = "Stock: ${producto.stock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (producto.stock <= 5) ErrorRed else OliveGreen
                    )
                }
            }
        }
    }
}
