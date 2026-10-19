package com.OYL.frutossecos.ui.screens.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.PedidoEntity
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
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "Inicia sesión para consultar los pedidos."
                        )
                    }
                    return@launch
                }

                val administrador = usuario.rol == "ADMIN"

                android.util.Log.d(
                    "PedidosDebug",
                    "Usuario activo: id=${usuario.id}, alias=${usuario.alias}, rol=${usuario.rol}"
                )

                _uiState.update {
                    it.copy(esAdministrador = administrador)
                }

                val flujoPedidos = if (administrador) {
                    pedidoDao.todosLosPedidos()
                } else {
                    pedidoDao.pedidosPorUsuario(usuario.id)
                }

                flujoPedidos.collect { pedidos ->
                    android.util.Log.d(
                        "PedidosDebug",
                        "Pedidos recibidos: ${pedidos.joinToString { "id=${it.id}, usuarioId=${it.usuarioId}, alias=${it.usuarioAlias}" }}"
                    )
                    _uiState.update {
                        it.copy(
                            pedidos = pedidos,
                            cargando = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar los pedidos."
                    )
                }
            }
        }
    }

    fun cambiarFiltro(estado: String) {
        _uiState.update {
            it.copy(estadoFiltro = estado)
        }
    }

    fun actualizarEstado(pedidoId: Int, nuevoEstado: String) {
        if (!_uiState.value.esAdministrador || _uiState.value.guardando) return

        val estadosPermitidos = listOf(
            "PENDIENTE",
            "EN_PREPARACION",
            "LISTO_RETIRO",
            "ENTREGADO"
        )

        if (nuevoEstado !in estadosPermitidos) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(guardando = true, error = null, mensaje = null)
            }

            try {
                val filasActualizadas = pedidoDao.actualizarEstado(
                    pedidoId = pedidoId,
                    nuevoEstado = nuevoEstado
                )

                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = if (filasActualizadas > 0) {
                            "Estado del pedido actualizado."
                        } else {
                            null
                        },
                        error = if (filasActualizadas == 0) {
                            "No se encontró el pedido seleccionado."
                        } else {
                            null
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No se pudo actualizar el estado del pedido."
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(error = null, mensaje = null)
        }
    }
}