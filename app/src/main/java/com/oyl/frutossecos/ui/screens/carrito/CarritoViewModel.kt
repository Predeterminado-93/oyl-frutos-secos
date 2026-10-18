package com.OYL.frutossecos.ui.screens.carrito

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.repository.CarritoRepository
import com.OYL.frutossecos.data.repository.UsuarioRepository
import com.OYL.frutossecos.ui.util.AvisoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarritoUiState(
    val items: List<CarritoItemEntity> = emptyList(),
    val isLoading: Boolean = true,
    val sinSesion: Boolean = false,
    val aviso: AvisoUi? = null
) {
    val subtotal: Double get() = items.sumOf { it.productoPrecio * it.cantidad }
}

class CarritoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val carritoRepository = CarritoRepository(db.carritoDao(), db.productoDao())

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActivo()
            if (usuario == null) {
                _uiState.update { it.copy(isLoading = false, sinSesion = true) }
                return@launch
            }
            carritoRepository.itemsPorUsuario(usuario.id).collect { lista ->
                _uiState.update { it.copy(items = lista, isLoading = false) }
            }
        }
    }

    fun aumentar(item: CarritoItemEntity) {
        viewModelScope.launch {
            carritoRepository.cambiarCantidad(item, item.cantidad + 1)
                .onFailure { avisar(it.message ?: "No se pudo cambiar la cantidad.") }
        }
    }

    fun disminuir(item: CarritoItemEntity) {
        if (item.cantidad <= 1) return
        viewModelScope.launch {
            carritoRepository.cambiarCantidad(item, item.cantidad - 1)
                .onFailure { avisar(it.message ?: "No se pudo cambiar la cantidad.") }
        }
    }

    fun eliminar(item: CarritoItemEntity) {
        viewModelScope.launch {
            carritoRepository.eliminar(item)
            avisar("${item.productoNombre} eliminado del carrito")
        }
    }

    /** Vuelve a validar disponibilidad antes de pasar a confirmar. Si hay problema, avisa cuál producto corregir. */
    fun continuarCompra(onValido: () -> Unit) {
        viewModelScope.launch {
            val problema = carritoRepository.validarDisponibilidad(_uiState.value.items)
            if (problema != null) avisar(problema) else onValido()
        }
    }

    fun avisoMostrado() = _uiState.update { it.copy(aviso = null) }

    private fun avisar(texto: String) {
        _uiState.update { it.copy(aviso = AvisoUi(texto)) }
    }
}
