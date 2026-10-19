package com.OYL.frutossecos.data.local.dao

import androidx.room.*
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItemEntity): Long

    @Update
    suspend fun actualizar(item: CarritoItemEntity)

    @Delete
    suspend fun eliminar(item: CarritoItemEntity)

    @Query("SELECT * FROM carrito_items WHERE usuarioId = :usuarioId ORDER BY agregadoEn DESC")
    fun itemsPorUsuario(usuarioId: Int): Flow<List<CarritoItemEntity>>

    @Query("SELECT * FROM carrito_items WHERE usuarioId = :usuarioId AND productoId = :productoId LIMIT 1")
    suspend fun obtenerItem(usuarioId: Int, productoId: Int): CarritoItemEntity?

    @Query("UPDATE carrito_items SET cantidad = :cantidad WHERE id = :itemId")
    suspend fun actualizarCantidad(itemId: Int, cantidad: Int)

    @Query("DELETE FROM carrito_items WHERE usuarioId = :usuarioId")
    suspend fun vaciarCarrito(usuarioId: Int)

    @Query("SELECT COUNT(*) FROM carrito_items WHERE usuarioId = :usuarioId")
    fun contarItems(usuarioId: Int): Flow<Int>

    @Query("SELECT SUM(productoPrecio * cantidad) FROM carrito_items WHERE usuarioId = :usuarioId")
    fun totalCarrito(usuarioId: Int): Flow<Double?>
}
