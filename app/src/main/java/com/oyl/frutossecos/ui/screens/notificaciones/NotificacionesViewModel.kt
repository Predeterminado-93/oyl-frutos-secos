
package com.OYL.frutossecos.ui.screens.notificaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import com.OYL.frutossecos.data.repository.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificacionesUiState(
    val notificaciones: List<NotificacionEntity> = emptyList(),
    val noLeidas: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = NotificacionRepository(db.notificacionDao())

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    private var usuarioIdActual: Int? = null

    init {
        observarUsuarioActivo()
    }

    private fun observarUsuarioActivo() {
        viewModelScope.launch {
            db.usuarioDao().usuarioActivo().collect { usuario ->
                if (usuario == null) {
                    usuarioIdActual = null
                    _uiState.value = NotificacionesUiState(
                        isLoading = false
                    )
                } else if (usuarioIdActual != usuario.id) {
                    usuarioIdActual = usuario.id
                    cargarNotificaciones(usuario.id)
                }
            }
        }
    }

    private fun cargarNotificaciones(usuarioId: Int) {
        viewModelScope.launch {
            repository.obtenerNotificaciones(usuarioId).collect { lista ->
                _uiState.update {
                    it.copy(
                        notificaciones = lista,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.contarNoLeidas(usuarioId).collect { cantidad ->
                _uiState.update {
                    it.copy(noLeidas = cantidad)
                }
            }
        }
    }

    fun marcarComoLeida(notificacionId: Int) {
        val usuarioId = usuarioIdActual ?: return

        viewModelScope.launch {
            try {
                repository.marcarComoLeida(notificacionId, usuarioId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudo marcar la notificación como leída.")
                }
            }
        }
    }

    fun marcarTodasComoLeidas() {
        val usuarioId = usuarioIdActual ?: return

        viewModelScope.launch {
            try {
                repository.marcarTodasComoLeidas(usuarioId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudieron marcar las notificaciones.")
                }
            }
        }
    }

    fun eliminar(notificacionId: Int) {
        val usuarioId = usuarioIdActual ?: return

        viewModelScope.launch {
            try {
                repository.eliminar(notificacionId, usuarioId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudo eliminar la notificación.")
                }
            }
        }
    }

    fun eliminarTodas() {
        val usuarioId = usuarioIdActual ?: return

        viewModelScope.launch {
            try {
                repository.eliminarTodas(usuarioId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudieron eliminar las notificaciones.")
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update {
            it.copy(error = null)
        }
    }
}
