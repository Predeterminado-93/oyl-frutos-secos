
package com.OYL.frutossecos.ui.screens.gestioninventario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.MovimientoInventarioEntity
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestionInventarioUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val movimientos: List<MovimientoInventarioEntity> = emptyList(),
    val productoSeleccionado: ProductoEntity? = null,
    val tipoMovimiento: String = "ENTRADA",
    val cantidad: String = "",
    val observacion: String = "",
    val busqueda: String = "",
    val error: String? = null,
    val mensaje: String? = null,
    val guardando: Boolean = false
)

class GestionInventarioViewModel(application: Application) :
    AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoInventarioDao()
    private val notificacionDao = db.notificacionDao()
    private val usuarioDao = db.usuarioDao()

    private val _uiState = MutableStateFlow(GestionInventarioUiState())
    val uiState: StateFlow<GestionInventarioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productoDao.todos().collect { productos ->
                _uiState.update { it.copy(productos = productos) }
            }
        }
        viewModelScope.launch {
            movimientoDao.todos().collect { movimientos ->
                _uiState.update { it.copy(movimientos = movimientos) }
            }
        }
    }

    fun cambiarBusqueda(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }

    fun seleccionarProducto(producto: ProductoEntity) {
        _uiState.update {
            it.copy(
                productoSeleccionado = producto,
                cantidad = "",
                error = null,
                mensaje = null
            )
        }
    }

    fun cambiarTipo(tipo: String) {
        if (tipo != "ENTRADA" && tipo != "SALIDA") return
        _uiState.update { it.copy(tipoMovimiento = tipo, error = null) }
    }

    fun cambiarCantidad(valor: String) {
        if (valor.isEmpty() || valor.all { it.isDigit() }) {
            _uiState.update { it.copy(cantidad = valor, error = null) }
        }
    }

    fun cambiarObservacion(valor: String) {
        _uiState.update { it.copy(observacion = valor) }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }

    fun registrarMovimiento() {
        val estado = _uiState.value
        val producto = estado.productoSeleccionado
        val cantidad = estado.cantidad.toIntOrNull()

        if (producto == null) {
            _uiState.update { it.copy(error = "Selecciona un producto.") }
            return
        }

        if (cantidad == null || cantidad <= 0) {
            _uiState.update { it.copy(error = "Ingresa una cantidad mayor que cero.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(guardando = true, error = null, mensaje = null)
            }

            try {
                db.withTransaction {
                    val productoActual = productoDao.obtenerPorId(producto.id)
                        ?: throw IllegalStateException("El producto ya no existe.")

                    val stockAnterior = productoActual.stock
                    val stockActualizado = if (estado.tipoMovimiento == "ENTRADA") {
                        stockAnterior.toLong() + cantidad
                    } else {
                        stockAnterior.toLong() - cantidad
                    }

                    if (stockActualizado < 0) {
                        throw IllegalStateException("No hay stock suficiente para esa salida.")
                    }

                    if (stockActualizado > Int.MAX_VALUE) {
                        throw IllegalStateException("La cantidad excede el límite permitido.")
                    }

                    val stockNuevo = stockActualizado.toInt()

                    productoDao.actualizarStock(productoActual.id, stockNuevo)

                    movimientoDao.insertar(
                        MovimientoInventarioEntity(
                            productoId = productoActual.id,
                            nombreProducto = productoActual.nombre,
                            tipo = estado.tipoMovimiento,
                            cantidad = cantidad,
                            stockAnterior = stockAnterior,
                            stockNuevo = stockNuevo,
                            observacion = estado.observacion.trim()
                        )
                    )

                    if (
                        stockAnterior > productoActual.stockMinimo &&
                        stockNuevo <= productoActual.stockMinimo
                    ) {
                        val administrador = usuarioDao.obtenerPorAlias("admin")

                        if (administrador != null) {
                            notificacionDao.insertar(
                                NotificacionEntity(
                                    usuarioId = administrador.id,
                                    titulo = "Stock bajo",
                                    mensaje = "El producto ${productoActual.nombre} tiene $stockNuevo unidades. El mínimo configurado es ${productoActual.stockMinimo}.",
                                    tipo = "STOCK",
                                    fecha = System.currentTimeMillis(),
                                    leida = false,
                                    rutaDestino = "gestion_inventario"
                                )
                            )
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        guardando = false,
                        cantidad = "",
                        observacion = "",
                        mensaje = "Movimiento registrado correctamente."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = e.message ?: "No se pudo registrar el movimiento."
                    )
                }
            }
        }
    }
}
