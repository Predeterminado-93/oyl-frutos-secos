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
    val fotoUrl: String,         // URL remota o URI local de imagen
    val precio: Double,
    val stock: Int,
    val categoria: String,       // Ej: "Frutos secos", "Semillas", "Deshidratados"
    val peso: String = "250 gramos",  // Peso o presentación
    val origen: String = "Nacional",  // Origen del producto
    val activo: Boolean = true,
    val actualizadoEn: Long = System.currentTimeMillis()
)
