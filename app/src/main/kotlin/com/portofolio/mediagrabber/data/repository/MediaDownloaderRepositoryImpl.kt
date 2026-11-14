package com.portofolio.mediagrabber.data.repository

import android.net.Uri
import com.portofolio.mediagrabber.core.notification.DownloadNotificationHelper
import com.portofolio.mediagrabber.data.storage.MediaStoreHelper
import com.portofolio.mediagrabber.domain.model.DownloadStatus
import com.portofolio.mediagrabber.domain.model.DownloadTask
import com.portofolio.mediagrabber.domain.repository.MediaDownloaderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MediaDownloaderRepositoryImpl(
    private val okHttpClient: OkHttpClient,
    private val mediaStoreHelper: MediaStoreHelper,
    private val notificationHelper: DownloadNotificationHelper
) : MediaDownloaderRepository {

    private val cancelledTasks = ConcurrentHashMap.newKeySet<String>()

    override fun downloadMedia(
        url: String,
        filename: String,
        title: String,
        mimeType: String
    ): Flow<DownloadTask> = flow {
        val taskId = UUID.randomUUID().toString()
        val notificationId = taskId.hashCode()
        var currentTask = DownloadTask(
            id = taskId,
            url = url,
            filename = filename,
            title = title,
            status = DownloadStatus.DOWNLOADING
        )

        emit(currentTask)
        notificationHelper.showProgress(notificationId, title, 0)

        var destinationUri: Uri? = null
        try {
            val destination = mediaStoreHelper.createMediaUri(filename, mimeType)
                ?: error("Failed to create destination uri")
            destinationUri = destination

            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                error("Server returned HTTP ${response.code}")
            }

            val body = response.body ?: error("Empty response body from media url")
            val totalBytes = body.contentLength()
            val outputStream = mediaStoreHelper.openOutputStream(destination)
                ?: error("Failed to open output stream")

            var downloadedBytes = 0L
            var lastReportedPercent = -1

            outputStream.use { out ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        if (cancelledTasks.contains(taskId)) {
                            cancelledTasks.remove(taskId)
                            error("Download was cancelled by user")
                        }
                        out.write(buffer, 0, read)
                        downloadedBytes += read

                        val percent = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt()
                        } else {
                            0
                        }

                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            currentTask = currentTask.copy(
                                totalBytes = totalBytes,
                                downloadedBytes = downloadedBytes,
                                progressPercent = percent
                            )
                            emit(currentTask)
                            notificationHelper.showProgress(notificationId, title, percent)
                        }
                    }
                    out.flush()
                }
            }

            mediaStoreHelper.finishPendingMedia(destination)
            currentTask = currentTask.copy(
                status = DownloadStatus.COMPLETED,
                destinationUri = destination.toString(),
                progressPercent = 100
            )
            emit(currentTask)
            notificationHelper.showComplete(notificationId, title, destination)
        } catch (e: Exception) {
            destinationUri?.let { mediaStoreHelper.deleteMedia(it) }
            val isCancelled = currentTask.status == DownloadStatus.CANCELLED || e.message?.contains("cancelled") == true
            val status = if (isCancelled) DownloadStatus.CANCELLED else DownloadStatus.FAILED
            currentTask = currentTask.copy(
                status = status,
                errorMessage = e.localizedMessage
            )
            emit(currentTask)
            if (isCancelled) {
                notificationHelper.cancel(notificationId)
            } else {
                notificationHelper.showFailed(notificationId, title)
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun cancelDownload(taskId: String) {
        cancelledTasks.add(taskId)
    }
}
