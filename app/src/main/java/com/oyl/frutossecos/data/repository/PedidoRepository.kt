
package com.OYL.frutossecos.data.repository

import androidx.room.withTransaction
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity

const val METODO_RETIRO = "Retiro en tienda"
const val METODO_DESPACHO = "Despacho a domicilio"
const val COSTO_DESPACHO = 2000.0

data class ProductoConStockBajo(
    val nombre: String,
    val stockNuevo: Int,
    val stockMinimo: Int
)

data class PedidoConfirmacionResultado(
    val pedido: PedidoEntity,
    val productosConStockBajo: List<ProductoConStockBajo>
)

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
    ): Result<PedidoConfirmacionResultado> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Tu carrito está vacío."))
        }

        return try {
            val resultado = db.withTransaction {
                val productoDao = db.productoDao()
                val productosConStockBajo = mutableListOf<ProductoConStockBajo>()
                var subtotal = 0.0

                for (item in items) {
                    val producto = productoDao.obtenerPorId(item.productoId)

                    if (producto == null || !producto.activo) {
                        throw PedidoInvalidoException(
                            "${item.productoNombre} ya no está disponible. Quítalo del carrito."
                        )
                    }

                    if (producto.stock < item.cantidad) {
                        throw PedidoInvalidoException(
                            "Stock insuficiente de ${producto.nombre}: hay ${producto.stock} un. y pediste ${item.cantidad}."
                        )
                    }

                    val stockNuevo = producto.stock - item.cantidad

                    if (
                        producto.stock > producto.stockMinimo &&
                        stockNuevo <= producto.stockMinimo
                    ) {
                        productosConStockBajo.add(
                            ProductoConStockBajo(
                                nombre = producto.nombre,
                                stockNuevo = stockNuevo,
                                stockMinimo = producto.stockMinimo
                            )
                        )
                    }

                    // Se usa el precio actual del producto (fuente oficial), no el guardado en el carrito.
                    subtotal += producto.precio * item.cantidad
                    productoDao.actualizarStock(producto.id, stockNuevo)
                }

                val despacho = if (metodoEntrega == METODO_DESPACHO) {
                    COSTO_DESPACHO
                } else {
                    0.0
                }

                val nuevo = PedidoEntity(
                    usuarioId = usuario.id,
                    usuarioAlias = usuario.alias,
                    estado = "PENDIENTE",
                    total = subtotal + despacho,
                    itemsResumen = items.joinToString(", ") {
                        "${it.cantidad}x ${it.productoNombre}"
                    },
                    puntoRetiroNombre = destino,
                    metodoEntrega = metodoEntrega,
                    telefonoContacto = telefono
                )

                val idGenerado = db.pedidoDao().insertar(nuevo).toInt()
                db.carritoDao().vaciarCarrito(usuario.id)

                PedidoConfirmacionResultado(
                    pedido = nuevo.copy(id = idGenerado),
                    productosConStockBajo = productosConStockBajo
                )
            }

            Result.success(resultado)
        } catch (e: PedidoInvalidoException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(
                Exception("No se pudo confirmar el pedido. Inténtalo nuevamente.")
            )
        }
    }
}
