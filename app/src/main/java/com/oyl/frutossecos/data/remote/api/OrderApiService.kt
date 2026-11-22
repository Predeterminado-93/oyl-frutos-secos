package com.OYL.frutossecos.data.remote.api

import com.OYL.frutossecos.data.remote.dto.RemoteVentaRequest
import com.OYL.frutossecos.data.remote.dto.RemoteVentaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApiService {

    @POST("api/ventas")
    suspend fun crearVenta(@Body request: RemoteVentaRequest): Response<RemoteVentaResponse>

    @GET("api/ventas/{id}")
    suspend fun obtenerVenta(@Path("id") id: Long): Response<RemoteVentaResponse>

    @GET("api/ventas/usuario/{usuarioId}")
    suspend fun listarVentasPorUsuario(@Path("usuarioId") usuarioId: Long): Response<List<RemoteVentaResponse>>
}
