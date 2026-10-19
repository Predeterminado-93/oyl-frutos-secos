
package com.OYL.frutossecos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un producto del catálogo.
 * Se usa como caché local; la fuente oficial es el backend Spring Boot.
 *
 * fotoUrl: puede ser una URL remota (http://...) o un path local de la cámara
 *          (content://... o file://...). Coil se encarga de resolverla.
 */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val fotoUrl: String,
    val precio: Double,
    val stock: Int,
    val categoria: String,
    val peso: String = "250 gramos",
    val origen: String = "Nacional",
    val activo: Boolean = true,
    val actualizadoEn: Long = System.currentTimeMillis(),
    val stockMinimo: Int = 5
)
