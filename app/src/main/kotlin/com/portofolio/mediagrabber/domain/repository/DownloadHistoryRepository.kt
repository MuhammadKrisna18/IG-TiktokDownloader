package com.portofolio.mediagrabber.domain.repository

import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import kotlinx.coroutines.flow.Flow

interface DownloadHistoryRepository {
    fun getAllHistory(): Flow<List<DownloadedMedia>>
    suspend fun saveHistory(item: DownloadedMedia)
    suspend fun deleteHistory(id: String)
}
