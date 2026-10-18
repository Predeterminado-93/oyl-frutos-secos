package com.OYL.frutossecos.ui.screens.escanerqr

import android.Manifest
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

/**
 * Pantalla 8 — Escáner QR de Productos
 *
 * Roles: Administrador y Cliente.
 * Lee el QR con la cámara (CameraX + ZXing), valida el código y muestra el producto.
 * Cliente: abre la ficha. Administrador: además ajusta el inventario (se guarda en SQLite).
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EscanerQrScreen(
    onVolver: () -> Unit,
    onVerProducto: (Int) -> Unit,
    viewModel: EscanerQrViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val permisoCamara = rememberPermissionState(Manifest.permission.CAMERA)
    val snackbarHostState = remember { SnackbarHostState() }

    var linterna by remember { mutableStateOf(false) }
    var mostrarManual by remember { mutableStateOf(false) }
    var codigoManual by remember { mutableStateOf("") }

    // Guardamos el último producto para que la tarjeta se anime también al cerrarse
    var ultimoProducto by remember { mutableStateOf<ProductoEntity?>(null) }
    LaunchedEffect(uiState.productoEncontrado) {
        uiState.productoEncontrado?.let { ultimoProducto = it }
    }

    LaunchedEffect(Unit) {
        if (!permisoCamara.status.isGranted) permisoCamara.launchPermissionRequest()
    }

    LaunchedEffect(uiState.aviso) {
        val aviso = uiState.aviso ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = aviso.texto, duration = SnackbarDuration.Short)
        viewModel.avisoMostrado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escanea el código QR del producto",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BrownDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BrownDark)
        ) {
            // ── Cámara o aviso de permiso ──────────────────────────────────
            if (permisoCamara.status.isGranted) {
                VistaCamara(
                    linternaEncendida = linterna,
                    onCodigoLeido = viewModel::onCodigoLeido,
                    modifier = Modifier.fillMaxSize()
                )
                // Marco de escaneo
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(240.dp)
                        .border(3.dp, AmberGold, RoundedCornerShape(24.dp))
                )
                IconButton(
                    onClick = { linterna = !linterna },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (linterna) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (linterna) "Apagar linterna" else "Encender linterna",
                        tint = Color.White
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "📷", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = "Necesitamos permiso de cámara para leer el QR.",
                        style = MaterialTheme.typography.titleMedium,
                        color = CreamWhite,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Si no aparece la ventana de permiso, actívalo en Ajustes > Apps > OYL Frutos Secos > Permisos. También puedes ingresar el código manualmente abajo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CreamWhite.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { permisoCamara.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = BrownDark)
                    ) { Text("Permitir cámara", fontWeight = FontWeight.Bold) }
                }
            }

            // ── Animación de lectura exitosa ───────────────────────────────
            AnimatedVisibility(
                visible = uiState.lecturaExitosa,
                enter = scaleIn() + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(shape = CircleShape, color = OliveGreen, modifier = Modifier.size(96.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Lectura exitosa",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            // ── Error de código inválido ───────────────────────────────────
            AnimatedVisibility(
                visible = uiState.errorCodigo != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = ErrorRed) {
                    Text(
                        text = uiState.errorCodigo.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // ── Panel inferior: instrucciones o resultado ──────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AnimatedVisibility(
                    visible = uiState.productoEncontrado != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    ultimoProducto?.let { producto ->
                        TarjetaResultado(
                            producto = producto,
                            esAdmin = uiState.esAdmin,
                            nuevoStock = uiState.nuevoStock,
                            onMenos = viewModel::disminuirStock,
                            onMas = viewModel::aumentarStock,
                            onGuardar = viewModel::guardarInventario,
                            onVerFicha = { onVerProducto(producto.id) },
                            onEscanearOtro = viewModel::cerrarResultado
                        )
                    }
                }

                if (uiState.productoEncontrado == null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Apunta la cámara al código QR para obtener información del producto.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrownDark
                            )
                            TextButton(onClick = { mostrarManual = !mostrarManual }) {
                                Text(
                                    text = if (mostrarManual) "Ocultar ingreso manual" else "¿Sin cámara? Ingresar código",
                                    color = BrownMedium
                                )
                            }
                            if (mostrarManual) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = codigoManual,
                                        onValueChange = { codigoManual = it },
                                        placeholder = { Text("OYL-PROD-1") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AmberGold,
                                            cursorColor = AmberGold
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = { viewModel.onCodigoLeido(codigoManual) },
                                        enabled = codigoManual.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AmberGold,
                                            contentColor = BrownDark
                                        )
                                    ) { Text("Buscar", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaResultado(
    producto: ProductoEntity,
    esAdmin: Boolean,
    nuevoStock: Int,
    onMenos: () -> Unit,
    onMas: () -> Unit,
    onGuardar: () -> Unit,
    onVerFicha: () -> Unit,
    onEscanearOtro: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = CreamWhite,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoProducto(
                    url = producto.fotoUrl,
                    descripcion = producto.nombre,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    Text(
                        text = "${formatearPrecio(producto.precio)} • ${producto.peso}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrownMedium
                    )
                    Text(
                        text = if (producto.stock > 0) "Disponible: ${producto.stock} un." else "Sin stock",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (producto.stock > 5) OliveGreen else ErrorRed
                    )
                }
            }

            if (esAdmin) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ajustar inventario",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrownDark
                        )
                        SelectorCantidad(cantidad = nuevoStock, onMenos = onMenos, onMas = onMas)
                    }
                    Button(
                        onClick = onGuardar,
                        enabled = nuevoStock != producto.stock,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OliveGreen, contentColor = Color.White)
                    ) { Text("Guardar inventario", fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onVerFicha,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = BrownDark)
                ) { Text("Ver ficha", fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = onEscanearOtro,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Escanear otro", color = BrownDark, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

/** Vista previa de la cámara trasera + análisis de cada fotograma buscando un QR. */
@Composable
private fun VistaCamara(
    linternaEncendida: Boolean,
    onCodigoLeido: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var camara by remember { mutableStateOf<Camera?>(null) }
    val codigoLeido by rememberUpdatedState(onCodigoLeido)

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    LaunchedEffect(camara, linternaEncendida) {
        camara?.cameraControl?.enableTorch(linternaEncendida)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analisis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(executor, AnalizadorQr { texto -> codigoLeido(texto) }) }

                    provider.unbindAll()
                    camara = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analisis
                    )
                } catch (e: Exception) {
                    Log.e("EscanerQr", "No se pudo iniciar la cámara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

/** Analiza el plano de luminancia (Y) de cada fotograma y busca un QR con ZXing. */
private class AnalizadorQr(
    private val onQrDetectado: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val lector = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
    }

    override fun analyze(image: ImageProxy) {
        try {
            val plano = image.planes[0]
            val buffer = plano.buffer
            val datos = ByteArray(buffer.remaining())
            buffer.get(datos)

            val fuente = PlanarYUVLuminanceSource(
                datos, plano.rowStride, image.height,
                0, 0, image.width, image.height,
                false
            )
            val resultado = lector.decodeWithState(BinaryBitmap(HybridBinarizer(fuente)))
            onQrDetectado(resultado.text)
        } catch (_: Exception) {
            // No hay QR en este fotograma: es lo normal, se sigue intentando
        } finally {
            lector.reset()
            image.close()
        }
    }
}
