package com.OYL.frutossecos.data.remote.api

import com.OYL.frutossecos.data.remote.dto.RemoteLoginRequest
import com.OYL.frutossecos.data.remote.dto.RemoteLoginResponse
import com.OYL.frutossecos.data.remote.dto.RemoteRegistroRequest
import com.OYL.frutossecos.data.remote.dto.RemoteUsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {

    @POST("api/usuarios/login")
    suspend fun login(@Body request: RemoteLoginRequest): Response<RemoteLoginResponse>

    @POST("api/usuarios/register")
    suspend fun register(@Body request: RemoteRegistroRequest): Response<RemoteUsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtenerPerfil(@Path("id") id: Long): Response<RemoteUsuarioResponse>
}
