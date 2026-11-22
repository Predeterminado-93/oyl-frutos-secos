package com.OYL.frutossecos.data.repository

import android.util.Log
import com.OYL.frutossecos.data.local.dao.ProductoDao
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.remote.ApiClient
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de productos: obtiene datos del backend y los almacena
 * en Room como caché para funcionamiento offline.
 */
class ProductoRepository(private val productoDao: ProductoDao) {

    fun todosActivos(): Flow<List<ProductoEntity>> = productoDao.todosActivos()

    /**
     * Sincroniza productos desde el backend hacia Room.
     * Si falla la conexión, Room sirve el caché existente.
     */
    suspend fun sincronizarDesdeBackend() {
        try {
            val response = ApiClient.productService.listarProductos()
            if (response.isSuccessful) {
                val remotos = response.body() ?: return
                val entidades = remotos.map { r ->
                    ProductoEntity(
                        id = r.id.toInt(),
                        nombre = r.nombre,
                        descripcion = r.descripcion ?: "",
                        fotoUrl = r.imagenUrl ?: "",
                        precio = r.precio,
                        stock = r.stock,
                        categoria = r.categoriaNombre ?: "Sin categoría",
                        activo = r.activo
                    )
                }
                productoDao.insertarTodos(entidades)
                Log.i("ProductoRepository", "Sincronizados ${entidades.size} productos desde el backend.")
            } else {
                Log.w("ProductoRepository", "Backend respondió ${response.code()}, usando caché local.")
            }
        } catch (e: Exception) {
            Log.w("ProductoRepository", "Sin conexión al backend, usando caché local: ${e.message}")
        }
    }
}
