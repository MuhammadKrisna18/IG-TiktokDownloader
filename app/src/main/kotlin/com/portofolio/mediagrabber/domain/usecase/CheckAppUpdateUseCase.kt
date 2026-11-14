package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.domain.model.AppUpdateInfo
import com.portofolio.mediagrabber.domain.repository.AppUpdateRepository

class CheckAppUpdateUseCase(
    private val repository: AppUpdateRepository
) {
    suspend operator fun invoke(): Resource<AppUpdateInfo> {
        return repository.checkLatestRelease()
    }
}
