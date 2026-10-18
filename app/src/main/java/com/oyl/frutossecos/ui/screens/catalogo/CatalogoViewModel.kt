package com.OYL.frutossecos.ui.screens.catalogo



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

data class CatalogoUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val busqueda: String = "",
    val categoriaSeleccionada: String = "Todos",
    val esAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val aviso: AvisoUi? = null
)

class CatalogoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())
    private val carritoRepository = CarritoRepository(db.carritoDao(), db.productoDao())

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        cargarUsuario()
        cargarProductos()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            usuarioRepository.obtenerUsuarioActivoFlow().collect { usuario ->
                _uiState.update { it.copy(esAdmin = usuario?.rol == "ADMIN") }
            }
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            db.productoDao().todosActivos().collect { lista ->
                _uiState.update { it.copy(productos = lista, isLoading = false) }
            }
        }
    }

    fun onBusquedaChanged(texto: String) = _uiState.update { it.copy(busqueda = texto) }

    fun seleccionarCategoria(categoria: String) =
        _uiState.update { it.copy(categoriaSeleccionada = categoria) }

    fun agregarAlCarrito(producto: ProductoEntity) {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActivo()
            if (usuario == null) {
                avisar("Inicia sesión para agregar productos al carrito.")
                return@launch
            }
            carritoRepository.agregar(usuario.id, producto, 1).fold(
                onSuccess = { avisar("${producto.nombre} agregado al carrito", exito = true) },
                onFailure = { avisar(it.message ?: "No se pudo agregar el producto.") }
            )
        }
    }

    fun avisoMostrado() = _uiState.update { it.copy(aviso = null) }

    private fun avisar(texto: String, exito: Boolean = false) {
        _uiState.update { it.copy(aviso = AvisoUi(texto, exito)) }
    }
}
