package com.OYL.frutossecos.ui.screens.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.repository.CarritoRepository
import com.OYL.frutossecos.data.repository.UsuarioRepository
import com.OYL.frutossecos.ui.util.AvisoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUiState(
    val producto: ProductoEntity? = null,
    val cantidad: Int = 1,
    val esAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val aviso: AvisoUi? = null
)

class DetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val carritoRepository = CarritoRepository(db.carritoDao(), db.productoDao())

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    fun cargar(productoId: Int) {
        if (_uiState.value.producto?.id == productoId) return
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActivo()
            val producto = db.productoDao().obtenerPorId(productoId)
            _uiState.update {
                it.copy(
                    producto = producto?.takeIf { p -> p.activo },
                    esAdmin = usuario?.rol == "ADMIN",
                    cantidad = 1,
                    isLoading = false
                )
            }
        }
    }

    fun aumentarCantidad() {
        val producto = _uiState.value.producto ?: return
        if (_uiState.value.cantidad >= producto.stock) {
            avisar("Solo hay ${producto.stock} un. disponibles de ${producto.nombre}.")
            return
        }
        _uiState.update { it.copy(cantidad = it.cantidad + 1) }
    }

    fun disminuirCantidad() {
        if (_uiState.value.cantidad > 1) {
            _uiState.update { it.copy(cantidad = it.cantidad - 1) }
        }
    }

    fun agregarAlCarrito() {
        val producto = _uiState.value.producto ?: return
        val cantidad = _uiState.value.cantidad
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActivo()
            if (usuario == null) {
                avisar("Inicia sesión para agregar productos al carrito.")
                return@launch
            }
            carritoRepository.agregar(usuario.id, producto, cantidad).fold(
                onSuccess = {
                    _uiState.update { it.copy(cantidad = 1) }
                    avisar("$cantidad x ${producto.nombre} agregado al carrito", exito = true)
                },
                onFailure = { avisar(it.message ?: "No se pudo agregar el producto.") }
            )
        }
    }

    fun avisoMostrado() = _uiState.update { it.copy(aviso = null) }

    private fun avisar(texto: String, exito: Boolean = false) {
        _uiState.update { it.copy(aviso = AvisoUi(texto, exito)) }
    }
}
