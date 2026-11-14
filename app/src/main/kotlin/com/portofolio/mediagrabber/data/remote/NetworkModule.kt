package com.portofolio.mediagrabber.data.remote

import com.portofolio.mediagrabber.BuildConfig
import com.portofolio.mediagrabber.data.remote.api.CobaltApiService
import com.portofolio.mediagrabber.data.remote.api.GithubReleaseApiService
import com.portofolio.mediagrabber.data.remote.api.TikWmApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val contentType = "application/json".toMediaType()

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

    val cobaltApiService: CobaltApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.COBALT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(CobaltApiService::class.java)
    }

    val tikWmApiService: TikWmApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.TIKWM_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(TikWmApiService::class.java)
    }

    val githubReleaseApiService: GithubReleaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.GITHUB_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GithubReleaseApiService::class.java)
    }
}
