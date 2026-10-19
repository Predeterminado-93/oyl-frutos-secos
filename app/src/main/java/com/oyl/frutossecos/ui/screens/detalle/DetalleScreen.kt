package com.OYL.frutossecos.ui.screens.detalle


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.ui.components.FotoProducto
import com.OYL.frutossecos.ui.components.SelectorCantidad
import com.OYL.frutossecos.ui.theme.AmberGold
import com.OYL.frutossecos.ui.theme.BrownDark
import com.OYL.frutossecos.ui.theme.BrownMedium
import com.OYL.frutossecos.ui.theme.CreamWhite
import com.OYL.frutossecos.ui.theme.ErrorRed
import com.OYL.frutossecos.ui.theme.OliveGreen
import com.OYL.frutossecos.ui.util.formatearPrecio

/**
 * Pantalla 7 — Detalle del Producto
 *
 * Roles: Administrador y Cliente.
 * Cliente: elige cantidad y agrega al carrito (si hay stock). Administrador: accede a la edición.
 */
@Composable
fun DetalleScreen(
    productoId: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    onIrAlCarrito: () -> Unit,
    viewModel: DetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productoId) { viewModel.cargar(productoId) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CreamWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            val producto = uiState.producto
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    color = AmberGold,
                    modifier = Modifier.align(Alignment.Center)
                )

                producto == null -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Este producto ya no está disponible en el catálogo.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BrownDark,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onVolver,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = BrownDark)
                    ) { Text("Volver al catálogo") }
                }

                else -> ContenidoDetalle(
                    producto = producto,
                    cantidad = uiState.cantidad,
                    esAdmin = uiState.esAdmin,
                    onMenos = viewModel::disminuirCantidad,
                    onMas = viewModel::aumentarCantidad,
                    onAgregar = viewModel::agregarAlCarrito,
                    onEditar = { onEditar(producto.id) }
                )
            }

            // Botón volver flotando sobre la foto
            IconButton(
                onClick = onVolver,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.9f)),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrownDark)
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    producto: ProductoEntity,
    cantidad: Int,
    esAdmin: Boolean,
    onMenos: () -> Unit,
    onMas: () -> Unit,
    onAgregar: () -> Unit,
    onEditar: () -> Unit
) {
    val (textoStock, colorStock) = when {
        producto.stock <= 0 -> "Sin stock" to ErrorRed
        producto.stock <= 5 -> "Stock bajo: ${producto.stock} un." to AmberGold
        else -> "En stock (${producto.stock} un.)" to OliveGreen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        FotoProducto(
            url = producto.fotoUrl,
            descripcion = producto.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = CreamWhite,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-28).dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatearPrecio(producto.precio),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "(${producto.peso})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrownMedium.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(50), color = colorStock.copy(alpha = 0.15f)) {
                    Text(
                        text = "● $textoStock",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorStock,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrownMedium
                )

                Spacer(Modifier.height(20.dp))

                if (esAdmin) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = BrownDark)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Editar producto", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SelectorCantidad(cantidad = cantidad, onMenos = onMenos, onMas = onMas)
                        Button(
                            onClick = onAgregar,
                            enabled = producto.stock > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OliveGreen, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar al carrito", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                FilaInfo(emoji = "🏷️", etiqueta = "Categoría", valor = producto.categoria)
                FilaInfo(emoji = "⚖️", etiqueta = "Peso", valor = producto.peso)
                FilaInfo(emoji = "📍", etiqueta = "Origen", valor = producto.origen)
            }
        }
    }
}

@Composable
private fun FilaInfo(emoji: String, etiqueta: String, valor: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrownMedium.copy(alpha = 0.8f)
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrownDark
                )
            }
        }
    }
}
