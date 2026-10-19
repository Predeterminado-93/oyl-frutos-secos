package com.OYL.frutossecos.ui.screens.carrito


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.ui.components.FotoProducto
import com.OYL.frutossecos.ui.components.SelectorCantidad
import com.OYL.frutossecos.ui.theme.AmberGold
import com.OYL.frutossecos.ui.theme.BrownDark
import com.OYL.frutossecos.ui.theme.BrownMedium
import com.OYL.frutossecos.ui.theme.CreamWhite
import com.OYL.frutossecos.ui.theme.ErrorRed
import com.OYL.frutossecos.ui.theme.OliveGreen
import com.OYL.frutossecos.ui.theme.WarmTan
import com.OYL.frutossecos.ui.util.formatearPrecio

/**
 * Pantalla 9 — Carrito de Compras
 *
 * Rol: Cliente. El carrito vive en SQLite (Room) y es independiente por cliente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    onVolver: () -> Unit,
    onContinuarCompra: () -> Unit,
    onVerCatalogo: () -> Unit,
    viewModel: CarritoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.aviso) {
        val aviso = uiState.aviso ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = aviso.texto, duration = SnackbarDuration.Long)
        viewModel.avisoMostrado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Carrito (${uiState.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrownDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamWhite)
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                ResumenCarrito(
                    subtotal = uiState.subtotal,
                    onContinuar = { viewModel.continuarCompra(onContinuarCompra) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CreamWhite
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = AmberGold) }

            uiState.items.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🛒", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (uiState.sinSesion) "Inicia sesión para ver tu carrito." else "Tu carrito está vacío.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Agrega frutos secos, semillas o deshidratados desde el catálogo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrownMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onVerCatalogo,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = BrownDark)
                ) { Text("Ver catálogo", fontWeight = FontWeight.Bold) }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    ItemCarritoCard(
                        item = item,
                        onMenos = { viewModel.disminuir(item) },
                        onMas = { viewModel.aumentar(item) },
                        onEliminar = { viewModel.eliminar(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCarritoCard(
    item: CarritoItemEntity,
    onMenos: () -> Unit,
    onMas: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoProducto(
                url = item.productoFotoUrl,
                descripcion = item.productoNombre,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productoNombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.productoPeso.isNotBlank()) {
                    Text(
                        text = item.productoPeso,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrownMedium.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = formatearPrecio(item.productoPrecio * item.cantidad),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OliveGreen
                )
                if (item.cantidad > 1) {
                    Text(
                        text = "${formatearPrecio(item.productoPrecio)} c/u",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrownMedium.copy(alpha = 0.8f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar ${item.productoNombre}",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                SelectorCantidad(cantidad = item.cantidad, onMenos = onMenos, onMas = onMas, compacto = true)
            }
        }
    }
}

@Composable
private fun ResumenCarrito(subtotal: Double, onContinuar: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )
            Spacer(Modifier.height(6.dp))
            FilaMonto("Subtotal", formatearPrecio(subtotal))
            FilaMonto("Despacho", "Se define al confirmar")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmTan.copy(alpha = 0.5f))
            FilaMonto("Total", formatearPrecio(subtotal), destacado = true)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onContinuar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OliveGreen, contentColor = Color.White)
            ) {
                Text("Continuar compra", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = BrownDark
        )
        Text(
            text = valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold,
            color = BrownDark
        )
    }
}
