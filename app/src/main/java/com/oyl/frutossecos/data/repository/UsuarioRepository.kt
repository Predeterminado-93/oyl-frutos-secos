
package com.OYL.frutossecos.data.repository

import com.OYL.frutossecos.data.local.dao.UsuarioDao
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun login(
        alias: String,
        password: String,
        rolEsperado: String
    ): Result<UsuarioEntity> {
        val cleanAlias = alias.trim()
        val cleanPassword = password.trim()

        if (cleanAlias.isBlank() || cleanPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Por favor ingresa tu alias y contraseña."))
        }

        val usuario = usuarioDao.obtenerPorAlias(cleanAlias)
            ?: return Result.failure(IllegalArgumentException("El usuario '$cleanAlias' no está registrado."))

        if (usuario.password != cleanPassword) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta. Inténtalo de nuevo."))
        }

        if (usuario.rol != rolEsperado) {
            val mensajeRol = if (rolEsperado == "ADMIN") {
                "El usuario '$cleanAlias' no tiene permisos de Administrador."
            } else {
                "La cuenta '$cleanAlias' es de Administrador. Selecciona el rol Administrador."
            }
            return Result.failure(IllegalArgumentException(mensajeRol))
        }

        usuarioDao.cerrarTodasLasSesiones()
        usuarioDao.activarSesion(usuario.id)

        val usuarioConSesion = usuario.copy(sesionActiva = true)
        return Result.success(usuarioConSesion)
    }

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

        if (cleanNombre.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre completo es obligatorio."))
        }
        if (cleanAlias.isBlank()) {
            return Result.failure(IllegalArgumentException("El alias de usuario es obligatorio."))
        }
        if (cleanAlias.length < 3) {
            return Result.failure(IllegalArgumentException("El alias debe tener al menos 3 caracteres."))
        }
        if (cleanPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("La contraseña es obligatoria."))
        }
        if (cleanPassword.length < 4) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 4 caracteres."))
        }
        if (cleanEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo electrónico es obligatorio."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo electrónico válido."))
        }
        if (cleanTelefono.isBlank()) {
            return Result.failure(IllegalArgumentException("El teléfono de contacto es obligatorio."))
        }
        if (cleanDireccion.isBlank()) {
            return Result.failure(IllegalArgumentException("La dirección de entrega es obligatoria."))
        }

        val aliasExistente = usuarioDao.obtenerPorAlias(cleanAlias)
        if (aliasExistente != null) {
            return Result.failure(IllegalArgumentException("El alias '$cleanAlias' ya se encuentra en uso. Elige otro."))
        }

        val emailExistente = usuarioDao.obtenerPorEmail(cleanEmail)
        if (emailExistente != null) {
            return Result.failure(IllegalArgumentException("El correo '$cleanEmail' ya está registrado."))
        }

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
        val clienteCreado = nuevoCliente.copy(id = nuevoId, sesionActiva = true)

        return Result.success(clienteCreado)
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

        if (cleanNombre.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre completo es obligatorio."))
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo electrónico válido."))
        }
        if (cleanTelefono.isBlank()) {
            return Result.failure(IllegalArgumentException("El teléfono de contacto es obligatorio."))
        }
        if (cleanDireccion.isBlank()) {
            return Result.failure(IllegalArgumentException("La dirección es obligatoria."))
        }

        val usuarioActual = usuarioDao.obtenerPorId(usuarioId)
            ?: return Result.failure(IllegalArgumentException("No se encontró el usuario."))

        val emailExistente = usuarioDao.obtenerPorEmail(cleanEmail)
        if (emailExistente != null && emailExistente.id != usuarioId) {
            return Result.failure(IllegalArgumentException("El correo '$cleanEmail' ya está registrado."))
        }

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

    suspend fun cerrarSesion() {
        usuarioDao.cerrarTodasLasSesiones()
    }
}
