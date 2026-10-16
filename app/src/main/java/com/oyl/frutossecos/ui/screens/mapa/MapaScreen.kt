package com.OYL.frutossecos.ui.screens.mapa

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.PuntoRetiroEntity
import com.OYL.frutossecos.ui.theme.AmberGold
import com.OYL.frutossecos.ui.theme.BrownDark
import com.OYL.frutossecos.ui.theme.BrownMedium
import com.OYL.frutossecos.ui.theme.CreamWhite
import com.OYL.frutossecos.ui.theme.ErrorRed
import com.OYL.frutossecos.ui.theme.GoldenLight
import com.OYL.frutossecos.ui.theme.OliveGreen
import com.OYL.frutossecos.ui.theme.WarmTan

/**
 * Pantalla 5 — Mapa de Tiendas y Puntos de Retiro
 *
 * Rol: Cliente (y Administrador).
 * Presenta la tienda matriz y los puntos de retiro habilitados guardados en SQLite (Room).
 * Permite consultar dirección, horarios de referencia, indicaciones y abrir la ruta en el mapa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    onVolver: (() -> Unit)? = null,
    viewModel: MapaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val puntosFiltrados = remember(uiState.puntos, uiState.filtroSeleccionado) {
        when (uiState.filtroSeleccionado) {
            "TIENDAS" -> uiState.puntos.filter { it.tipo == "TIENDA_FISICA" }
            "METRO" -> uiState.puntos.filter { it.tipo == "PUNTO_RETIRO" }
            else -> uiState.puntos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tiendas y Puntos de Retiro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrownDark
                        )
                        Text(
                            text = "Retira tus pedidos sin costo de envío",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrownMedium.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = BrownDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamWhite
                )
            )
        },
        containerColor = CreamWhite
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmberGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Filtros Rápidos ────────────────────────────────────────
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FiltroChip(
                                label = "Todos (${uiState.puntos.size})",
                                seleccionado = uiState.filtroSeleccionado == "TODOS",
                                onClick = { viewModel.cambiarFiltro("TODOS") }
                            )
                        }
                        item {
                            FiltroChip(
                                label = "🏪 Tienda Matriz",
                                seleccionado = uiState.filtroSeleccionado == "TIENDAS",
                                onClick = { viewModel.cambiarFiltro("TIENDAS") }
                            )
                        }
                        item {
                            FiltroChip(
                                label = "🚇 Puntos Metro",
                                seleccionado = uiState.filtroSeleccionado == "METRO",
                                onClick = { viewModel.cambiarFiltro("METRO") }
                            )
                        }
                    }
                }

                // ── Mapa Visual Interactivo (Estilizado) ───────────────────
                item {
                    VisualMapCanvas(
                        puntos = puntosFiltrados,
                        puntoSeleccionado = uiState.puntoSeleccionado,
                        onPuntoClick = { viewModel.seleccionarPunto(it) }
                    )
                }

                // ── Tarjeta de Detalle del Punto Seleccionado ──────────────
                item {
                    val seleccionado = uiState.puntoSeleccionado
                    if (seleccionado != null) {
                        TarjetaDetallePunto(
                            punto = seleccionado,
                            onComoLlegar = {
                                val uri = Uri.parse("geo:${seleccionado.latitud},${seleccionado.longitud}?q=${Uri.encode(seleccionado.nombre + " " + seleccionado.direccion)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${seleccionado.latitud},${seleccionado.longitud}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            },
                            onLlamar = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${seleccionado.telefono.replace(" ", "")}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // ── Lista de Todos los Puntos Disponibles ──────────────────
                item {
                    Text(
                        text = "Todos los Puntos Habilitados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark,
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }

                items(puntosFiltrados, key = { it.id }) { punto ->
                    PuntoItemCard(
                        punto = punto,
                        esSeleccionado = punto.id == uiState.puntoSeleccionado?.id,
                        onClick = { viewModel.seleccionarPunto(punto) }
                    )
                }
            }
        }
    }
}

/**
 * Lienzo visual estilizado que simula el mapa de Santiago con pines interactivos.
 */
@Composable
private fun VisualMapCanvas(
    puntos: List<PuntoRetiroEntity>,
    puntoSeleccionado: PuntoRetiroEntity?,
    onPuntoClick: (PuntoRetiroEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFE6D8)),
        border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.6f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con textura de mapa urbano
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFF7F2EA), Color(0xFFE5DAC7)),
                            radius = 600f
                        )
                    )
            )

            // Líneas decorativas representando avenidas principales (Providencia, Alameda)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .offset(y = 100.dp)
                    .background(AmberGold.copy(alpha = 0.25f))
            )
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .offset(x = 180.dp)
                    .background(OliveGreen.copy(alpha = 0.20f))
            )

            // Etiqueta de la ciudad
            Text(
                text = "📍 Santiago, Región Metropolitana",
                style = MaterialTheme.typography.labelSmall,
                color = BrownMedium.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )

            // Pines de los puntos de retiro en posiciones relativas
            puntos.forEachIndexed { index, punto ->
                val esSeleccionado = punto.id == puntoSeleccionado?.id
                // Coordenadas visuales relativas en el lienzo
                val offsetX = when (index % 4) {
                    0 -> 50.dp
                    1 -> 140.dp
                    2 -> 230.dp
                    else -> 300.dp
                }
                val offsetY = when (index % 4) {
                    0 -> 60.dp
                    1 -> 130.dp
                    2 -> 75.dp
                    else -> 125.dp
                }

                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .clip(CircleShape)
                        .clickable { onPuntoClick(punto) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = if (esSeleccionado) AmberGold else if (punto.esTiendaPrincipal) BrownDark else BrownMedium,
                            shadowElevation = if (esSeleccionado) 8.dp else 4.dp,
                            border = if (esSeleccionado) BorderStroke(2.dp, CreamWhite) else null,
                            modifier = Modifier.size(if (esSeleccionado) 42.dp else 34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (punto.esTiendaPrincipal) "🌰" else "📍",
                                    fontSize = if (esSeleccionado) 18.sp else 14.sp
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrownDark.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = punto.comuna.split(",").firstOrNull() ?: punto.nombre,
                                style = MaterialTheme.typography.labelSmall,
                                color = CreamWhite,
                                fontSize = 9.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Indicador de modo demostración
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrownDark.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Toca un pin para ver detalles",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldenLight,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta completa con todos los datos del punto de retiro seleccionado.
 */
@Composable
private fun TarjetaDetallePunto(
    punto: PuntoRetiroEntity,
    onComoLlegar: () -> Unit,
    onLlamar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, AmberGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (punto.esTiendaPrincipal) AmberGold else WarmTan.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (punto.esTiendaPrincipal) "⭐ TIENDA MATRIZ" else "📦 PUNTO DE RETIRO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = OliveGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Retiro Gratis",
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = punto.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )

            Spacer(Modifier.height(8.dp))

            // Dirección
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = punto.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BrownDark
                    )
                    Text(
                        text = punto.comuna,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Horario
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = OliveGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = punto.horario,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrownDark
                )
            }

            Spacer(Modifier.height(6.dp))

            // Teléfono
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = BrownMedium,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = punto.telefono,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrownDark
                )
            }

            Spacer(Modifier.height(10.dp))

            // Indicaciones de retiro
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldenLight.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = punto.indicaciones,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownDark
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onComoLlegar,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = BrownDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Cómo Llegar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onLlamar,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AmberGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = BrownDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Llamar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrownDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Ítem de la lista de puntos de retiro.
 */
@Composable
private fun PuntoItemCard(
    punto: PuntoRetiroEntity,
    esSeleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esSeleccionado) GoldenLight.copy(alpha = 0.35f) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (esSeleccionado) AmberGold else WarmTan.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (punto.esTiendaPrincipal) AmberGold else WarmTan.copy(alpha = 0.6f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (punto.esTiendaPrincipal) "🌰" else "📍",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = punto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${punto.direccion} • ${punto.comuna}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrownMedium.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "⏰ ${punto.horario}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (esSeleccionado) AmberGold else BrownMedium.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FiltroChip(
    label: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (seleccionado) AmberGold else WarmTan.copy(alpha = 0.35f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
            color = if (seleccionado) BrownDark else BrownMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
