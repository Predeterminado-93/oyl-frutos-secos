package com.OYL.frutossecos.ui.screens.escanerqr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.OYL.frutossecos.data.local.db.AppDatabase
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.repository.UsuarioRepository
import com.OYL.frutossecos.ui.util.AvisoUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Formato del QR de cada producto: "OYL-PROD-<id>" (también se acepta solo el número del id). */
const val PREFIJO_QR = "OYL-PROD-"

data class EscanerUiState(
    val esAdmin: Boolean = false,
    val productoEncontrado: ProductoEntity? = null,
    val nuevoStock: Int = 0,
    val lecturaExitosa: Boolean = false,
    val errorCodigo: String? = null,
    val aviso: AvisoUi? = null
)

class EscanerQrViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(db.usuarioDao())

    private val _uiState = MutableStateFlow(EscanerUiState())
    val uiState: StateFlow<EscanerUiState> = _uiState.asStateFlow()

    /** Evita procesar el mismo QR muchas veces mientras la cámara sigue enfocándolo. */
    @Volatile
    private var bloqueado = false

    init {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActivo()
            _uiState.update { it.copy(esAdmin = usuario?.rol == "ADMIN") }
        }
    }

    /** Se llama desde la cámara (o desde el ingreso manual) con el texto leído. */
    fun onCodigoLeido(texto: String) {
        if (bloqueado) return
        bloqueado = true

        viewModelScope.launch {
            val id = extraerId(texto)
            val producto = id?.let { db.productoDao().obtenerPorId(it) }?.takeIf { it.activo }

            if (producto == null) {
                _uiState.update {
                    it.copy(errorCodigo = "Código no válido: no corresponde a un producto de OYL Frutos Secos.")
                }
                delay(2000)
                _uiState.update { it.copy(errorCodigo = null) }
                bloqueado = false
            } else {
                _uiState.update {
                    it.copy(
                        productoEncontrado = producto,
                        nuevoStock = producto.stock,
                        lecturaExitosa = true,
                        errorCodigo = null
                    )
                }
                delay(900) // animación breve de lectura exitosa
                _uiState.update { it.copy(lecturaExitosa = false) }
                // bloqueado sigue en true hasta que el usuario cierre el resultado
            }
        }
    }

    fun cerrarResultado() {
        _uiState.update { it.copy(productoEncontrado = null, lecturaExitosa = false) }
        bloqueado = false
    }

    fun aumentarStock() = _uiState.update { it.copy(nuevoStock = it.nuevoStock + 1) }

    fun disminuirStock() = _uiState.update { if (it.nuevoStock > 0) it.copy(nuevoStock = it.nuevoStock - 1) else it }

    /** Solo Administrador. Guarda el nuevo stock en SQLite (luego se sincronizará con el backend). */
    fun guardarInventario() {
        val state = _uiState.value
        val producto = state.productoEncontrado ?: return
        if (!state.esAdmin) return
        viewModelScope.launch {
            db.productoDao().actualizarStock(producto.id, state.nuevoStock)
            _uiState.update {
                it.copy(
                    productoEncontrado = producto.copy(stock = state.nuevoStock),
                    aviso = AvisoUi("Inventario actualizado: ${producto.nombre} ahora tiene ${state.nuevoStock} un.", true)
                )
            }
        }
    }

    fun avisoMostrado() = _uiState.update { it.copy(aviso = null) }

    private fun extraerId(texto: String): Int? {
        val limpio = texto.trim()
        val numero = if (limpio.startsWith(PREFIJO_QR, ignoreCase = true)) {
            limpio.substring(PREFIJO_QR.length)
        } else {
            limpio
        }
        return numero.trim().toIntOrNull()
    }
}
