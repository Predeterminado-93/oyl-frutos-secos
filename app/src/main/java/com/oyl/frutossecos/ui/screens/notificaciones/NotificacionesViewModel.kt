
package com.OYL.frutossecos.ui.screens.notificaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import com.OYL.frutossecos.data.repository.NotificacionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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
    private var observacionNotificaciones: Job? = null
    private var observacionNoLeidas: Job? = null

    init {
        observarUsuarioActivo()
    }

    private fun observarUsuarioActivo() {
        viewModelScope.launch {
            db.usuarioDao().usuarioActivo().collect { usuario ->
                if (usuario?.id != usuarioIdActual) {
                    observacionNotificaciones?.cancel()
                    observacionNoLeidas?.cancel()
                    usuarioIdActual = usuario?.id

                    if (usuario == null) {
                        _uiState.value = NotificacionesUiState(isLoading = false)
                    } else {
                        _uiState.value = NotificacionesUiState()

                        observarNotificaciones(usuario.id)
                        observarNoLeidas(usuario.id)
                    }
                }
            }
        }
    }

    private fun observarNotificaciones(usuarioId: Int) {
        observacionNotificaciones = viewModelScope.launch {
            repository.obtenerNotificaciones(usuarioId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudieron cargar las notificaciones."
                        )
                    }
                }
                .collect { lista ->
                    _uiState.update {
                        it.copy(
                            notificaciones = lista,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun observarNoLeidas(usuarioId: Int) {
        observacionNoLeidas = viewModelScope.launch {
            repository.contarNoLeidas(usuarioId)
                .catch {
                    _uiState.update {
                        it.copy(error = "No se pudo actualizar el contador de notificaciones.")
                    }
                }
                .collect { cantidad ->
                    _uiState.update { it.copy(noLeidas = cantidad) }
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
        _uiState.update { it.copy(error = null) }
    }
}
