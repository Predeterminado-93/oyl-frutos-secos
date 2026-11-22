
package com.OYL.frutossecos.data.repository

import android.util.Log
import com.OYL.frutossecos.data.local.dao.UsuarioDao
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import com.OYL.frutossecos.data.remote.ApiClient
import com.OYL.frutossecos.data.remote.dto.RemoteLoginRequest
import com.OYL.frutossecos.data.remote.dto.RemoteRegistroRequest
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // -------------------------------------------------------------------------
    // LOGIN: intenta backend primero; si falla, cae a Room (modo offline)
    // -------------------------------------------------------------------------
    suspend fun login(
        alias: String,
        password: String,
        rolEsperado: String
    ): Result<UsuarioEntity> {
        val cleanAlias = alias.trim()
        val cleanPassword = password.trim()

        if (cleanAlias.isBlank() || cleanPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Por favor ingresa tu email/alias y contraseña."))
        }

        // --- Intento backend (el backend usa email, el campo alias puede ser email) ---
        try {
            val response = ApiClient.authService.login(
                RemoteLoginRequest(email = cleanAlias, password = cleanPassword)
            )
            if (response.isSuccessful) {
                val remoto = response.body()
                if (remoto != null) {
                    val remoteRol = when {
                        remoto.usuario.rol.contains("ADMIN", ignoreCase = true) -> "ADMIN"
                        else -> "CLIENTE"
                    }
                    if (remoteRol != rolEsperado) {
                        val msg = if (rolEsperado == "ADMIN")
                            "El usuario no tiene permisos de Administrador."
                        else
                            "La cuenta es de Administrador. Selecciona el rol Administrador."
                        return Result.failure(IllegalArgumentException(msg))
                    }
                    // Sincroniza o crea el usuario en Room para que la sesión persista
                    val existente = usuarioDao.obtenerPorEmail(remoto.usuario.email)
                    val entidad = if (existente != null) {
                        existente.copy(
                            nombre = remoto.usuario.nombre,
                            sesionActiva = true
                        )
                    } else {
                        UsuarioEntity(
                            nombre = "${remoto.usuario.nombre} ${remoto.usuario.apellido}".trim(),
                            alias = remoto.usuario.email,
                            email = remoto.usuario.email,
                            password = cleanPassword,
                            telefono = remoto.usuario.telefono ?: "",
                            direccion = remoto.usuario.direccion ?: "",
                            rol = remoteRol,
                            sesionActiva = true
                        )
                    }
                    usuarioDao.cerrarTodasLasSesiones()
                    if (existente != null) {
                        usuarioDao.actualizar(entidad)
                    } else {
                        val newId = usuarioDao.insertar(entidad.copy(sesionActiva = false)).toInt()
                        usuarioDao.activarSesion(newId)
                        return Result.success(entidad.copy(id = newId))
                    }
                    usuarioDao.activarSesion(entidad.id)
                    return Result.success(entidad)
                }
            }
            Log.w("UsuarioRepository", "Backend login falló (${response.code()}), intentando Room...")
        } catch (e: Exception) {
            Log.w("UsuarioRepository", "Sin conexión al backend, usando Room: ${e.message}")
        }

        // --- Fallback: Room local ---
        val usuario = usuarioDao.obtenerPorAlias(cleanAlias)
            ?: usuarioDao.obtenerPorEmail(cleanAlias)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado. Verifica tus credenciales."))

        if (usuario.password != cleanPassword) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta."))
        }
        if (usuario.rol != rolEsperado) {
            val msg = if (rolEsperado == "ADMIN")
                "El usuario no tiene permisos de Administrador."
            else
                "La cuenta es de Administrador. Selecciona el rol Administrador."
            return Result.failure(IllegalArgumentException(msg))
        }
        usuarioDao.cerrarTodasLasSesiones()
        usuarioDao.activarSesion(usuario.id)
        return Result.success(usuario.copy(sesionActiva = true))
    }

    // -------------------------------------------------------------------------
    // REGISTRO: intenta backend; si falla, guarda solo en Room
    // -------------------------------------------------------------------------
    suspend fun registrarCliente(
        nombre: String,
        alias: String,
        password: String,
        email: String,
        telefono: String,
        direccion: String
    ): Result<UsuarioEntity> {
        val cleanNombre = nombre.trim()
        val cleanAlias = alias.trim()
        val cleanPassword = password.trim()
        val cleanEmail = email.trim()
        val cleanTelefono = telefono.trim()
        val cleanDireccion = direccion.trim()

        if (cleanNombre.isBlank()) return Result.failure(IllegalArgumentException("El nombre completo es obligatorio."))
        if (cleanAlias.isBlank()) return Result.failure(IllegalArgumentException("El alias de usuario es obligatorio."))
        if (cleanAlias.length < 3) return Result.failure(IllegalArgumentException("El alias debe tener al menos 3 caracteres."))
        if (cleanPassword.isBlank()) return Result.failure(IllegalArgumentException("La contraseña es obligatoria."))
        if (cleanPassword.length < 4) return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 4 caracteres."))
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains("."))
            return Result.failure(IllegalArgumentException("Ingresa un correo electrónico válido."))
        if (cleanTelefono.isBlank()) return Result.failure(IllegalArgumentException("El teléfono de contacto es obligatorio."))
        if (cleanDireccion.isBlank()) return Result.failure(IllegalArgumentException("La dirección de entrega es obligatoria."))

        // Separa nombre y apellido (todo lo que venga después del primer espacio es apellido)
        val partes = cleanNombre.split(" ", limit = 2)
        val primerNombre = partes[0]
        val apellido = if (partes.size > 1) partes[1] else "."

        // --- Intento backend ---
        try {
            val response = ApiClient.authService.register(
                RemoteRegistroRequest(
                    nombre = primerNombre,
                    apellido = apellido,
                    email = cleanEmail,
                    password = cleanPassword,
                    telefono = cleanTelefono.ifBlank { null },
                    direccion = cleanDireccion.ifBlank { null }
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Log.i("UsuarioRepository", "Registro exitoso en backend.")
            } else {
                Log.w("UsuarioRepository", "Backend registro falló (${response.code()}), guardando solo en Room.")
            }
        } catch (e: Exception) {
            Log.w("UsuarioRepository", "Sin conexión para registro, guardando en Room: ${e.message}")
        }

        // Siempre guarda en Room para que la sesión funcione offline
        val aliasExistente = usuarioDao.obtenerPorAlias(cleanAlias)
        if (aliasExistente != null) return Result.failure(IllegalArgumentException("El alias '$cleanAlias' ya está en uso."))

        val emailExistente = usuarioDao.obtenerPorEmail(cleanEmail)
        if (emailExistente != null) return Result.failure(IllegalArgumentException("El correo '$cleanEmail' ya está registrado."))

        val nuevoCliente = UsuarioEntity(
            nombre = cleanNombre,
            alias = cleanAlias,
            email = cleanEmail,
            password = cleanPassword,
            telefono = cleanTelefono,
            direccion = cleanDireccion,
            rol = "CLIENTE",
            sesionActiva = true
        )
        usuarioDao.cerrarTodasLasSesiones()
        val nuevoId = usuarioDao.insertar(nuevoCliente.copy(sesionActiva = false)).toInt()
        usuarioDao.activarSesion(nuevoId)
        return Result.success(nuevoCliente.copy(id = nuevoId, sesionActiva = true))
    }

    suspend fun actualizarPerfil(
        usuarioId: Int,
        nombre: String,
        email: String,
        telefono: String,
        direccion: String
    ): Result<UsuarioEntity> {
        val cleanNombre = nombre.trim()
        val cleanEmail = email.trim()
        val cleanTelefono = telefono.trim()
        val cleanDireccion = direccion.trim()

        if (cleanNombre.isBlank()) return Result.failure(IllegalArgumentException("El nombre completo es obligatorio."))
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains("."))
            return Result.failure(IllegalArgumentException("Ingresa un correo electrónico válido."))
        if (cleanTelefono.isBlank()) return Result.failure(IllegalArgumentException("El teléfono de contacto es obligatorio."))
        if (cleanDireccion.isBlank()) return Result.failure(IllegalArgumentException("La dirección es obligatoria."))

        val usuarioActual = usuarioDao.obtenerPorId(usuarioId)
            ?: return Result.failure(IllegalArgumentException("No se encontró el usuario."))

        val emailExistente = usuarioDao.obtenerPorEmail(cleanEmail)
        if (emailExistente != null && emailExistente.id != usuarioId)
            return Result.failure(IllegalArgumentException("El correo '$cleanEmail' ya está registrado."))

        val usuarioActualizado = usuarioActual.copy(
            nombre = cleanNombre,
            email = cleanEmail,
            telefono = cleanTelefono,
            direccion = cleanDireccion
        )
        usuarioDao.actualizar(usuarioActualizado)
        return Result.success(usuarioActualizado)
    }

    fun obtenerUsuarioActivoFlow(): Flow<UsuarioEntity?> = usuarioDao.usuarioActivo()
    suspend fun obtenerUsuarioActivo(): UsuarioEntity? = usuarioDao.obtenerUsuarioActivo()
    suspend fun cerrarSesion() { usuarioDao.cerrarTodasLasSesiones() }
}
