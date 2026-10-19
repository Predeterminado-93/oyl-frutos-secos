
package com.OYL.frutossecos.ui.screens.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidosScreen(
    onVolver: () -> Unit,
    viewModel: PedidosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val pedidosFiltrados = uiState.pedidos.filter {
        uiState.estadoFiltro == "TODOS" || it.estado == uiState.estadoFiltro
    }

    Scaffold(
        containerColor = CreamWhite,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamWhite)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = BrownDark
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.esAdministrador) {
                            "Gestión de Pedidos"
                        } else {
                            "Mis Pedidos"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = if (uiState.esAdministrador) {
                            "Pedidos recibidos"
                        } else {
                            "Historial de compras"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroPedidoButton(
                    texto = "Todos",
                    seleccionado = uiState.estadoFiltro == "TODOS",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.cambiarFiltro("TODOS") }
                )
                FiltroPedidoButton(
                    texto = "Pendientes",
                    seleccionado = uiState.estadoFiltro == "PENDIENTE",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.cambiarFiltro("PENDIENTE") }
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroPedidoButton(
                    texto = "Preparación",
                    seleccionado = uiState.estadoFiltro == "EN_PREPARACION",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.cambiarFiltro("EN_PREPARACION") }
                )
                FiltroPedidoButton(
                    texto = "Listos",
                    seleccionado = uiState.estadoFiltro == "LISTO_RETIRO",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.cambiarFiltro("LISTO_RETIRO") }
                )
                FiltroPedidoButton(
                    texto = "Entregados",
                    seleccionado = uiState.estadoFiltro == "ENTREGADO",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.cambiarFiltro("ENTREGADO") }
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                uiState.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AmberGold)
                    }
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                pedidosFiltrados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay pedidos para este filtro.",
                            color = BrownMedium
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pedidosFiltrados, key = { it.id }) { pedido ->
                            PedidoCard(
                                pedido = pedido,
                                esAdministrador = uiState.esAdministrador,
                                guardando = uiState.guardando,
                                onCambiarEstado = {
                                    viewModel.actualizarEstado(pedido.id, it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val mensaje = uiState.mensaje
    val error = uiState.error

    if (mensaje != null || (error != null && !uiState.cargando)) {
        AlertDialog(
            onDismissRequest = { viewModel.limpiarMensajes() },
            title = {
                Text(
                    text = if (mensaje != null) "Operación completada" else "Aviso",
                    color = BrownDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(mensaje ?: error.orEmpty())
            },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarMensajes() }) {
                    Text("Aceptar", color = BrownDark)
                }
            },
            containerColor = CreamWhite
        )
    }
}

@Composable
private fun FiltroPedidoButton(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (seleccionado) {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 42.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberGold,
                contentColor = BrownDark
            )
        ) {
            Text(texto, style = MaterialTheme.typography.labelSmall)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 42.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BrownDark
            )
        ) {
            Text(texto, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PedidoCard(
    pedido: PedidoEntity,
    esAdministrador: Boolean,
    guardando: Boolean,
    onCambiarEstado: (String) -> Unit
) {
    var mostrarOpcionesEstado by remember(pedido.estado) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date(pedido.fecha)),
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorEstado(pedido.estado).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = textoEstado(pedido.estado),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = colorEstado(pedido.estado),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (esAdministrador) {
                Text(
                    text = "Cliente: ${pedido.usuarioAlias}",
                    color = BrownMedium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = pedido.itemsResumen,
                color = BrownDark,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Entrega: ${pedido.metodoEntrega}",
                color = BrownMedium,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Destino: ${pedido.puntoRetiroNombre}",
                color = BrownMedium,
                style = MaterialTheme.typography.bodySmall
            )

            if (esAdministrador && pedido.telefonoContacto.isNotBlank()) {
                Text(
                    text = "Contacto: ${pedido.telefonoContacto}",
                    color = BrownMedium,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(color = CreamWhite)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    color = BrownMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "\$${"%,.0f".format(Locale.forLanguageTag("es-CL"), pedido.total)}",
                    color = BrownDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (esAdministrador && pedido.estado != "ENTREGADO") {
                Box {
                    OutlinedButton(
                        onClick = { mostrarOpcionesEstado = true },
                        enabled = !guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cambiar estado")
                    }

                    DropdownMenu(
                        expanded = mostrarOpcionesEstado,
                        onDismissRequest = { mostrarOpcionesEstado = false }
                    ) {
                        val siguientesEstados = when (pedido.estado) {
                            "PENDIENTE" -> listOf("EN_PREPARACION")
                            "EN_PREPARACION" -> listOf("LISTO_RETIRO")
                            "LISTO_RETIRO" -> listOf("ENTREGADO")
                            else -> emptyList()
                        }

                        siguientesEstados.forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(textoEstado(estado)) },
                                onClick = {
                                    mostrarOpcionesEstado = false
                                    onCambiarEstado(estado)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun textoEstado(estado: String): String {
    return when (estado) {
        "PENDIENTE" -> "Pendiente"
        "EN_PREPARACION" -> "En preparación"
        "LISTO_RETIRO" -> "Listo para retiro"
        "ENTREGADO" -> "Entregado"
        else -> estado
    }
}

private fun colorEstado(estado: String): Color {
    return when (estado) {
        "PENDIENTE" -> AmberGold
        "EN_PREPARACION" -> Color(0xFF3976A8)
        "LISTO_RETIRO" -> OliveGreen
        "ENTREGADO" -> BrownMedium
        else -> BrownDark
    }
}
