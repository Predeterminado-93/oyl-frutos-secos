package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una tienda física o punto de retiro habilitado.
 */
@Entity(tableName = "puntos_retiro")
data class PuntoRetiroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val tipo: String,             // "TIENDA_FISICA" | "PUNTO_RETIRO"
    val direccion: String,
    val comuna: String,
    val latitud: Double,
    val longitud: Double,
    val horario: String,
    val telefono: String,
    val indicaciones: String,
    val esTiendaPrincipal: Boolean = false
)
