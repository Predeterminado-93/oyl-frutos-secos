package com.OYL.frutossecos.data.repository

import com.OYL.frutossecos.data.local.dao.UsuarioDao
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de Usuarios y Autenticación.
 * Maneja la lógica de inicio de sesión, registro de clientes y persistencia de sesión.
 */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /**
     * Valida credenciales contra SQLite/Room.
     * Comprueba existencia de alias, correspondencia de contraseña y rol seleccionado.
     * Si todo es correcto, activa la sesión local.
     */
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

        // Credenciales válidas: actualizar sesión en SQLite
        usuarioDao.cerrarTodasLasSesiones()
        usuarioDao.activarSesion(usuario.id)

        val usuarioConSesion = usuario.copy(sesionActiva = true)
        return Result.success(usuarioConSesion)
    }

    /**
     * Registra un nuevo cliente validando campos obligatorios y duplicados.
     * Solo permite rol "CLIENTE".
     */
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

        // Validaciones de campos obligatorios
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

        // Comprobar que no exista duplicidad de alias
        val aliasExistente = usuarioDao.obtenerPorAlias(cleanAlias)
        if (aliasExistente != null) {
            return Result.failure(IllegalArgumentException("El alias '$cleanAlias' ya se encuentra en uso. Elige otro."))
        }

        // Comprobar que no exista duplicidad de email
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

        // Limpiar sesiones activas anteriores para no mezclar datos de clientes
        usuarioDao.cerrarTodasLasSesiones()
        val nuevoId = usuarioDao.insertar(nuevoCliente.copy(sesionActiva = false)).toInt()
        usuarioDao.activarSesion(nuevoId)
        val clienteCreado = nuevoCliente.copy(id = nuevoId, sesionActiva = true)

        return Result.success(clienteCreado)
    }

    /**
     * Obtiene el flujo del usuario que tiene la sesión activa actualmente.
     */
    fun obtenerUsuarioActivoFlow(): Flow<UsuarioEntity?> = usuarioDao.usuarioActivo()

    /**
     * Obtiene directamente el usuario activo.
     */
    suspend fun obtenerUsuarioActivo(): UsuarioEntity? = usuarioDao.obtenerUsuarioActivo()

    /**
     * Cierra todas las sesiones activas en SQLite.
     */
    suspend fun cerrarSesion() {
        usuarioDao.cerrarTodasLasSesiones()
    }
}
