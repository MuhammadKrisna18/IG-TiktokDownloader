package com.portofolio.mediagrabber.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.portofolio.mediagrabber.core.notification.DownloadNotificationHelper
import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.data.remote.NetworkModule
import com.portofolio.mediagrabber.data.repository.AppUpdateRepositoryImpl
import com.portofolio.mediagrabber.data.repository.MediaDownloaderRepositoryImpl
import com.portofolio.mediagrabber.data.repository.MediaExtractorRepositoryImpl
import com.portofolio.mediagrabber.data.storage.MediaStoreHelper
import com.portofolio.mediagrabber.domain.model.AppUpdateInfo
import com.portofolio.mediagrabber.domain.model.DownloadOption
import com.portofolio.mediagrabber.domain.model.DownloadStatus
import com.portofolio.mediagrabber.domain.model.DownloadTask
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.usecase.CheckAppUpdateUseCase
import com.portofolio.mediagrabber.domain.usecase.DetectPlatformUseCase
import com.portofolio.mediagrabber.domain.usecase.DownloadMediaUseCase
import com.portofolio.mediagrabber.domain.usecase.ExtractMediaUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val inputUrl: String = "",
    val detectedPlatform: PlatformType = PlatformType.UNKNOWN,
    val isLoading: Boolean = false,
    val mediaItem: MediaItem? = null,
    val errorMessage: String? = null,
    val updateInfo: AppUpdateInfo? = null,
    val showUpdateDialog: Boolean = false,
    val currentDownload: DownloadTask? = null
)

class MainScreenViewModel(
    application: Application,
    private val extractMediaUseCase: ExtractMediaUseCase = ExtractMediaUseCase(
        MediaExtractorRepositoryImpl(
            tikWmApiService = NetworkModule.tikWmApiService,
            cobaltApiService = NetworkModule.cobaltApiService
        )
    ),
    private val detectPlatformUseCase: DetectPlatformUseCase = DetectPlatformUseCase(),
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase = CheckAppUpdateUseCase(
        AppUpdateRepositoryImpl(
            apiService = NetworkModule.githubReleaseApiService
        )
    ),
    private val downloadMediaUseCase: DownloadMediaUseCase = DownloadMediaUseCase(
        MediaDownloaderRepositoryImpl(
            okHttpClient = NetworkModule.okHttpClient,
            mediaStoreHelper = MediaStoreHelper(application.applicationContext),
            notificationHelper = DownloadNotificationHelper(application.applicationContext)
        )
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    init {
        checkForUpdate()
    }

    fun onUrlChanged(url: String) {
        val platform = detectPlatformUseCase(url)
        _uiState.update {
            it.copy(
                inputUrl = url,
                detectedPlatform = platform,
                errorMessage = null
            )
        }
    }

    fun onClearUrl() {
        _uiState.update {
            it.copy(
                inputUrl = "",
                detectedPlatform = PlatformType.UNKNOWN,
                mediaItem = null,
                errorMessage = null
            )
        }
    }

    fun fetchMedia() {
        val currentUrl = _uiState.value.inputUrl.trim()
        if (currentUrl.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = extractMediaUseCase(currentUrl)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mediaItem = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun startDownload(option: DownloadOption) {
        val media = _uiState.value.mediaItem ?: return
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            downloadMediaUseCase(media, option).collect { task ->
                _uiState.update { it.copy(currentDownload = task) }
            }
        }
    }

    fun cancelDownload() {
        val taskId = _uiState.value.currentDownload?.id
        if (taskId != null) {
            downloadMediaUseCase.cancel(taskId)
        }
        downloadJob?.cancel()
        _uiState.update {
            it.copy(
                currentDownload = it.currentDownload?.copy(
                    status = DownloadStatus.CANCELLED
                )
            )
        }
    }

    fun dismissDownloadProgress() {
        _uiState.update { it.copy(currentDownload = null) }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            val result = checkAppUpdateUseCase()
            if (result is Resource.Success && result.data.isUpdateAvailable) {
                _uiState.update {
                    it.copy(
                        updateInfo = result.data,
                        showUpdateDialog = true
                    )
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }
}
