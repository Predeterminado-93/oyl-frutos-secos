package com.OYL.frutossecos.data.repository

import com.OYL.frutossecos.data.local.dao.CarritoDao
import com.OYL.frutossecos.data.local.dao.ProductoDao
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio del carrito. Cada cliente tiene su propio carrito en SQLite (por usuarioId).
 * Valida stock antes de agregar o cambiar cantidades.
 */
class CarritoRepository(
    private val carritoDao: CarritoDao,
    private val productoDao: ProductoDao
) {

    fun itemsPorUsuario(usuarioId: Int): Flow<List<CarritoItemEntity>> =
        carritoDao.itemsPorUsuario(usuarioId)

    suspend fun agregar(usuarioId: Int, producto: ProductoEntity, cantidad: Int = 1): Result<Unit> {
        if (cantidad < 1) {
            return Result.failure(IllegalArgumentException("La cantidad debe ser al menos 1."))
        }
        if (producto.stock <= 0) {
            return Result.failure(IllegalArgumentException("${producto.nombre} no tiene stock disponible."))
        }

        val existente = carritoDao.obtenerItem(usuarioId, producto.id)
        val nuevaCantidad = (existente?.cantidad ?: 0) + cantidad
        if (nuevaCantidad > producto.stock) {
            return Result.failure(
                IllegalArgumentException("Stock insuficiente de ${producto.nombre}. Disponible: ${producto.stock} un.")
            )
        }

        if (existente != null) {
            carritoDao.actualizarCantidad(existente.id, nuevaCantidad)
        } else {
            carritoDao.insertar(
                CarritoItemEntity(
                    usuarioId = usuarioId,
                    productoId = producto.id,
                    productoNombre = producto.nombre,
                    productoFotoUrl = producto.fotoUrl,
                    productoPrecio = producto.precio,
                    productoCategoria = producto.categoria,
                    productoPeso = producto.peso,
                    cantidad = cantidad
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun cambiarCantidad(item: CarritoItemEntity, nuevaCantidad: Int): Result<Unit> {
        if (nuevaCantidad < 1) {
            return Result.failure(IllegalArgumentException("Usa el botón eliminar para quitar el producto."))
        }
        val producto = productoDao.obtenerPorId(item.productoId)
        if (producto == null || !producto.activo) {
            return Result.failure(IllegalArgumentException("${item.productoNombre} ya no está disponible."))
        }
        if (nuevaCantidad > producto.stock) {
            return Result.failure(
                IllegalArgumentException("Stock insuficiente de ${producto.nombre}. Disponible: ${producto.stock} un.")
            )
        }
        carritoDao.actualizarCantidad(item.id, nuevaCantidad)
        return Result.success(Unit)
    }

    suspend fun eliminar(item: CarritoItemEntity) {
        carritoDao.eliminar(item)
    }

    /**
     * Revisa que cada producto siga disponible y con stock suficiente.
     * Devuelve el mensaje del primer problema, o null si todo está bien.
     * TODO: cuando exista el backend Spring Boot, consultar la disponibilidad oficial aquí.
     */
    suspend fun validarDisponibilidad(items: List<CarritoItemEntity>): String? {
        for (item in items) {
            val producto = productoDao.obtenerPorId(item.productoId)
            if (producto == null || !producto.activo) {
                return "${item.productoNombre} ya no está disponible. Quítalo del carrito."
            }
            if (producto.stock < item.cantidad) {
                return "Stock insuficiente de ${producto.nombre}: hay ${producto.stock} un. y tienes ${item.cantidad} en el carrito."
            }
        }
        return null
    }
}
