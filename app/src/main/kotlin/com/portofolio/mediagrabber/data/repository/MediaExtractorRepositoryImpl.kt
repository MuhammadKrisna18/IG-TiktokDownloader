package com.portofolio.mediagrabber.data.repository

import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.data.remote.api.CobaltApiService
import com.portofolio.mediagrabber.data.remote.api.TikWmApiService
import com.portofolio.mediagrabber.data.remote.dto.CobaltRequestDto
import com.portofolio.mediagrabber.domain.model.DownloadOption
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.repository.MediaExtractorRepository
import com.portofolio.mediagrabber.domain.usecase.DetectPlatformUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MediaExtractorRepositoryImpl(
    private val tikWmApiService: TikWmApiService,
    private val cobaltApiService: CobaltApiService,
    private val detectPlatformUseCase: DetectPlatformUseCase = DetectPlatformUseCase()
) : MediaExtractorRepository {

    override suspend fun extractMedia(url: String): Resource<MediaItem> = withContext(Dispatchers.IO) {
        val platform = detectPlatformUseCase(url)
        when (platform) {
            PlatformType.TIKTOK -> extractTikTokMedia(url)
            PlatformType.INSTAGRAM -> extractInstagramMedia(url)
            PlatformType.UNKNOWN -> extractGenericMedia(url)
        }
    }

    private suspend fun extractTikTokMedia(url: String): Resource<MediaItem> {
        return try {
            val response = tikWmApiService.extractMedia(url)
            val data = response.data
            if (response.code == 0 && data != null) {
                val options = mutableListOf<DownloadOption>()
                val baseUrl = data.hdplay ?: data.play
                if (!baseUrl.isNullOrBlank()) {
                    options.add(
                        DownloadOption(
                            id = UUID.randomUUID().toString(),
                            title = "HD (No Watermark)",
                            downloadUrl = if (baseUrl.startsWith("http")) baseUrl else "https://www.tikwm.com$baseUrl",
                            isWatermarkFree = true,
                            quality = "1080p",
                            extension = "mp4"
                        )
                    )
                }
                if (!data.play.isNullOrBlank() && data.play != data.hdplay) {
                    val playUrl = if (data.play.startsWith("http")) data.play else "https://www.tikwm.com${data.play}"
                    options.add(
                        DownloadOption(
                            id = UUID.randomUUID().toString(),
                            title = "SD (No Watermark)",
                            downloadUrl = playUrl,
                            isWatermarkFree = true,
                            quality = "720p",
                            extension = "mp4"
                        )
                    )
                }
                if (!data.wmplay.isNullOrBlank()) {
                    val wmUrl = if (data.wmplay.startsWith("http")) data.wmplay else "https://www.tikwm.com${data.wmplay}"
                    options.add(
                        DownloadOption(
                            id = UUID.randomUUID().toString(),
                            title = "With Watermark",
                            downloadUrl = wmUrl,
                            isWatermarkFree = false,
                            quality = "Standard",
                            extension = "mp4"
                        )
                    )
                }

                if (options.isNotEmpty()) {
                    val rawCover = data.cover ?: ""
                    val coverUrl = if (rawCover.startsWith("http")) rawCover else if (rawCover.isNotBlank()) "https://www.tikwm.com$rawCover" else ""
                    return Resource.Success(
                        MediaItem(
                            id = data.id ?: UUID.randomUUID().toString(),
                            originalUrl = url,
                            title = data.title?.ifBlank { "TikTok Video" } ?: "TikTok Video",
                            author = data.author?.nickname ?: data.author?.uniqueId ?: "TikTok User",
                            coverUrl = coverUrl,
                            durationSeconds = data.duration ?: 0L,
                            platform = PlatformType.TIKTOK,
                            downloadOptions = options
                        )
                    )
                }
            }
            extractGenericMedia(url, PlatformType.TIKTOK)
        } catch (_: Exception) {
            extractGenericMedia(url, PlatformType.TIKTOK)
        }
    }

    private suspend fun extractInstagramMedia(url: String): Resource<MediaItem> {
        return extractGenericMedia(url, PlatformType.INSTAGRAM)
    }

    private suspend fun extractGenericMedia(
        url: String,
        fallbackPlatform: PlatformType = PlatformType.UNKNOWN
    ): Resource<MediaItem> {
        return try {
            val response = cobaltApiService.extractMedia(CobaltRequestDto(url = url))
            val directUrl = response.url
            val pickerItems = response.picker

            val options = mutableListOf<DownloadOption>()
            if (!directUrl.isNullOrBlank()) {
                options.add(
                    DownloadOption(
                        id = UUID.randomUUID().toString(),
                        title = response.filename ?: "Standard Quality",
                        downloadUrl = directUrl,
                        isWatermarkFree = true,
                        quality = "Default",
                        extension = "mp4"
                    )
                )
            } else if (!pickerItems.isNullOrEmpty()) {
                pickerItems.forEachIndexed { index, item ->
                    item.url?.let { itemUrl ->
                        options.add(
                            DownloadOption(
                                id = UUID.randomUUID().toString(),
                                title = "Item #${index + 1}",
                                downloadUrl = itemUrl,
                                isWatermarkFree = true,
                                quality = "Default",
                                extension = if (item.type == "photo") "jpg" else "mp4"
                            )
                        )
                    }
                }
            }

            if (options.isNotEmpty()) {
                val coverUrl = pickerItems?.firstOrNull()?.thumb ?: ""
                Resource.Success(
                    MediaItem(
                        id = UUID.randomUUID().toString(),
                        originalUrl = url,
                        title = response.filename ?: response.text ?: "Social Media Video",
                        author = "User",
                        coverUrl = coverUrl,
                        durationSeconds = 0L,
                        platform = if (fallbackPlatform != PlatformType.UNKNOWN) fallbackPlatform else detectPlatformUseCase(url),
                        downloadOptions = options
                    )
                )
            } else {
                Resource.Error(response.text ?: "Could not resolve download link")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to extract media", e)
        }
    }
}
