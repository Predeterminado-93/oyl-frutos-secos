
package com.OYL.frutossecos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.OYL.frutossecos.data.local.entities.MovimientoInventarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoInventarioDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoInventarioEntity): Long

    @Query("SELECT * FROM movimientos_inventario ORDER BY fecha DESC")
    fun todos(): Flow<List<MovimientoInventarioEntity>>

    @Query("SELECT * FROM movimientos_inventario WHERE productoId = :productoId ORDER BY fecha DESC")
    fun porProducto(productoId: Int): Flow<List<MovimientoInventarioEntity>>

    @Query("SELECT * FROM movimientos_inventario WHERE sincronizado = 0 ORDER BY fecha ASC")
    suspend fun pendientesSincronizacion(): List<MovimientoInventarioEntity>
}
