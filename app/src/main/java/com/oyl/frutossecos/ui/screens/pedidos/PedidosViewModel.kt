
package com.OYL.frutossecos.ui.screens.pedidos

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.remote.ApiClient
import com.OYL.frutossecos.data.repository.NotificacionRepository
import com.OYL.frutossecos.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PedidosUiState(
    val pedidos: List<PedidoEntity> = emptyList(),
    val estadoFiltro: String = "TODOS",
    val esAdministrador: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class PedidosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val pedidoDao = db.pedidoDao()
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val notificacionRepository = NotificacionRepository(db.notificacionDao())

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    init {
        cargarPedidos()
    }

    private fun cargarPedidos() {
        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.obtenerUsuarioActivo()
                if (usuario == null) {
                    _uiState.update { it.copy(cargando = false, error = "Inicia sesión para consultar los pedidos.") }
                    return@launch
                }

                val administrador = usuario.rol == "ADMIN"
                _uiState.update { it.copy(esAdministrador = administrador) }

                // --- Intento sincronizar pedidos desde el backend ---
                try {
                    val response = ApiClient.orderService.listarVentasPorUsuario(usuario.id.toLong())
                    if (response.isSuccessful) {
                        val remotos = response.body() ?: emptyList()
                        val entidades = remotos.map { v ->
                            PedidoEntity(
                                id = v.id.toInt(),
                                usuarioId = usuario.id,
                                usuarioAlias = usuario.alias,
                                fecha = System.currentTimeMillis(),
                                estado = v.estado,
                                total = v.total,
                                itemsResumen = v.detalles.joinToString(", ") { "${it.cantidad}x ${it.nombreProducto}" },
                                puntoRetiroNombre = v.direccionEntrega ?: "Sin dirección",
                                metodoEntrega = v.metodoPago
                            )
                        }
                        // Inserta/actualiza en Room (usa REPLACE por ID)
                        entidades.forEach { pedidoDao.insertar(it) }
                        Log.i("PedidosViewModel", "Sincronizados ${entidades.size} pedidos desde backend.")
                    }
                } catch (e: Exception) {
                    Log.w("PedidosViewModel", "Sin conexión, mostrando pedidos locales: ${e.message}")
                }

                // Observa Room (incluye pedidos sincronizados + locales)
                val flujo = if (administrador) pedidoDao.todosLosPedidos()
                            else pedidoDao.pedidosPorUsuario(usuario.id)

                flujo.collect { pedidos ->
                    _uiState.update { it.copy(pedidos = pedidos, cargando = false, error = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No se pudieron cargar los pedidos.") }
            }
        }
    }

    fun cambiarFiltro(estado: String) {
        _uiState.update { it.copy(estadoFiltro = estado) }
    }

    fun actualizarEstado(pedidoId: Int, nuevoEstado: String) {
        if (!_uiState.value.esAdministrador || _uiState.value.guardando) return

        val estadosPermitidos = listOf("PENDIENTE", "EN_PREPARACION", "LISTO_RETIRO", "ENTREGADO")
        if (nuevoEstado !in estadosPermitidos) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null, mensaje = null) }
            try {
                val pedido = _uiState.value.pedidos.firstOrNull { it.id == pedidoId }
                    ?: run {
                        _uiState.update { it.copy(guardando = false, error = "No se encontró el pedido seleccionado.") }
                        return@launch
                    }

                if (pedido.estado == nuevoEstado) {
                    _uiState.update { it.copy(guardando = false, mensaje = "El pedido ya tiene ese estado.") }
                    return@launch
                }

                // Intenta actualizar en backend
                try {
                    // El backend usa estados ligeramente distintos — mapea LISTO_RETIRO a EN_CAMINO
                    val estadoBackend = if (nuevoEstado == "LISTO_RETIRO") "EN_CAMINO" else nuevoEstado
                    ApiClient.orderService // llamada directa no disponible en este service; se actualiza vía Room
                } catch (_: Exception) {}

                val filasActualizadas = pedidoDao.actualizarEstado(pedidoId, nuevoEstado)
                if (filasActualizadas > 0) {
                    val nombreEstado = when (nuevoEstado) {
                        "PENDIENTE" -> "Pendiente de validación"
                        "EN_PREPARACION" -> "En preparación"
                        "LISTO_RETIRO" -> "Listo para retirar"
                        "ENTREGADO" -> "Entregado"
                        else -> nuevoEstado
                    }
                    notificacionRepository.insertar(
                        NotificacionEntity(
                            usuarioId = pedido.usuarioId,
                            titulo = "Actualización de pedido",
                            mensaje = "Tu pedido N° ${pedido.id} ahora está: $nombreEstado.",
                            tipo = "PEDIDO",
                            rutaDestino = "mis_pedidos"
                        )
                    )
                    _uiState.update { it.copy(guardando = false, mensaje = "Estado del pedido actualizado.") }
                } else {
                    _uiState.update { it.copy(guardando = false, error = "No se encontró el pedido seleccionado.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo actualizar el estado del pedido.") }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }
}
