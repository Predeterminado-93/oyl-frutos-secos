
package com.OYL.frutossecos.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import com.OYL.frutossecos.data.remote.ApiClient
import com.OYL.frutossecos.data.remote.dto.RemoteDetalleVentaRequest
import com.OYL.frutossecos.data.remote.dto.RemoteVentaRequest

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
 * Confirma un pedido en UNA transacción de Room (valida stock, descuenta, crea pedido,
 * vacía carrito). Además intenta registrar la venta en el backend Spring Boot.
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
                        ?: throw PedidoInvalidoException("${item.productoNombre} ya no está disponible. Quítalo del carrito.")

                    if (!producto.activo) throw PedidoInvalidoException("${item.productoNombre} ya no está disponible.")

                    if (producto.stock < item.cantidad) {
                        throw PedidoInvalidoException(
                            "Stock insuficiente de ${producto.nombre}: hay ${producto.stock} un. y pediste ${item.cantidad}."
                        )
                    }

                    val stockNuevo = producto.stock - item.cantidad
                    if (producto.stock > producto.stockMinimo && stockNuevo <= producto.stockMinimo) {
                        productosConStockBajo.add(
                            ProductoConStockBajo(producto.nombre, stockNuevo, producto.stockMinimo)
                        )
                    }

                    subtotal += producto.precio * item.cantidad
                    productoDao.actualizarStock(producto.id, stockNuevo)
                }

                val despacho = if (metodoEntrega == METODO_DESPACHO) COSTO_DESPACHO else 0.0

                // Mapea método de entrega al enum del backend
                val metodoPagoBackend = if (metodoEntrega == METODO_DESPACHO) "TRANSFERENCIA" else "EFECTIVO"

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

                Log.i(
                    "PedidoRepository",
                    "[OYL-COMPRA] NUEVA COMPRA REALIZADA: Pedido #$idGenerado | Cliente: '${usuario.nombre}' (${usuario.alias}) | Total: $$${nuevo.total} | Método: $metodoEntrega | Destino: '$destino' | Items (${items.size}): ${nuevo.itemsResumen}"
                )

                // --- Envío al backend (best-effort: no falla si no hay conexión) ---
                try {
                    val request = RemoteVentaRequest(
                        usuarioId = usuario.id.toLong(),
                        metodoPago = metodoPagoBackend,
                        direccionEntrega = destino,
                        detalles = items.map { item ->
                            RemoteDetalleVentaRequest(
                                productoId = item.productoId.toLong(),
                                nombreProducto = item.productoNombre,
                                precioUnitario = item.productoPrecio,
                                cantidad = item.cantidad
                            )
                        }
                    )
                    val response = ApiClient.orderService.crearVenta(request)
                    if (response.isSuccessful) {
                        Log.i("PedidoRepository", "Venta registrada en backend: id=${response.body()?.id}")
                    } else {
                        Log.w("PedidoRepository", "Backend rechazó la venta (${response.code()}). Guardado solo en Room.")
                    }
                } catch (e: Exception) {
                    Log.w("PedidoRepository", "Sin conexión al backend, pedido guardado solo en Room: ${e.message}")
                }

                PedidoConfirmacionResultado(
                    pedido = nuevo.copy(id = idGenerado),
                    productosConStockBajo = productosConStockBajo
                )
            }

            Result.success(resultado)
        } catch (e: PedidoInvalidoException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo confirmar el pedido. Inténtalo nuevamente."))
        }
    }
}
