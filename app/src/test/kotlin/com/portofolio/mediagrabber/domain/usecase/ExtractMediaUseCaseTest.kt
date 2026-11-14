package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.domain.model.DownloadOption
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.repository.MediaExtractorRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractMediaUseCaseTest {

    private val fakeRepository = object : MediaExtractorRepository {
        override suspend fun extractMedia(url: String): Resource<MediaItem> {
            return Resource.Success(
                MediaItem(
                    id = "test-id",
                    originalUrl = url,
                    title = "Test Video",
                    author = "Creator",
                    coverUrl = "https://example.com/cover.jpg",
                    platform = PlatformType.TIKTOK,
                    downloadOptions = listOf(
                        DownloadOption(
                            id = "opt-1",
                            title = "HD",
                            downloadUrl = "https://example.com/video.mp4"
                        )
                    )
                )
            )
        }
    }

    private val useCase = ExtractMediaUseCase(fakeRepository)

    @Test
    fun returnsError_whenUrlIsBlank() = runTest {
        val result = useCase("   ")
        assertTrue(result is Resource.Error)
    }

    @Test
    fun returnsSuccess_whenUrlIsValid() = runTest {
        val result = useCase("https://www.tiktok.com/@creator/video/123")
        assertTrue(result is Resource.Success)
    }
}
