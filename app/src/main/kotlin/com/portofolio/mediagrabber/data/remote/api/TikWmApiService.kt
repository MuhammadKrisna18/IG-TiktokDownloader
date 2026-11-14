package com.portofolio.mediagrabber.data.remote.api

import com.portofolio.mediagrabber.data.remote.dto.TikWmResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TikWmApiService {
    @FormUrlEncoded
    @POST("api/")
    suspend fun extractMedia(
        @Field("url") url: String,
        @Field("hd") hd: Int = 1
    ): TikWmResponseDto
}
