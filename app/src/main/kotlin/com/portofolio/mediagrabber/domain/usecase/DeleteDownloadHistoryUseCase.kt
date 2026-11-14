package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.repository.DownloadHistoryRepository

class DeleteDownloadHistoryUseCase(
    private val repository: DownloadHistoryRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteHistory(id)
    }
}
