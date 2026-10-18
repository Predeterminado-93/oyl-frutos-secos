package com.OYL.frutossecos.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeChile: Locale = Locale.forLanguageTag("es-CL")

/** Formatea pesos chilenos: 12470.0 -> "$12.470" */
fun formatearPrecio(valor: Double): String {
    val formato = NumberFormat.getIntegerInstance(localeChile)
    return "$" + formato.format(Math.round(valor))
}

/** Formatea una fecha en milisegundos: "12/09/2026 - 14:20" */
fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy - HH:mm", localeChile).format(Date(millis))

/** Número de pedido con 6 dígitos: 1245 -> "#001245" */
fun formatearNumeroPedido(id: Int): String = "#" + id.toString().padStart(6, '0')

/**
 * Mensaje corto para mostrar en un Snackbar.
 * [id] hace que dos avisos con el mismo texto se muestren dos veces.
 */
data class AvisoUi(
    val texto: String,
    val exito: Boolean = false,
    val id: Long = System.nanoTime()
)
