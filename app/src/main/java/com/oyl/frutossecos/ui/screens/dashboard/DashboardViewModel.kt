package com.OYL.frutossecos.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val usuario: UsuarioEntity? = null,
    val esAdmin: Boolean = false,
    val productosDestacados: List<ProductoEntity> = emptyList(),
    val productosStockBajo: List<ProductoEntity> = emptyList(),
    val pedidosRecientes: List<PedidoEntity> = emptyList(),
    val pedidosPendientesAdmin: List<PedidoEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            // Cargar usuario activo de SQLite
            db.usuarioDao().usuarioActivo().collect { user ->
                val esAdmin = user?.rol == "ADMIN"
                _uiState.update { it.copy(usuario = user, esAdmin = esAdmin) }

                if (user != null) {
                    observarProductos()
                    observarPedidos(user.id, esAdmin)
                }
            }
        }
    }

    private fun observarProductos() {
        viewModelScope.launch {
            db.productoDao().todosActivos().collect { lista ->
                val destacados = lista.take(5)
                val stockBajo = lista.filter { it.stock <= 5 }
                _uiState.update {
                    it.copy(
                        productosDestacados = destacados,
                        productosStockBajo = stockBajo,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observarPedidos(usuarioId: Int, esAdmin: Boolean) {
        viewModelScope.launch {
            if (esAdmin) {
                db.pedidoDao().pedidosPendientes().collect { pendientes ->
                    _uiState.update { it.copy(pedidosPendientesAdmin = pendientes) }
                }
            } else {
                db.pedidoDao().pedidosPorUsuario(usuarioId).collect { misPedidos ->
                    _uiState.update { it.copy(pedidosRecientes = misPedidos) }
                }
            }
        }
    }
}
