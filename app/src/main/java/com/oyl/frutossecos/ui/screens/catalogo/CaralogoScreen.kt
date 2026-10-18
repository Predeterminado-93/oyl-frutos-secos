package com.OYL.frutossecos.ui.screens.catalogo


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.ui.components.FotoProducto
import com.OYL.frutossecos.ui.theme.AmberGold
import com.OYL.frutossecos.ui.theme.BrownDark
import com.OYL.frutossecos.ui.theme.BrownMedium
import com.OYL.frutossecos.ui.theme.CreamWhite
import com.OYL.frutossecos.ui.theme.ErrorRed
import com.OYL.frutossecos.ui.theme.OliveGreen
import com.OYL.frutossecos.ui.theme.WarmTan
import com.OYL.frutossecos.ui.util.formatearPrecio

/**
 * Pantalla 6 — Catálogo de Productos
 *
 * Roles: Administrador y Cliente.
 * Cliente: ve el detalle y agrega al carrito. Administrador: abre las opciones de edición.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onVolver: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onEditarProducto: (Int) -> Unit,
    onIrAlCarrito: () -> Unit,
    viewModel: CatalogoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.aviso) {
        val aviso = uiState.aviso ?: return@LaunchedEffect
        val resultado = snackbarHostState.showSnackbar(
            message = aviso.texto,
            actionLabel = if (aviso.exito) "Ver carrito" else null,
            duration = SnackbarDuration.Short
        )
        viewModel.avisoMostrado()
        if (resultado == SnackbarResult.ActionPerformed) onIrAlCarrito()
    }

    val categorias = remember(uiState.productos) {
        listOf("Todos") + uiState.productos.map { it.categoria }.distinct()
    }
    val productosFiltrados = remember(uiState.productos, uiState.busqueda, uiState.categoriaSeleccionada) {
        uiState.productos.filter { producto ->
            val coincideCategoria = uiState.categoriaSeleccionada == "Todos" ||
                    producto.categoria == uiState.categoriaSeleccionada
            val coincideBusqueda = uiState.busqueda.isBlank() ||
                    producto.nombre.contains(uiState.busqueda.trim(), ignoreCase = true)
            coincideCategoria && coincideBusqueda
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catálogo de Productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrownDark
                        )
                        Text(
                            text = "Frutos secos, semillas y deshidratados",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrownMedium.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrownDark)
                    }
                },
                actions = {
                    if (!uiState.esAdmin) {
                        IconButton(onClick = onIrAlCarrito) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Ir al carrito", tint = BrownDark)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamWhite)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CreamWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Búsqueda ───────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = viewModel::onBusquedaChanged,
                placeholder = { Text("Buscar productos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrownMedium) },
                trailingIcon = {
                    if (uiState.busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onBusquedaChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda", tint = BrownMedium)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = WarmTan,
                    cursorColor = AmberGold,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Filtros por categoría ──────────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { categoria ->
                    item(key = categoria) {
                        FiltroChip(
                            label = categoria,
                            seleccionado = uiState.categoriaSeleccionada == categoria,
                            onClick = { viewModel.seleccionarCategoria(categoria) }
                        )
                    }
                }
            }

            // ── Contenido ──────────────────────────────────────────────────
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmberGold)
                }

                productosFiltrados.isEmpty() -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No encontramos productos con esa búsqueda. Prueba con otra palabra o cambia la categoría.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrownMedium,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productosFiltrados, key = { it.id }) { producto ->
                        ProductoCard(
                            producto = producto,
                            esAdmin = uiState.esAdmin,
                            onClick = { onVerDetalle(producto.id) },
                            onAccion = {
                                if (uiState.esAdmin) onEditarProducto(producto.id)
                                else viewModel.agregarAlCarrito(producto)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(
    producto: ProductoEntity,
    esAdmin: Boolean,
    onClick: () -> Unit,
    onAccion: () -> Unit
) {
    val sinStock = producto.stock <= 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.4f))
    ) {
        Column {
            FotoProducto(
                url = producto.fotoUrl,
                descripcion = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = producto.peso,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrownMedium.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatearPrecio(producto.precio),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrownDark
                        )
                        Text(
                            text = if (sinStock) "Sin stock" else "Stock: ${producto.stock} un.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (producto.stock <= 5) ErrorRed else OliveGreen
                        )
                    }
                    FilledIconButton(
                        onClick = onAccion,
                        enabled = esAdmin || !sinStock,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = OliveGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (esAdmin) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = if (esAdmin) "Editar ${producto.nombre}" else "Agregar ${producto.nombre} al carrito",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroChip(label: String, seleccionado: Boolean, onClick: () -> Unit) {
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
