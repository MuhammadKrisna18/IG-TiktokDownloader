package com.portofolio.mediagrabber.data.repository

import android.content.Context
import com.portofolio.mediagrabber.data.local.DownloadHistoryDbHelper
import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import com.portofolio.mediagrabber.domain.repository.DownloadHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class DownloadHistoryRepositoryImpl(
    private val dbHelper: DownloadHistoryDbHelper
) : DownloadHistoryRepository {

    private val _historyFlow = MutableStateFlow<List<DownloadedMedia>>(emptyList())

    init {
        refresh()
    }

    private fun refresh() {
        runCatching {
            _historyFlow.value = dbHelper.getAll()
        }
    }

    override fun getAllHistory(): Flow<List<DownloadedMedia>> {
        refresh()
        return _historyFlow.asStateFlow()
    }

    override suspend fun saveHistory(item: DownloadedMedia): Unit = withContext(Dispatchers.IO) {
        dbHelper.insert(item)
        refresh()
    }

    override suspend fun deleteHistory(id: String): Unit = withContext(Dispatchers.IO) {
        dbHelper.delete(id)
        refresh()
    }

    companion object {
        @Volatile
        private var instance: DownloadHistoryRepositoryImpl? = null

        fun getInstance(context: Context): DownloadHistoryRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: DownloadHistoryRepositoryImpl(
                    DownloadHistoryDbHelper(context.applicationContext)
                ).also { instance = it }
            }
        }
    }
}
