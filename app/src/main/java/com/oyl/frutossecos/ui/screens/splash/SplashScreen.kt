package com.OYL.frutossecos.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.OYL.frutossecos.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Pantalla 1 - Splash / Landing
 *
 * Presenta la marca OYL Frutos Secos con animaciones de entrada.
 * Roles: visible para todos (Admin y Cliente).
 * Acciones:
 *   - "Comenzar"           -> login (usuario nuevo)
 *   - "Ya tengo cuenta"    -> login (usuario existente)
 *   - "Ver catálogo"       -> catálogo sin sesión (solo lectura)
 *   - "Cómo llegar"        -> mapa de la tienda
 */
@Composable
fun SplashScreen(
    onComenzar: () -> Unit,
    onYaTengoCuenta: () -> Unit,
    onVerCatalogo: () -> Unit,
    onVerMapa: () -> Unit
) {
    // ── Estado de animaciones ──────────────────────────────────────────────
    var animacionLista by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        animacionLista = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (animacionLista) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val contenidoAlpha by animateFloatAsState(
        targetValue = if (animacionLista) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "contenidoAlpha"
    )
    val desplazamientoY by animateFloatAsState(
        targetValue = if (animacionLista) 0f else 60f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "desplazamientoY"
    )

    // ── Fondo degradado de la marca ────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrownDark, BrownMedium, Color(0xFF8B4A20))
                )
            )
    ) {
        // Círculos decorativos de fondo (efecto bokeh)
        DecorativeCircles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(64.dp))

            // ── Logo / ícono de la marca ───────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(logoScale)
            ) {
                // Ícono en círculo dorado
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AmberGold, Color(0xFF8B5E1A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌰",
                        fontSize = 56.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Nombre de la marca
                Text(
                    text = "OYL",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp
                    ),
                    color = AmberGold
                )
                Text(
                    text = "FRUTOS SECOS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 4.sp
                    ),
                    color = WarmTan
                )
            }

            // ── Descripción y chips de categorías ─────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(contenidoAlpha)
                    .offset(y = desplazamientoY.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Tu tienda de productos naturales",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CreamWhite,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Frutos secos, semillas, deshidratados y snacks naturales " +
                            "de la más alta calidad. Frescura y sabor en cada compra.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WarmTan,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(24.dp))

                // Chips de categorías
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoriaChip(emoji = "🥜", texto = "Frutos secos")
                    CategoriaChip(emoji = "🌱", texto = "Semillas")
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoriaChip(emoji = "🍇", texto = "Deshidratados")
                    CategoriaChip(emoji = "🍫", texto = "Snacks")
                }
            }

            // ── Botones de acción ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(contenidoAlpha)
                    .offset(y = desplazamientoY.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón principal: Comenzar
                Button(
                    onClick = onComenzar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = BrownDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Comenzar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón secundario: Ya tengo cuenta
                OutlinedButton(
                    onClick = onYaTengoCuenta,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CreamWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, CreamWhite.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ya tengo cuenta",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Accesos rápidos (Catálogo y Mapa)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ver catálogo
                    TextButton(
                        onClick = onVerCatalogo,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = WarmTan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Ver catálogo",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Cómo llegar
                    TextButton(
                        onClick = onVerMapa,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = WarmTan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Cómo llegar",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chip pequeño que muestra una categoría de producto con emoji.
 */
@Composable
private fun CategoriaChip(emoji: String, texto: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = BrownDark.copy(alpha = 0.5f),
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                color = WarmTan
            )
        }
    }
}

/**
 * Círculos decorativos de fondo (efecto visual tipo bokeh/glassmorphism).
 */
@Composable
private fun DecorativeCircles() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo superior derecha
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 250.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(AmberGold.copy(alpha = 0.15f))
                .blur(30.dp)
        )
        // Círculo inferior izquierda
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = 550.dp)
                .clip(CircleShape)
                .background(AmberGold.copy(alpha = 0.10f))
                .blur(40.dp)
        )
        // Círculo central
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 120.dp, y = 380.dp)
                .clip(CircleShape)
                .background(OliveGreen.copy(alpha = 0.08f))
                .blur(25.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    com.OYL.frutossecos.ui.theme.FrutosSecosTheme {
        SplashScreen(
            onComenzar = {},
            onYaTengoCuenta = {},
            onVerCatalogo = {},
            onVerMapa = {}
        )
    }
}
