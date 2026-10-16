package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un usuario de la aplicación.
 * Rol puede ser: "ADMIN" o "CLIENTE"
 */
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["alias"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val alias: String,
    val email: String,
    val password: String,
    val telefono: String = "",
    val direccion: String = "",
    val rol: String,             // "ADMIN" | "CLIENTE"
    val sesionActiva: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)
