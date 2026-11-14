package com.portofolio.mediagrabber.domain.model

data class AppUpdateInfo(
    val latestVersionName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?,
    val isUpdateAvailable: Boolean
)
