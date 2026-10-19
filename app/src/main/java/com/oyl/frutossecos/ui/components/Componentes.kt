package com.OYL.frutossecos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.OYL.frutossecos.ui.theme.BrownDark
import com.OYL.frutossecos.ui.theme.WarmTan

/** Foto de producto (URL remota o URI local). Mientras carga se ve un fondo tostado. */
@Composable
fun FotoProducto(
    url: String,
    descripcion: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = url,
        contentDescription = descripcion,
        contentScale = contentScale,
        modifier = modifier.background(WarmTan.copy(alpha = 0.35f))
    )
}

/** Selector  [ − 1 + ]  usado en detalle, carrito y ajuste de inventario. */
@Composable
fun SelectorCantidad(
    cantidad: Int,
    onMenos: () -> Unit,
    onMas: () -> Unit,
    modifier: Modifier = Modifier,
    compacto: Boolean = false
) {
    val tamano = if (compacto) 28.dp else 38.dp
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BotonCirculo(Icons.Default.Remove, "Disminuir cantidad", onMenos, tamano)
        Text(
            text = cantidad.toString(),
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = if (compacto) 28.dp else 40.dp)
        )
        BotonCirculo(Icons.Default.Add, "Aumentar cantidad", onMas, tamano)
    }
}

@Composable
private fun BotonCirculo(icono: ImageVector, descripcion: String, onClick: () -> Unit, tamano: Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(WarmTan.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = BrownDark,
            modifier = Modifier.size(tamano * 0.55f)
        )
    }
}
