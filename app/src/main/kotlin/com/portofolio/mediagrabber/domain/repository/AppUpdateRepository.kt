package com.portofolio.mediagrabber.domain.repository

import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.domain.model.AppUpdateInfo

interface AppUpdateRepository {
    suspend fun checkLatestRelease(): Resource<AppUpdateInfo>
}
