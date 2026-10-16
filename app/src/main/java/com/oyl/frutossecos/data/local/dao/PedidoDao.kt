package com.OYL.frutossecos.data.local.dao

import androidx.room.*
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(pedidos: List<PedidoEntity>)

    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun pedidosPorUsuario(usuarioId: Int): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos ORDER BY fecha DESC")
    fun todosLosPedidos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos WHERE estado = 'PENDIENTE' ORDER BY fecha DESC")
    fun pedidosPendientes(): Flow<List<PedidoEntity>>

    @Query("SELECT COUNT(*) FROM pedidos")
    suspend fun contar(): Int
}
