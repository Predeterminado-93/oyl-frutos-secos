
package com.OYL.frutossecos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(notificacion: NotificacionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(notificaciones: List<NotificacionEntity>)

    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun notificacionesPorUsuario(usuarioId: Int): Flow<List<NotificacionEntity>>

    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId AND tipo = :tipo ORDER BY fecha DESC")
    fun notificacionesPorTipo(usuarioId: Int, tipo: String): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE usuarioId = :usuarioId AND leida = 0")
    fun contarNoLeidas(usuarioId: Int): Flow<Int>

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :notificacionId AND usuarioId = :usuarioId")
    suspend fun marcarComoLeida(notificacionId: Int, usuarioId: Int): Int

    @Query("UPDATE notificaciones SET leida = 1 WHERE usuarioId = :usuarioId")
    suspend fun marcarTodasComoLeidas(usuarioId: Int): Int

    @Query("DELETE FROM notificaciones WHERE id = :notificacionId AND usuarioId = :usuarioId")
    suspend fun eliminar(notificacionId: Int, usuarioId: Int): Int

    @Query("DELETE FROM notificaciones WHERE usuarioId = :usuarioId")
    suspend fun eliminarTodas(usuarioId: Int): Int
}
