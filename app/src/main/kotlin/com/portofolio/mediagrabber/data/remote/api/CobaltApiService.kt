package com.portofolio.mediagrabber.data.remote.api

import com.portofolio.mediagrabber.data.remote.dto.CobaltRequestDto
import com.portofolio.mediagrabber.data.remote.dto.CobaltResponseDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CobaltApiService {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("/")
    suspend fun extractMedia(@Body request: CobaltRequestDto): CobaltResponseDto
}
