package com.OYL.frutossecos.data.remote

import com.OYL.frutossecos.data.remote.api.AuthApiService
import com.OYL.frutossecos.data.remote.api.OrderApiService
import com.OYL.frutossecos.data.remote.api.ProductApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit centralizado para la comunicación entre la aplicación Android
 * y el API Gateway del backend de OYL Frutos Secos.
 */
object ApiClient {

    /**
     * IP de tu PC en la red WiFi local (192.168.1.14).
     * Esto permite que dispositivos físicos (como tu teléfono en 192.168.1.21)
     * y también el emulador puedan conectarse directamente al API Gateway en el puerto 8080.
     */
    private const val BASE_URL = "http://192.168.1.14:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val productService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }

    val orderService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }
}
