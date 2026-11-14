package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.repository.DownloadHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadHistoryUseCasesTest {

    private val historyItems = mutableListOf<DownloadedMedia>()

    private val fakeRepository = object : DownloadHistoryRepository {
        override fun getAllHistory(): Flow<List<DownloadedMedia>> {
            return flowOf(historyItems.toList())
        }

        override suspend fun saveHistory(item: DownloadedMedia) {
            historyItems.add(item)
        }

        override suspend fun deleteHistory(id: String) {
            historyItems.removeAll { it.id == id }
        }
    }

    private val getHistoryUseCase = GetDownloadHistoryUseCase(fakeRepository)
    private val saveHistoryUseCase = SaveDownloadHistoryUseCase(fakeRepository)
    private val deleteHistoryUseCase = DeleteDownloadHistoryUseCase(fakeRepository)

    @Test
    fun saveAndGetHistory_success() = runTest {
        val item = DownloadedMedia(
            id = "media-1",
            title = "Test Video",
            author = "Test Author",
            coverUrl = "https://example.com/cover.jpg",
            platform = PlatformType.TIKTOK,
            contentUri = "content://media/external/video/media/1",
            filename = "test.mp4"
        )

        saveHistoryUseCase(item)
        val list = getHistoryUseCase().first()

        assertEquals(1, list.size)
        assertEquals("Test Video", list[0].title)
    }

    @Test
    fun deleteHistory_success() = runTest {
        val item = DownloadedMedia(
            id = "media-2",
            title = "Delete Me",
            author = "Author",
            coverUrl = "https://example.com/cover.jpg",
            platform = PlatformType.INSTAGRAM,
            contentUri = "content://media/external/video/media/2",
            filename = "delete.mp4"
        )

        saveHistoryUseCase(item)
        deleteHistoryUseCase("media-2")
        val list = getHistoryUseCase().first()

        assertEquals(0, list.size)
    }
}
