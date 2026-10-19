
package com.OYL.frutossecos.ui.screens.gestionproductos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestionProductosUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val busqueda: String = "",
    val mostrarFormulario: Boolean = false,
    val productoEditando: ProductoEntity? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoria: String = "Frutos secos",
    val peso: String = "250 gramos",
    val origen: String = "Nacional",
    val activo: Boolean = true,
    val error: String? = null,
    val guardando: Boolean = false
)

class GestionProductosViewModel(application: Application) : AndroidViewModel(application) {

    private val productoDao = AppDatabase.getDatabase(application).productoDao()

    private val _uiState = MutableStateFlow(GestionProductosUiState())
    val uiState: StateFlow<GestionProductosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productoDao.todos().collect { productos ->
                _uiState.update { it.copy(productos = productos) }
            }
        }
    }

    fun cambiarBusqueda(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }

    fun nuevoProducto() {
        _uiState.update {
            GestionProductosUiState(
                productos = it.productos,
                busqueda = it.busqueda,
                mostrarFormulario = true
            )
        }
    }

    fun editarProducto(producto: ProductoEntity) {
        _uiState.update {
            it.copy(
                mostrarFormulario = true,
                productoEditando = producto,
                nombre = producto.nombre,
                descripcion = producto.descripcion,
                fotoUrl = producto.fotoUrl,
                precio = producto.precio.toString(),
                stock = producto.stock.toString(),
                categoria = producto.categoria,
                peso = producto.peso,
                origen = producto.origen,
                activo = producto.activo,
                error = null
            )
        }
    }

    fun cambiarNombre(valor: String) = _uiState.update { it.copy(nombre = valor, error = null) }
    fun cambiarDescripcion(valor: String) = _uiState.update { it.copy(descripcion = valor) }
    fun cambiarFotoUrl(valor: String) = _uiState.update { it.copy(fotoUrl = valor) }
    fun cambiarPrecio(valor: String) = _uiState.update { it.copy(precio = valor, error = null) }
    fun cambiarStock(valor: String) = _uiState.update { it.copy(stock = valor, error = null) }
    fun cambiarCategoria(valor: String) = _uiState.update { it.copy(categoria = valor) }
    fun cambiarPeso(valor: String) = _uiState.update { it.copy(peso = valor) }
    fun cambiarOrigen(valor: String) = _uiState.update { it.copy(origen = valor) }
    fun cambiarActivo(valor: Boolean) = _uiState.update { it.copy(activo = valor) }

    fun cerrarFormulario() {
        _uiState.update {
            GestionProductosUiState(
                productos = it.productos,
                busqueda = it.busqueda
            )
        }
    }

    fun guardarProducto() {
        val estado = _uiState.value
        val nombre = estado.nombre.trim()
        val descripcion = estado.descripcion.trim()
        val precio = estado.precio.replace(",", ".").toDoubleOrNull()
        val stock = estado.stock.toIntOrNull()

        if (nombre.isBlank() || descripcion.isBlank()) {
            _uiState.update { it.copy(error = "Completa el nombre y la descripción.") }
            return
        }

        if (precio == null || precio <= 0) {
            _uiState.update { it.copy(error = "Ingresa un precio mayor que cero.") }
            return
        }

        if (stock == null || stock < 0) {
            _uiState.update { it.copy(error = "El stock debe ser un número igual o mayor que cero.") }
            return
        }

        if (estado.categoria.isBlank() || estado.peso.isBlank()) {
            _uiState.update { it.copy(error = "Completa la categoría y la presentación.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }

            try {
                val productoAnterior = estado.productoEditando

                val producto = ProductoEntity(
                    id = productoAnterior?.id ?: 0,
                    nombre = nombre,
                    descripcion = descripcion,
                    fotoUrl = estado.fotoUrl.trim(),
                    precio = precio,
                    stock = stock,
                    categoria = estado.categoria,
                    peso = estado.peso.trim(),
                    origen = estado.origen.trim(),
                    activo = estado.activo,
                    actualizadoEn = System.currentTimeMillis()
                )

                if (productoAnterior == null) {
                    productoDao.insertar(producto)
                } else {
                    productoDao.actualizar(producto)
                }

                cerrarFormulario()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No se pudo guardar el producto: ${e.message ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    fun cambiarEstado(producto: ProductoEntity) {
        viewModelScope.launch {
            try {
                productoDao.actualizar(
                    producto.copy(
                        activo = !producto.activo,
                        actualizadoEn = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudo cambiar el estado del producto.")
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}
