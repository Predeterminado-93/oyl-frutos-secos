package com.OYL.frutossecos.data.repository

import androidx.room.withTransaction
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity

const val METODO_RETIRO = "Retiro en tienda"
const val METODO_DESPACHO = "Despacho a domicilio"
const val COSTO_DESPACHO = 2000.0

/**
 * Repositorio de pedidos.
 * Confirma un pedido en UNA transacción de Room: valida stock, descuenta stock,
 * crea el pedido y vacía el carrito. Si algo falla, no se guarda nada (evita sobreventas).
 */
class PedidoRepository(private val db: AppDatabase) {

    private class PedidoInvalidoException(mensaje: String) : Exception(mensaje)

    suspend fun confirmarPedido(
        usuario: UsuarioEntity,
        items: List<CarritoItemEntity>,
        metodoEntrega: String,
        destino: String,
        telefono: String
    ): Result<PedidoEntity> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Tu carrito está vacío."))
        }

        return try {
            val pedido = db.withTransaction {
                val productoDao = db.productoDao()
                var subtotal = 0.0

                for (item in items) {
                    val producto = productoDao.obtenerPorId(item.productoId)
                    if (producto == null || !producto.activo) {
                        throw PedidoInvalidoException("${item.productoNombre} ya no está disponible. Quítalo del carrito.")
                    }
                    if (producto.stock < item.cantidad) {
                        throw PedidoInvalidoException(
                            "Stock insuficiente de ${producto.nombre}: hay ${producto.stock} un. y pediste ${item.cantidad}."
                        )
                    }
                    // Se usa el precio actual del producto (fuente oficial), no el guardado en el carrito
                    subtotal += producto.precio * item.cantidad
                    productoDao.actualizarStock(producto.id, producto.stock - item.cantidad)
                }

                val despacho = if (metodoEntrega == METODO_DESPACHO) COSTO_DESPACHO else 0.0
                val nuevo = PedidoEntity(
                    usuarioId = usuario.id,
                    usuarioAlias = usuario.alias,
                    estado = "PENDIENTE",
                    total = subtotal + despacho,
                    itemsResumen = items.joinToString(", ") { "${it.cantidad}x ${it.productoNombre}" },
                    puntoRetiroNombre = destino,
                    metodoEntrega = metodoEntrega,
                    telefonoContacto = telefono
                )
                val idGenerado = db.pedidoDao().insertar(nuevo).toInt()
                db.carritoDao().vaciarCarrito(usuario.id)
                nuevo.copy(id = idGenerado)
            }
            Result.success(pedido)
        } catch (e: PedidoInvalidoException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo confirmar el pedido. Inténtalo nuevamente."))
        }
    }
}
