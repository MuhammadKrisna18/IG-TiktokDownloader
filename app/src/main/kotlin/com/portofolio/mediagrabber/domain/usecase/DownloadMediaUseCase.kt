package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.DownloadOption
import com.portofolio.mediagrabber.domain.model.DownloadTask
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.repository.MediaDownloaderRepository
import kotlinx.coroutines.flow.Flow

class DownloadMediaUseCase(
    private val repository: MediaDownloaderRepository
) {
    operator fun invoke(mediaItem: MediaItem, option: DownloadOption): Flow<DownloadTask> {
        val sanitizedTitle = mediaItem.title
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(30)
            .ifBlank { "media" }
        val filename = "${mediaItem.platform.name.lowercase()}_${sanitizedTitle}_${System.currentTimeMillis()}.${option.extension}"
        val mimeType = if (option.extension == "jpg" || option.extension == "png") "image/${option.extension}" else "video/mp4"

        return repository.downloadMedia(
            url = option.downloadUrl,
            filename = filename,
            title = mediaItem.title,
            mimeType = mimeType
        )
    }

    fun cancel(taskId: String) {
        repository.cancelDownload(taskId)
    }
}
