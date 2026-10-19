
package com.OYL.frutossecos.ui.screens.confirmacion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.PuntoRetiroEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import com.OYL.frutossecos.data.repository.COSTO_DESPACHO
import com.OYL.frutossecos.data.repository.CarritoRepository
import com.OYL.frutossecos.data.repository.METODO_DESPACHO
import com.OYL.frutossecos.data.repository.METODO_RETIRO
import com.OYL.frutossecos.data.repository.NotificacionRepository
import com.OYL.frutossecos.data.repository.PedidoRepository
import com.OYL.frutossecos.data.repository.UsuarioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmacionUiState(
    val items: List<CarritoItemEntity> = emptyList(),
    val metodoEntrega: String = METODO_RETIRO,
    val puntos: List<PuntoRetiroEntity> = emptyList(),
    val puntoSeleccionado: PuntoRetiroEntity? = null,
    val telefono: String = "",
    val isLoading: Boolean = true,
    val isEnviando: Boolean = false,
    val errorMessage: String? = null,
    val pedidoConfirmado: PedidoEntity? = null
) {
    val subtotal: Double get() = items.sumOf { it.productoPrecio * it.cantidad }
    val costoDespacho: Double get() = if (metodoEntrega == METODO_DESPACHO) COSTO_DESPACHO else 0.0
    val total: Double get() = subtotal + costoDespacho
}

class ConfirmacionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val carritoRepository = CarritoRepository(db.carritoDao(), db.productoDao())
    private val pedidoRepository = PedidoRepository(db)
    private val notificacionRepository = NotificacionRepository(db.notificacionDao())

    private var usuarioActual: UsuarioEntity? = null

    private val _uiState = MutableStateFlow(ConfirmacionUiState())
    val uiState: StateFlow<ConfirmacionUiState> = _uiState.asStateFlow()

    init {
        cargarPuntos()
        cargarCarrito()
    }

    private fun cargarPuntos() {
        viewModelScope.launch {
            db.puntoRetiroDao().todosLosPuntos().collect { lista ->
                _uiState.update { s ->
                    s.copy(
                        puntos = lista,
                        puntoSeleccionado = s.puntoSeleccionado
                            ?: lista.firstOrNull { it.esTiendaPrincipal }
                            ?: lista.firstOrNull()
                    )
                }
            }
        }
    }

    private fun cargarCarrito() {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActivo()
            if (usuario == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Inicia sesión para confirmar tu pedido.") }
                return@launch
            }
            usuarioActual = usuario
            _uiState.update { if (it.telefono.isBlank()) it.copy(telefono = usuario.telefono) else it }
            carritoRepository.itemsPorUsuario(usuario.id).collect { lista ->
                _uiState.update { it.copy(items = lista, isLoading = false) }
            }
        }
    }

    fun seleccionarMetodo(metodo: String) =
        _uiState.update { it.copy(metodoEntrega = metodo, errorMessage = null) }

    fun seleccionarPunto(punto: PuntoRetiroEntity) =
        _uiState.update { it.copy(puntoSeleccionado = punto, errorMessage = null) }

    fun onTelefonoChanged(valor: String) =
        _uiState.update { it.copy(telefono = valor, errorMessage = null) }

    fun confirmar() {
        val state = _uiState.value
        val usuario = usuarioActual
        if (state.isEnviando) return

        if (usuario == null) {
            mostrarError("Inicia sesión para confirmar tu pedido.")
            return
        }
        if (state.items.isEmpty()) {
            mostrarError("Tu carrito está vacío.")
            return
        }
        if (state.telefono.isBlank()) {
            mostrarError("Ingresa un teléfono de contacto.")
            return
        }

        val destino: String
        if (state.metodoEntrega == METODO_DESPACHO) {
            destino = "Despacho a ${usuario.direccion}"
        } else {
            val punto = state.puntoSeleccionado
            if (punto == null) {
                mostrarError("Selecciona un punto de retiro.")
                return
            }
            destino = punto.nombre
        }

        _uiState.update { it.copy(isEnviando = true, errorMessage = null) }
        viewModelScope.launch {
            // TODO: aquí se enviará el pedido al backend Spring Boot (Retrofit). Por ahora se guarda en SQLite.
            delay(800)
            pedidoRepository.confirmarPedido(
                usuario = usuario,
                items = state.items,
                metodoEntrega = state.metodoEntrega,
                destino = destino,
                telefono = state.telefono.trim()
            ).fold(
                onSuccess = { pedido ->
                    try {
                        notificacionRepository.insertar(
                            NotificacionEntity(
                                usuarioId = usuario.id,
                                titulo = "Pedido recibido",
                                mensaje = "Tu pedido N° ${pedido.id} fue recibido y está pendiente de validación.",
                                tipo = "PEDIDO"
                            )
                        )
                    } catch (e: Exception) {
                        // La notificación no debe impedir mostrar un pedido guardado correctamente.
                    }

                    _uiState.update {
                        it.copy(isEnviando = false, pedidoConfirmado = pedido)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isEnviando = false,
                            errorMessage = error.message ?: "No se pudo confirmar el pedido."
                        )
                    }
                }
            )
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(errorMessage = mensaje) }
    }
}
