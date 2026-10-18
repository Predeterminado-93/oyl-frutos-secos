package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un artículo en el carrito de compras.
 * Cada cliente tiene su propio carrito identificado por [usuarioId].
 * Se conserva en SQLite para persistir entre sesiones.
 */
@Entity(tableName = "carrito_items")
data class CarritoItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val productoId: Int,
    val productoNombre: String,
    val productoFotoUrl: String,
    val productoPrecio: Double,
    val productoCategoria: String,
    val productoPeso: String = "",
    val cantidad: Int = 1,
    val agregadoEn: Long = System.currentTimeMillis()
)
