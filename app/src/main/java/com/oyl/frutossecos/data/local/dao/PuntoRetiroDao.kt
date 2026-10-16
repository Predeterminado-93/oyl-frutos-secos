package com.OYL.frutossecos.data.local.dao

import androidx.room.*
import com.OYL.frutossecos.data.local.entities.PuntoRetiroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoRetiroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoRetiroEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(punto: PuntoRetiroEntity): Long

    @Query("SELECT * FROM puntos_retiro ORDER BY esTiendaPrincipal DESC, nombre ASC")
    fun todosLosPuntos(): Flow<List<PuntoRetiroEntity>>

    @Query("SELECT * FROM puntos_retiro ORDER BY esTiendaPrincipal DESC, nombre ASC")
    suspend fun obtenerTodos(): List<PuntoRetiroEntity>

    @Query("SELECT * FROM puntos_retiro WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): PuntoRetiroEntity?

    @Query("SELECT COUNT(*) FROM puntos_retiro")
    suspend fun contar(): Int
}
