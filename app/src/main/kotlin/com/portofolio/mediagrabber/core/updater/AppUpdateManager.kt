package com.portofolio.mediagrabber.core.updater

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class AppUpdateManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    suspend fun downloadAndInstallApk(
        downloadUrl: String,
        onProgress: (Int) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                error("HTTP error: ${response.code}")
            }
            val body = response.body ?: error("Empty response body")
            val totalBytes = body.contentLength()
            val apkFile = File(context.cacheDir, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            body.byteStream().use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var accumulated = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        accumulated += bytesRead
                        if (totalBytes > 0) {
                            val percent = ((accumulated * 100) / totalBytes).toInt()
                            onProgress(percent)
                        }
                    }
                    output.flush()
                }
            }

            installApk(apkFile)
        }
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
