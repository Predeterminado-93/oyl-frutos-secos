package com.OYL.frutossecos.ui.screens.confirmacion

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.repository.METODO_DESPACHO
import com.OYL.frutossecos.data.repository.METODO_RETIRO
import com.OYL.frutossecos.data.repository.COSTO_DESPACHO
import com.OYL.frutossecos.ui.theme.AmberGold
import com.OYL.frutossecos.ui.theme.BrownDark
import com.OYL.frutossecos.ui.theme.BrownMedium
import com.OYL.frutossecos.ui.theme.CreamWhite
import com.OYL.frutossecos.ui.theme.ErrorRed
import com.OYL.frutossecos.ui.theme.GoldenLight
import com.OYL.frutossecos.ui.theme.OliveGreen
import com.OYL.frutossecos.ui.theme.WarmTan
import com.OYL.frutossecos.ui.util.formatearFecha
import com.OYL.frutossecos.ui.util.formatearNumeroPedido
import com.OYL.frutossecos.ui.util.formatearPrecio

/**
 * Pantalla 10 — Confirmación del Pedido
 *
 * Rol: Cliente.
 * Paso 1: resumen (entrega, contacto, total) y botón "Confirmar pedido".
 * Paso 2: pantalla "¡Pedido confirmado!" con número, fecha, total y accesos directos.
 */
@Composable
fun ConfirmacionScreen(
    onVolver: () -> Unit,
    onVerMisPedidos: () -> Unit,
    onSeguirComprando: () -> Unit,
    viewModel: ConfirmacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pedido = uiState.pedidoConfirmado

    if (pedido != null) {
        BackHandler(onBack = onSeguirComprando)
        PedidoConfirmado(
            pedido = pedido,
            onVerMisPedidos = onVerMisPedidos,
            onSeguirComprando = onSeguirComprando
        )
    } else {
        ResumenPedido(uiState = uiState, viewModel = viewModel, onVolver = onVolver)
    }
}

// ── Paso 1: resumen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResumenPedido(
    uiState: ConfirmacionUiState,
    viewModel: ConfirmacionViewModel,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirmar pedido",
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
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        FilaMonto("Subtotal", formatearPrecio(uiState.subtotal))
                        FilaMonto(
                            "Despacho",
                            if (uiState.costoDespacho > 0) formatearPrecio(uiState.costoDespacho) else "Gratis"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = WarmTan.copy(alpha = 0.5f)
                        )
                        FilaMonto("Total", formatearPrecio(uiState.total), destacado = true)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::confirmar,
                            enabled = !uiState.isEnviando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OliveGreen, contentColor = Color.White)
                        ) {
                            if (uiState.isEnviando) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Enviando pedido...", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Confirmar pedido", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
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
                Text(
                    text = uiState.errorMessage ?: "Tu carrito está vacío.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrownDark,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = BrownDark)
                ) { Text("Volver", fontWeight = FontWeight.Bold) }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Artículos
                item { Titulo("Tu pedido") }
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            uiState.items.forEach { item ->
                                FilaMonto(
                                    etiqueta = "${item.cantidad} x ${item.productoNombre}",
                                    valor = formatearPrecio(item.productoPrecio * item.cantidad)
                                )
                            }
                        }
                    }
                }

                // Modalidad de entrega
                item { Titulo("Modalidad de entrega") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OpcionEntrega(
                            titulo = "🏪 Retiro en tienda",
                            detalle = "Gratis",
                            seleccionada = uiState.metodoEntrega == METODO_RETIRO,
                            onClick = { viewModel.seleccionarMetodo(METODO_RETIRO) },
                            modifier = Modifier.weight(1f)
                        )
                        OpcionEntrega(
                            titulo = "🚚 Despacho",
                            detalle = formatearPrecio(COSTO_DESPACHO),
                            seleccionada = uiState.metodoEntrega == METODO_DESPACHO,
                            onClick = { viewModel.seleccionarMetodo(METODO_DESPACHO) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (uiState.metodoEntrega == METODO_RETIRO) {
                    item { Titulo("Punto de retiro") }
                    items(uiState.puntos, key = { it.id }) { punto ->
                        val seleccionado = punto.id == uiState.puntoSeleccionado?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.seleccionarPunto(punto) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionado) GoldenLight.copy(alpha = 0.35f) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (seleccionado) AmberGold else WarmTan.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionado,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = AmberGold)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = punto.nombre,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrownDark
                                    )
                                    Text(
                                        text = "${punto.direccion} • ${punto.comuna}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrownMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Contacto
                item { Titulo("Datos de contacto") }
                item {
                    OutlinedTextField(
                        value = uiState.telefono,
                        onValueChange = viewModel::onTelefonoChanged,
                        label = { Text("Teléfono de contacto") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = WarmTan,
                            focusedLabelColor = BrownMedium,
                            cursorColor = AmberGold,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Error
                uiState.errorMessage?.let { mensaje ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ErrorRed.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorRed,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Paso 2: pedido confirmado ────────────────────────────────────────────────
@Composable
private fun PedidoConfirmado(
    pedido: PedidoEntity,
    onVerMisPedidos: () -> Unit,
    onSeguirComprando: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            Surface(shape = CircleShape, color = OliveGreen, modifier = Modifier.size(110.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Pedido confirmado",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "¡Pedido confirmado!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = BrownDark
        )
        Text(
            text = "Tu pedido ha sido recibido correctamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = BrownMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WarmTan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FilaMonto("N° de pedido", formatearNumeroPedido(pedido.id))
                FilaMonto("Fecha", formatearFecha(pedido.fecha))
                FilaMonto("Estado", "Pendiente de validación")
                FilaMonto("Método de entrega", pedido.metodoEntrega)
                FilaMonto("Destino", pedido.puntoRetiroNombre)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmTan.copy(alpha = 0.5f))
                FilaMonto("Total", formatearPrecio(pedido.total), destacado = true)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onVerMisPedidos,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OliveGreen, contentColor = Color.White)
        ) { Text("Ver mis pedidos", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onSeguirComprando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, AmberGold)
        ) { Text("Seguir comprando", color = BrownDark, fontWeight = FontWeight.SemiBold) }
    }
}

// ── Componentes pequeños ─────────────────────────────────────────────────────
@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = BrownDark,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun OpcionEntrega(
    titulo: String,
    detalle: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) GoldenLight.copy(alpha = 0.35f) else Color.White
        ),
        border = BorderStroke(if (seleccionada) 1.5.dp else 1.dp, if (seleccionada) AmberGold else WarmTan.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )
            Text(text = detalle, style = MaterialTheme.typography.bodySmall, color = OliveGreen)
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = BrownDark,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold,
            color = BrownDark,
            textAlign = TextAlign.End
        )
    }
}
