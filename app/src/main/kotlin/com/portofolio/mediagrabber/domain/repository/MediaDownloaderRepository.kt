package com.portofolio.mediagrabber.domain.repository

import com.portofolio.mediagrabber.domain.model.DownloadTask
import kotlinx.coroutines.flow.Flow

interface MediaDownloaderRepository {
    fun downloadMedia(
        url: String,
        filename: String,
        title: String,
        mimeType: String = "video/mp4"
    ): Flow<DownloadTask>

    fun cancelDownload(taskId: String)
}
