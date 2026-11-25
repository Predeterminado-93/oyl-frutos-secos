
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

        // --- Intento backend (normaliza alias a email si no contiene @) ---
        Log.i("UsuarioRepository", "[OYL-AUTH] Iniciando sesión como: $rolEsperado | Identificador: '$cleanAlias'")

        val emailParaBackend = when {
            cleanAlias.contains("@") -> cleanAlias
            cleanAlias.equals("admin", ignoreCase = true) -> "admin@oyl.cl"
            cleanAlias.equals("cliente", ignoreCase = true) -> "cliente@oyl.cl"
            else -> {
                val local = usuarioDao.obtenerPorAlias(cleanAlias)
                local?.email ?: if (cleanAlias.isNotBlank()) "$cleanAlias@oyl.cl" else cleanAlias
            }
        }

        try {
            val response = ApiClient.authService.login(
                RemoteLoginRequest(email = emailParaBackend, password = cleanPassword)
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
                        val usuarioFinal = entidad.copy(id = newId)
                        Log.i("UsuarioRepository", "[OYL-AUTH] Sesión iniciada con éxito vía Backend como: ${usuarioFinal.rol} | Usuario: '${usuarioFinal.nombre}' (${usuarioFinal.email})")
                        return Result.success(usuarioFinal)
                    }
                    usuarioDao.activarSesion(entidad.id)
                    Log.i("UsuarioRepository", "[OYL-AUTH] Sesión iniciada con éxito vía Backend como: ${entidad.rol} | Usuario: '${entidad.nombre}' (${entidad.email})")
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
        Log.i("UsuarioRepository", "[OYL-AUTH] Sesión iniciada con éxito vía Room Local como: ${usuario.rol} | Usuario: '${usuario.nombre}'")
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

        // Separa nombre y apellido (todo lo que venga después del primer espacio es apellido).
        // Si no se ingresó apellido, usamos el primer nombre como apellido para cumplir con el mínimo de 2 caracteres en el backend.
        val partes = cleanNombre.split(" ", limit = 2)
        val primerNombre = partes[0]
        val apellido = if (partes.size > 1 && partes[1].trim().length >= 2) partes[1].trim() else primerNombre

        // Verificamos antes si ya existe en Room local por alias o correo para fallar rápido
        val aliasExistente = usuarioDao.obtenerPorAlias(cleanAlias)
        if (aliasExistente != null) return Result.failure(IllegalArgumentException("El alias '$cleanAlias' ya está en uso."))

        val emailExistente = usuarioDao.obtenerPorEmail(cleanEmail)
        if (emailExistente != null) return Result.failure(IllegalArgumentException("El correo '$cleanEmail' ya está registrado."))

        var idRemoto: Int? = null

        // --- Intento backend (PostgreSQL) ---
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
                val body = response.body()!!
                idRemoto = body.id.toInt()
                Log.i("UsuarioRepository", "[OYL-AUTH] Registro exitoso en PostgreSQL (ID: $idRemoto) para '$cleanNombre' ($cleanEmail)")
            } else {
                val err = response.errorBody()?.string() ?: ""
                Log.e("UsuarioRepository", "[OYL-AUTH] Backend registro falló (${response.code()}): $err")
                if (response.code() == 409 || err.contains("registrado", ignoreCase = true) || err.contains("existe", ignoreCase = true)) {
                    return Result.failure(IllegalArgumentException("El correo electrónico ya está registrado en la base de datos."))
                }
                if (response.code() == 400) {
                    val msg = if (err.isNotBlank() && err.contains("message")) err else "Datos de registro inválidos en el servidor."
                    return Result.failure(IllegalArgumentException(msg))
                }
            }
        } catch (e: Exception) {
            Log.w("UsuarioRepository", "[OYL-AUTH] Sin conexión al backend para registro (${e.message}), guardando en Room local.")
        }

        // Guarda en Room para que la sesión funcione offline y persista en la app
        val nuevoCliente = UsuarioEntity(
            id = idRemoto ?: 0,
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
        val idFinal = if (idRemoto != null && idRemoto > 0) idRemoto else nuevoId
        usuarioDao.activarSesion(idFinal)
        val usuarioRetorno = nuevoCliente.copy(id = idFinal, sesionActiva = true)
        Log.i("UsuarioRepository", "[OYL-AUTH] Usuario registrado y sesión iniciada: '${usuarioRetorno.nombre}' (ID: ${usuarioRetorno.id})")
        return Result.success(usuarioRetorno)
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
        Log.i(
            "UsuarioRepository",
            "[OYL-CLIENTE] PERFIL DE CLIENTE ACTUALIZADO (ID: $usuarioId): Nombre: '$cleanNombre' | Email: '$cleanEmail' | Tel: '$cleanTelefono' | Dirección: '$cleanDireccion'"
        )
        return Result.success(usuarioActualizado)
    }

    fun obtenerUsuarioActivoFlow(): Flow<UsuarioEntity?> = usuarioDao.usuarioActivo()
    suspend fun obtenerUsuarioActivo(): UsuarioEntity? = usuarioDao.obtenerUsuarioActivo()
    suspend fun cerrarSesion() { usuarioDao.cerrarTodasLasSesiones() }
}
