package com.OYL.frutossecos.data.local.dao

import androidx.room.*
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE LOWER(alias) = LOWER(:alias) LIMIT 1")
    suspend fun obtenerPorAlias(alias: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun obtenerPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(alias) = LOWER(:alias) AND password = :password LIMIT 1")
    suspend fun login(alias: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 LIMIT 1")
    fun usuarioActivo(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 LIMIT 1")
    suspend fun obtenerUsuarioActivo(): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    @Query("UPDATE usuarios SET sesionActiva = 0")
    suspend fun cerrarTodasLasSesiones()

    @Query("UPDATE usuarios SET sesionActiva = 1 WHERE id = :id")
    suspend fun activarSesion(id: Int)

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun todosLosUsuarios(): Flow<List<UsuarioEntity>>

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity)
}
