package com.OYL.frutossecos.ui.screens.login

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

data class LoginUiState(
    val alias: String = "",
    val password: String = "",
    val rol: String = "CLIENTE", // "CLIENTE" | "ADMIN"
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val usuarioAutenticado: UsuarioEntity? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UsuarioRepository(database.usuarioDao())
    }

    fun onAliasChanged(nuevoAlias: String) {
        _uiState.update { it.copy(alias = nuevoAlias, errorMessage = null) }
    }

    fun onPasswordChanged(nuevaPassword: String) {
        _uiState.update { it.copy(password = nuevaPassword, errorMessage = null) }
    }

    fun onRolChanged(nuevoRol: String) {
        _uiState.update { it.copy(rol = nuevoRol, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun llenarCredencialesDemoAdmin() {
        _uiState.update {
            it.copy(
                alias = "admin",
                password = "admin123",
                rol = "ADMIN",
                errorMessage = null
            )
        }
    }

    fun llenarCredencialesDemoCliente() {
        _uiState.update {
            it.copy(
                alias = "cliente",
                password = "cliente123",
                rol = "CLIENTE",
                errorMessage = null
            )
        }
    }

    fun iniciarSesion(onSuccess: (UsuarioEntity) -> Unit) {
        val currentState = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.login(
                alias = currentState.alias,
                password = currentState.password,
                rolEsperado = currentState.rol
            )
            result.fold(
                onSuccess = { usuario ->
                    _uiState.update { it.copy(isLoading = false, usuarioAutenticado = usuario) }
                    onSuccess(usuario)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al iniciar sesión."
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
