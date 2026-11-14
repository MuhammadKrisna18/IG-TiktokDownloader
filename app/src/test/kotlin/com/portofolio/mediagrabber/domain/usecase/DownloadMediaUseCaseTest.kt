package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.DownloadOption
import com.portofolio.mediagrabber.domain.model.DownloadStatus
import com.portofolio.mediagrabber.domain.model.DownloadTask
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.repository.MediaDownloaderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadMediaUseCaseTest {

    private var capturedFilename: String = ""
    private var capturedMimeType: String = ""

    private val fakeRepository = object : MediaDownloaderRepository {
        override fun downloadMedia(
            url: String,
            filename: String,
            title: String,
            mimeType: String
        ): Flow<DownloadTask> {
            capturedFilename = filename
            capturedMimeType = mimeType
            return flowOf(
                DownloadTask(
                    id = "task-1",
                    url = url,
                    filename = filename,
                    title = title,
                    status = DownloadStatus.DOWNLOADING,
                    progressPercent = 50
                )
            )
        }

        override fun cancelDownload(taskId: String) {}
    }

    private val useCase = DownloadMediaUseCase(fakeRepository)

    @Test
    fun generatesCorrectFilenameAndMimeType_forVideo() = runTest {
        val mediaItem = MediaItem(
            id = "media-1",
            originalUrl = "https://tiktok.com/@user/video/1",
            title = "Funny Cat Video! 100%",
            author = "CatFan",
            coverUrl = "https://example.com/cover.jpg",
            platform = PlatformType.TIKTOK,
            downloadOptions = emptyList()
        )
        val option = DownloadOption(
            id = "opt-1",
            title = "HD (No Watermark)",
            downloadUrl = "https://example.com/video.mp4",
            extension = "mp4"
        )

        val task = useCase(mediaItem, option).first()

        assertEquals("video/mp4", capturedMimeType)
        assertTrue(capturedFilename.startsWith("tiktok_Funny_Cat_Video__100_"))
        assertTrue(capturedFilename.endsWith(".mp4"))
        assertEquals(50, task.progressPercent)
    }

    @Test
    fun generatesCorrectMimeType_forImage() = runTest {
        val mediaItem = MediaItem(
            id = "media-2",
            originalUrl = "https://instagram.com/p/1",
            title = "Photo Post",
            author = "Photographer",
            coverUrl = "https://example.com/photo.jpg",
            platform = PlatformType.INSTAGRAM,
            downloadOptions = emptyList()
        )
        val option = DownloadOption(
            id = "opt-2",
            title = "Photo 1",
            downloadUrl = "https://example.com/photo.jpg",
            extension = "jpg"
        )

        useCase(mediaItem, option).first()

        assertEquals("image/jpg", capturedMimeType)
        assertTrue(capturedFilename.startsWith("instagram_Photo_Post_"))
        assertTrue(capturedFilename.endsWith(".jpg"))
    }
}
