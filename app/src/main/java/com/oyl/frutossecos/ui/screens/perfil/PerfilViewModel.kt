
package com.OYL.frutossecos.ui.screens.perfil
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import com.OYL.frutossecos.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val usuario: UsuarioEntity? = null,
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UsuarioRepository(database.usuarioDao())
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, mensaje = null)
            }

            val usuario = repository.obtenerUsuarioActivo()

            if (usuario == null) {
                _uiState.update {
                    it.copy(
                        usuario = null,
                        isLoading = false,
                        error = "No hay una sesión activa."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        usuario = usuario,
                        nombre = usuario.nombre,
                        email = usuario.email,
                        telefono = usuario.telefono,
                        direccion = usuario.direccion,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun actualizarNombre(valor: String) {
        _uiState.update { it.copy(nombre = valor, error = null, mensaje = null) }
    }

    fun actualizarEmail(valor: String) {
        _uiState.update { it.copy(email = valor, error = null, mensaje = null) }
    }

    fun actualizarTelefono(valor: String) {
        _uiState.update { it.copy(telefono = valor, error = null, mensaje = null) }
    }

    fun actualizarDireccion(valor: String) {
        _uiState.update { it.copy(direccion = valor, error = null, mensaje = null) }
    }

    fun guardarPerfil() {
        val estado = _uiState.value
        val usuario = estado.usuario

        if (usuario == null) {
            _uiState.update {
                it.copy(error = "No hay un usuario activo.")
            }
            return
        }

        if (estado.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, error = null, mensaje = null)
            }

            repository.actualizarPerfil(
                usuarioId = usuario.id,
                nombre = estado.nombre,
                email = estado.email,
                telefono = estado.telefono,
                direccion = estado.direccion
            ).fold(
                onSuccess = { usuarioActualizado ->
                    _uiState.update {
                        it.copy(
                            usuario = usuarioActualizado,
                            nombre = usuarioActualizado.nombre,
                            email = usuarioActualizado.email,
                            telefono = usuarioActualizado.telefono,
                            direccion = usuarioActualizado.direccion,
                            isSaving = false,
                            mensaje = "Perfil actualizado correctamente.",
                            error = null
                        )
                    }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = excepcion.message ?: "No se pudo actualizar el perfil."
                        )
                    }
                }
            )
        }
    }

    fun cerrarSesion(onCerrarSesion: () -> Unit) {
        viewModelScope.launch {
            repository.cerrarSesion()
            onCerrarSesion()
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(mensaje = null, error = null)
        }
    }
}
