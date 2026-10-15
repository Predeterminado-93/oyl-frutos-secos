package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un usuario de la aplicación.
 * Rol puede ser: "ADMIN" o "CLIENTE"
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,        // En producción usar hash; aquí es demo
    val rol: String,             // "ADMIN" | "CLIENTE"
    val sesionActiva: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)
