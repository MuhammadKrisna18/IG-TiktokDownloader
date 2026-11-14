package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import com.portofolio.mediagrabber.domain.repository.DownloadHistoryRepository

class SaveDownloadHistoryUseCase(
    private val repository: DownloadHistoryRepository
) {
    suspend operator fun invoke(item: DownloadedMedia) {
        repository.saveHistory(item)
    }
}
