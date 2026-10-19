
package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones",
    indices = [Index(value = ["usuarioId"])]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val fecha: Long = System.currentTimeMillis(),
    val leida: Boolean = false,
    val rutaDestino: String? = null
)
