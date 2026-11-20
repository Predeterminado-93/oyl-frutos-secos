package com.OYL.frutossecos.data.remote.api

import com.OYL.frutossecos.data.remote.dto.RemoteCategoriaResponse
import com.OYL.frutossecos.data.remote.dto.RemoteProductoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {

    @GET("api/productos")
    suspend fun listarProductos(): Response<List<RemoteProductoResponse>>

    @GET("api/productos/{id}")
    suspend fun obtenerProducto(@Path("id") id: Long): Response<RemoteProductoResponse>

    @GET("api/productos/categoria/{categoriaId}")
    suspend fun listarPorCategoria(@Path("categoriaId") categoriaId: Long): Response<List<RemoteProductoResponse>>

    @GET("api/productos/buscar")
    suspend fun buscarProductos(@Query("q") query: String): Response<List<RemoteProductoResponse>>

    @GET("api/categorias")
    suspend fun listarCategorias(): Response<List<RemoteCategoriaResponse>>
}
