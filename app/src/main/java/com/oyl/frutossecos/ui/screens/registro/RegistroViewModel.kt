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
    val errorNombre: String? = null,
    val errorAlias: String? = null,
    val errorPassword: String? = null,
    val errorConfirmPassword: String? = null,
    val errorEmail: String? = null,
    val errorTelefono: String? = null,
    val errorDireccion: String? = null,
    val errorMessage: String? = null,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
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

    fun onNombreChanged(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null) }
    fun onAliasChanged(v: String) = _uiState.update { it.copy(alias = v, errorAlias = null) }
    fun onPasswordChanged(v: String) = _uiState.update { it.copy(password = v, errorPassword = null, errorConfirmPassword = null) }
    fun onConfirmPasswordChanged(v: String) = _uiState.update { it.copy(confirmPassword = v, errorConfirmPassword = null) }
    fun onEmailChanged(v: String) = _uiState.update { it.copy(email = v, errorEmail = null) }
    fun onTelefonoChanged(v: String) = _uiState.update { it.copy(telefono = v, errorTelefono = null) }
    fun onDireccionChanged(v: String) = _uiState.update { it.copy(direccion = v, errorDireccion = null) }

    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleConfirmPasswordVisibility() = _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }

    fun registrar(onSuccess: (UsuarioEntity) -> Unit) {
        val state = _uiState.value

        var hayErrores = false
        var errNombre: String? = null
        var errAlias: String? = null
        var errPassword: String? = null
        var errConfirmPassword: String? = null
        var errEmail: String? = null
        var errTelefono: String? = null
        var errDireccion: String? = null

        val cleanNombre = state.nombre.trim()
        val cleanAlias = state.alias.trim()
        val cleanPassword = state.password.trim()
        val cleanConfirmPassword = state.confirmPassword.trim()
        val cleanEmail = state.email.trim()
        val cleanTelefono = state.telefono.trim()
        val cleanDireccion = state.direccion.trim()

        if (cleanNombre.isBlank()) {
            errNombre = "El nombre completo es obligatorio."
            hayErrores = true
        }

        if (cleanAlias.isBlank()) {
            errAlias = "El alias de usuario es obligatorio."
            hayErrores = true
        } else if (cleanAlias.length < 3) {
            errAlias = "El alias debe tener al menos 3 caracteres."
            hayErrores = true
        }

        if (cleanPassword.isBlank()) {
            errPassword = "La contraseña es obligatoria."
            hayErrores = true
        } else if (cleanPassword.length < 4) {
            errPassword = "La contraseña debe tener al menos 4 caracteres."
            hayErrores = true
        }

        if (cleanConfirmPassword.isBlank()) {
            errConfirmPassword = "Debes confirmar tu contraseña."
            hayErrores = true
        } else if (cleanPassword != cleanConfirmPassword) {
            errConfirmPassword = "Las contraseñas no coinciden."
            hayErrores = true
        }

        if (cleanEmail.isBlank()) {
            errEmail = "El correo electrónico es obligatorio."
            hayErrores = true
        } else if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            errEmail = "Ingresa un correo electrónico válido."
            hayErrores = true
        }

        if (cleanTelefono.isBlank()) {
            errTelefono = "El teléfono de contacto es obligatorio."
            hayErrores = true
        }

        if (cleanDireccion.isBlank()) {
            errDireccion = "La dirección de entrega es obligatoria."
            hayErrores = true
        }

        if (hayErrores) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorAlias = errAlias,
                    errorPassword = errPassword,
                    errorConfirmPassword = errConfirmPassword,
                    errorEmail = errEmail,
                    errorTelefono = errTelefono,
                    errorDireccion = errDireccion,
                    errorMessage = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorNombre = null,
                errorAlias = null,
                errorPassword = null,
                errorConfirmPassword = null,
                errorEmail = null,
                errorTelefono = null,
                errorDireccion = null,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = repository.registrarCliente(
                nombre = cleanNombre,
                alias = cleanAlias,
                password = cleanPassword,
                email = cleanEmail,
                telefono = cleanTelefono,
                direccion = cleanDireccion
            )

            result.fold(
                onSuccess = { nuevoCliente ->
                    _uiState.update { it.copy(isLoading = false, registroExitoso = true) }
                    onSuccess(nuevoCliente)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Error al registrar cliente."
                    val lower = msg.lowercase()
                    _uiState.update { current ->
                        when {
                            lower.contains("alias") -> current.copy(isLoading = false, errorAlias = msg)
                            lower.contains("correo") || lower.contains("email") -> current.copy(isLoading = false, errorEmail = msg)
                            lower.contains("nombre") -> current.copy(isLoading = false, errorNombre = msg)
                            lower.contains("contraseña") || lower.contains("password") -> current.copy(isLoading = false, errorPassword = msg)
                            lower.contains("teléfono") || lower.contains("telefono") -> current.copy(isLoading = false, errorTelefono = msg)
                            lower.contains("dirección") || lower.contains("direccion") -> current.copy(isLoading = false, errorDireccion = msg)
                            else -> current.copy(isLoading = false, errorMessage = msg)
                        }
                    }
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                errorNombre = null,
                errorAlias = null,
                errorPassword = null,
                errorConfirmPassword = null,
                errorEmail = null,
                errorTelefono = null,
                errorDireccion = null
            )
        }
    }
}
