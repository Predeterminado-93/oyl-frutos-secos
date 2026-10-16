package com.OYL.frutossecos.ui.screens.registro

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

data class RegistroUiState(
    val nombre: String = "",
    val alias: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UsuarioRepository(database.usuarioDao())
    }

    fun onNombreChanged(v: String) = _uiState.update { it.copy(nombre = v, errorMessage = null) }
    fun onAliasChanged(v: String) = _uiState.update { it.copy(alias = v, errorMessage = null) }
    fun onPasswordChanged(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onConfirmPasswordChanged(v: String) = _uiState.update { it.copy(confirmPassword = v, errorMessage = null) }
    fun onEmailChanged(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onTelefonoChanged(v: String) = _uiState.update { it.copy(telefono = v, errorMessage = null) }
    fun onDireccionChanged(v: String) = _uiState.update { it.copy(direccion = v, errorMessage = null) }

    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleConfirmPasswordVisibility() = _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }

    fun registrar(onSuccess: (UsuarioEntity) -> Unit) {
        val state = _uiState.value

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.registrarCliente(
                nombre = state.nombre,
                alias = state.alias,
                password = state.password,
                email = state.email,
                telefono = state.telefono,
                direccion = state.direccion
            )

            result.fold(
                onSuccess = { nuevoCliente ->
                    _uiState.update { it.copy(isLoading = false, registroExitoso = true) }
                    onSuccess(nuevoCliente)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al registrar cliente."
                        )
                    }
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
