package com.pulsepoint.app.core.network

import com.pulsepoint.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Volatile
    private var baseUrl: String = BuildConfig.BASE_URL

    @Volatile
    private var serviceInstance: ApiService? = null

    fun configure(baseUrl: String) {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        synchronized(this) {
            if (normalized != this.baseUrl) {
                this.baseUrl = normalized
                serviceInstance = buildService()
            }
        }
    }

    val apiService: ApiService
        get() = serviceInstance ?: synchronized(this) {
            serviceInstance ?: buildService().also { serviceInstance = it }
        }

    private fun buildService(): ApiService =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
}
