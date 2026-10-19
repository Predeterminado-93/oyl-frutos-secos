
package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos_inventario")
data class MovimientoInventarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productoId: Int,
    val nombreProducto: String,
    val tipo: String,
    val cantidad: Int,
    val stockAnterior: Int,
    val stockNuevo: Int,
    val fecha: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false,
    val observacion: String = ""
)
