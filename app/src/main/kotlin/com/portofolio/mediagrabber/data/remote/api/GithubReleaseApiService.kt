package com.portofolio.mediagrabber.data.remote.api

import com.portofolio.mediagrabber.data.remote.dto.GithubReleaseDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface GithubReleaseApiService {
    @Headers("Accept: application/vnd.github.v3+json")
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GithubReleaseDto
}
