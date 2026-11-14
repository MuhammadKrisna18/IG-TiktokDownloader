package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import com.portofolio.mediagrabber.domain.repository.DownloadHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetDownloadHistoryUseCase(
    private val repository: DownloadHistoryRepository
) {
    operator fun invoke(): Flow<List<DownloadedMedia>> {
        return repository.getAllHistory()
    }
}
