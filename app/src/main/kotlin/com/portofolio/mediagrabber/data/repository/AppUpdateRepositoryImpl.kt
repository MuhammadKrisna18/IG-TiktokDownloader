package com.portofolio.mediagrabber.data.repository

import com.portofolio.mediagrabber.BuildConfig
import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.data.remote.api.GithubReleaseApiService
import com.portofolio.mediagrabber.domain.model.AppUpdateInfo
import com.portofolio.mediagrabber.domain.repository.AppUpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppUpdateRepositoryImpl(
    private val apiService: GithubReleaseApiService,
    private val owner: String = BuildConfig.GITHUB_REPO_OWNER,
    private val repo: String = BuildConfig.GITHUB_REPO_NAME,
    private val currentVersionName: String = BuildConfig.VERSION_NAME
) : AppUpdateRepository {

    override suspend fun checkLatestRelease(): Resource<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val release = apiService.getLatestRelease(owner = owner, repo = repo)
            val releaseTag = release.tagName?.removePrefix("v")?.trim() ?: ""
            val apkAsset = release.assets.firstOrNull { it.name?.endsWith(".apk", ignoreCase = true) == true }
            val downloadUrl = apkAsset?.browserDownloadUrl

            val isUpdateAvailable = isRemoteVersionNewer(currentVersionName, releaseTag)

            Resource.Success(
                AppUpdateInfo(
                    latestVersionName = release.tagName ?: currentVersionName,
                    releaseNotes = release.body ?: "",
                    apkDownloadUrl = downloadUrl,
                    isUpdateAvailable = isUpdateAvailable && !downloadUrl.isNullOrBlank()
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to check latest release", e)
        }
    }

    private fun isRemoteVersionNewer(current: String, remote: String): Boolean {
        if (remote.isBlank() || current == remote) return false
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val length = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until length) {
            val c = currentParts.getOrElse(i) { 0 }
            val r = remoteParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
