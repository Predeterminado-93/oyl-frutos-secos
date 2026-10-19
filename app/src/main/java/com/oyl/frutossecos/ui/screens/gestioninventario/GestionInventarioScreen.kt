package com.OYL.frutossecos.ui.screens.gestioninventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.ui.theme.*

@Composable
fun GestionInventarioScreen(
    onVolver: () -> Unit,
    viewModel: GestionInventarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarMovimientos by remember { mutableStateOf(false) }
    var mostrarDialogoMovimiento by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensaje) {
        if (uiState.mensaje != null) {
            mostrarDialogoMovimiento = false
        }
    }

    val productosFiltrados = uiState.productos.filter {
        it.nombre.contains(uiState.busqueda, ignoreCase = true) ||
                it.categoria.contains(uiState.busqueda, ignoreCase = true)
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = BrownDark
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gestión de Inventario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = "Existencias y movimientos",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium
                    )
                }
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                val stockBajo = uiState.productos.count {
                    it.activo && it.stock <= 5
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResumenInventarioCard(
                        titulo = "Productos",
                        valor = "${uiState.productos.count { it.activo }}",
                        modifier = Modifier.weight(1f)
                    )
                    ResumenInventarioCard(
                        titulo = "Stock bajo",
                        valor = "$stockBajo",
                        alerta = stockBajo > 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.busqueda,
                    onValueChange = viewModel::cambiarBusqueda,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto o categoría") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { mostrarMovimientos = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!mostrarMovimientos) AmberGold else Color.White,
                            contentColor = BrownDark
                        )
                    ) {
                        Text("Existencias")
                    }
                    OutlinedButton(
                        onClick = { mostrarMovimientos = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrownDark
                        )
                    ) {
                        Text("Movimientos")
                    }
                }
            }

            if (!mostrarMovimientos) {
                item {
                    Text(
                        text = "Inventario de productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                }

                if (productosFiltrados.isEmpty()) {
                    item {
                        Text(
                            text = "No se encontraron productos.",
                            color = BrownMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                items(productosFiltrados, key = { it.id }) { producto ->
                    ProductoInventarioCard(
                        producto = producto,
                        onRegistrarMovimiento = {
                            viewModel.limpiarMensaje()
                            viewModel.seleccionarProducto(producto)
                            mostrarDialogoMovimiento = true
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Historial de movimientos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                }

                if (uiState.movimientos.isEmpty()) {
                    item {
                        Text(
                            text = "Todavía no hay movimientos registrados.",
                            color = BrownMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                items(uiState.movimientos, key = { it.id }) { movimiento ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = movimiento.nombreProducto,
                                fontWeight = FontWeight.Bold,
                                color = BrownDark
                            )
                            Text(
                                text = "${movimiento.tipo}: ${movimiento.cantidad} unidades",
                                color = if (movimiento.tipo == "ENTRADA") {
                                    OliveGreen
                                } else {
                                    ErrorRed
                                }
                            )
                            Text(
                                text = "Stock: ${movimiento.stockAnterior} → ${movimiento.stockNuevo}",
                                color = BrownMedium
                            )
                            if (movimiento.observacion.isNotBlank()) {
                                Text(
                                    text = movimiento.observacion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrownMedium
                                )
                            }
                            Text(
                                text = if (movimiento.sincronizado) {
                                    "Sincronizado"
                                } else {
                                    "Pendiente de sincronización"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = BrownMedium
                            )
                        }
                    }
                }
            }
        }
    }

    val productoSeleccionado = uiState.productoSeleccionado

    if (mostrarDialogoMovimiento && productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoMovimiento = false
                viewModel.limpiarMensaje()
            },
            title = {
                Text(
                    text = "Registrar movimiento",
                    fontWeight = FontWeight.Bold,
                    color = BrownDark
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = productoSeleccionado.nombre,
                        fontWeight = FontWeight.SemiBold,
                        color = BrownDark
                    )
                    Text(
                        text = "Stock actual: ${productoSeleccionado.stock} unidades",
                        color = BrownMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.cambiarTipo("ENTRADA") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.tipoMovimiento == "ENTRADA") {
                                    OliveGreen
                                } else {
                                    Color.LightGray
                                },
                                contentColor = if (uiState.tipoMovimiento == "ENTRADA") {
                                    Color.White
                                } else {
                                    BrownDark
                                }
                            )
                        ) {
                            Text("Entrada")
                        }

                        Button(
                            onClick = { viewModel.cambiarTipo("SALIDA") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.tipoMovimiento == "SALIDA") {
                                    ErrorRed
                                } else {
                                    Color.LightGray
                                },
                                contentColor = if (uiState.tipoMovimiento == "SALIDA") {
                                    Color.White
                                } else {
                                    BrownDark
                                }
                            )
                        ) {
                            Text("Salida")
                        }
                    }

                    OutlinedTextField(
                        value = uiState.cantidad,
                        onValueChange = viewModel::cambiarCantidad,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cantidad de unidades") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.observacion,
                        onValueChange = viewModel::cambiarObservacion,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observación (opcional)") },
                        singleLine = true
                    )

                    uiState.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::registrarMovimiento,
                    enabled = !uiState.guardando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = BrownDark
                    )
                ) {
                    Text(
                        if (uiState.guardando) "Guardando..." else "Confirmar",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoMovimiento = false
                        viewModel.limpiarMensaje()
                    },
                    enabled = !uiState.guardando
                ) {
                    Text("Cancelar", color = BrownMedium)
                }
            },
            containerColor = CreamWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.mensaje != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.limpiarMensaje()
                // La pantalla conserva el historial y el inventario actualizados.
            },
            title = {
                Text("Movimiento registrado", color = BrownDark)
            },
            text = {
                Text(uiState.mensaje ?: "")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.limpiarMensaje()
                    }
                ) {
                    Text("Aceptar", color = BrownDark)
                }
            },
            containerColor = CreamWhite
        )
    }
}

@Composable
private fun ResumenInventarioCard(
    titulo: String,
    valor: String,
    alerta: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = titulo,
                color = BrownMedium,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = valor,
                color = if (alerta) ErrorRed else BrownDark,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (alerta) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Revisar",
                        color = ErrorRed,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoInventarioCard(
    producto: ProductoEntity,
    onRegistrarMovimiento: () -> Unit
) {
    val stockBajo = producto.stock <= 5

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrownMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stockBajo) {
                        ErrorRed.copy(alpha = 0.12f)
                    } else {
                        OliveGreen.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = "${producto.stock} unidades",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = if (stockBajo) ErrorRed else OliveGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (stockBajo) {
                Text(
                    text = "Stock bajo: quedan 5 unidades o menos.",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onRegistrarMovimiento,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = BrownDark
                    )
                ) {
                    Text("Registrar movimiento")
                }
            }
        }
    }
}
