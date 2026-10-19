package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un pedido de frutos secos.
 * Permite que cada cliente tenga su propio historial sin mezclar datos.
 */
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val usuarioAlias: String,
    val fecha: Long = System.currentTimeMillis(),
    val estado: String,          // "PENDIENTE" | "EN_PREPARACION" | "LISTO_RETIRO" | "ENTREGADO"
    val total: Double,
    val itemsResumen: String,
    val puntoRetiroNombre: String,
    val metodoEntrega: String = "Retiro en tienda",  // "Retiro en tienda" | "Despacho a domicilio"
    val telefonoContacto: String = ""
)
