package com.OYL.frutossecos.data.remote.dto

import com.google.gson.annotations.SerializedName

// --- Autenticación y Usuarios ---
data class RemoteLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RemoteRegistroRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("direccion") val direccion: String? = null
)

data class RemoteLoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("usuario") val usuario: RemoteUsuarioResponse
)

data class RemoteUsuarioResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("rol") val rol: String,
    @SerializedName("activo") val activo: Boolean
)

// --- Catálogo y Productos ---
data class RemoteCategoriaResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("activo") val activo: Boolean
)

data class RemoteProductoResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagenUrl") val imagenUrl: String?,
    @SerializedName("categoriaId") val categoriaId: Long?,
    @SerializedName("categoriaNombre") val categoriaNombre: String?,
    @SerializedName("activo") val activo: Boolean
)

// --- Ventas y Pedidos ---
data class RemoteDetalleVentaRequest(
    @SerializedName("productoId") val productoId: Long,
    @SerializedName("nombreProducto") val nombreProducto: String,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("cantidad") val cantidad: Int
)

data class RemoteVentaRequest(
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("metodoPago") val metodoPago: String,
    @SerializedName("direccionEntrega") val direccionEntrega: String?,
    @SerializedName("detalles") val detalles: List<RemoteDetalleVentaRequest>
)

data class RemoteDetalleVentaResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("productoId") val productoId: Long,
    @SerializedName("nombreProducto") val nombreProducto: String,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: Double
)

data class RemoteVentaResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("total") val total: Double,
    @SerializedName("estado") val estado: String,
    @SerializedName("metodoPago") val metodoPago: String,
    @SerializedName("direccionEntrega") val direccionEntrega: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("detalles") val detalles: List<RemoteDetalleVentaResponse>
)
