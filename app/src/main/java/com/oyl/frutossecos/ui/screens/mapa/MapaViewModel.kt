package com.OYL.frutossecos.ui.screens.mapa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.PuntoRetiroEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapaUiState(
    val puntos: List<PuntoRetiroEntity> = emptyList(),
    val puntoSeleccionado: PuntoRetiroEntity? = null,
    val filtroSeleccionado: String = "TODOS", // "TODOS" | "TIENDAS" | "METRO"
    val isLoading: Boolean = true
)

class MapaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        cargarPuntos()
    }

    private fun cargarPuntos() {
        viewModelScope.launch {
            db.puntoRetiroDao().todosLosPuntos().collect { lista ->
                _uiState.update { state ->
                    state.copy(
                        puntos = lista,
                        puntoSeleccionado = state.puntoSeleccionado ?: lista.firstOrNull { it.esTiendaPrincipal } ?: lista.firstOrNull(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun seleccionarPunto(punto: PuntoRetiroEntity) {
        _uiState.update { it.copy(puntoSeleccionado = punto) }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        _uiState.update { it.copy(filtroSeleccionado = nuevoFiltro) }
    }
}
