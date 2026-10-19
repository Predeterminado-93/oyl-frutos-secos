
package com.OYL.frutossecos.data.repository

import com.OYL.frutossecos.data.local.dao.NotificacionDao
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import kotlinx.coroutines.flow.Flow

class NotificacionRepository(
    private val notificacionDao: NotificacionDao
) {

    fun obtenerNotificaciones(usuarioId: Int): Flow<List<NotificacionEntity>> {
        return notificacionDao.notificacionesPorUsuario(usuarioId)
    }

    fun obtenerNotificacionesPorTipo(
        usuarioId: Int,
        tipo: String
    ): Flow<List<NotificacionEntity>> {
        return notificacionDao.notificacionesPorTipo(usuarioId, tipo)
    }

    fun contarNoLeidas(usuarioId: Int): Flow<Int> {
        return notificacionDao.contarNoLeidas(usuarioId)
    }

    suspend fun insertar(notificacion: NotificacionEntity): Long {
        return notificacionDao.insertar(notificacion)
    }

    suspend fun insertarTodas(notificaciones: List<NotificacionEntity>) {
        notificacionDao.insertarTodas(notificaciones)
    }

    suspend fun marcarComoLeida(
        notificacionId: Int,
        usuarioId: Int
    ): Int {
        return notificacionDao.marcarComoLeida(notificacionId, usuarioId)
    }

    suspend fun marcarTodasComoLeidas(usuarioId: Int): Int {
        return notificacionDao.marcarTodasComoLeidas(usuarioId)
    }

    suspend fun eliminar(
        notificacionId: Int,
        usuarioId: Int
    ): Int {
        return notificacionDao.eliminar(notificacionId, usuarioId)
    }

    suspend fun eliminarTodas(usuarioId: Int): Int {
        return notificacionDao.eliminarTodas(usuarioId)
    }
}
