package com.portofolio.mediagrabber.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.portofolio.mediagrabber.core.notification.DownloadNotificationHelper
import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.core.updater.AppUpdateManager
import com.portofolio.mediagrabber.data.local.DownloadHistoryDbHelper
import com.portofolio.mediagrabber.data.remote.NetworkModule
import com.portofolio.mediagrabber.data.repository.AppUpdateRepositoryImpl
import com.portofolio.mediagrabber.data.repository.DownloadHistoryRepositoryImpl
import com.portofolio.mediagrabber.data.repository.MediaDownloaderRepositoryImpl
import com.portofolio.mediagrabber.data.repository.MediaExtractorRepositoryImpl
import com.portofolio.mediagrabber.data.storage.MediaStoreHelper
import com.portofolio.mediagrabber.domain.model.AppUpdateInfo
import com.portofolio.mediagrabber.domain.model.DownloadOption
import com.portofolio.mediagrabber.domain.model.DownloadStatus
import com.portofolio.mediagrabber.domain.model.DownloadTask
import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.usecase.CheckAppUpdateUseCase
import com.portofolio.mediagrabber.domain.usecase.DeleteDownloadHistoryUseCase
import com.portofolio.mediagrabber.domain.usecase.DetectPlatformUseCase
import com.portofolio.mediagrabber.domain.usecase.DownloadMediaUseCase
import com.portofolio.mediagrabber.domain.usecase.ExtractMediaUseCase
import com.portofolio.mediagrabber.domain.usecase.GetDownloadHistoryUseCase
import com.portofolio.mediagrabber.domain.usecase.SaveDownloadHistoryUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedTab: Int = 0,
    val inputUrl: String = "",
    val detectedPlatform: PlatformType = PlatformType.UNKNOWN,
    val isLoading: Boolean = false,
    val mediaItem: MediaItem? = null,
    val errorMessage: String? = null,
    val updateInfo: AppUpdateInfo? = null,
    val showUpdateDialog: Boolean = false,
    val isUpdatingApp: Boolean = false,
    val updateProgress: Int = 0,
    val clipboardCandidateUrl: String? = null,
    val showClipboardDialog: Boolean = false,
    val currentDownload: DownloadTask? = null,
    val historyList: List<DownloadedMedia> = emptyList(),
    val activePlayingMedia: DownloadedMedia? = null
)

class MainScreenViewModel(
    application: Application,
    private val extractMediaUseCase: ExtractMediaUseCase,
    private val detectPlatformUseCase: DetectPlatformUseCase,
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase,
    private val downloadMediaUseCase: DownloadMediaUseCase,
    private val getDownloadHistoryUseCase: GetDownloadHistoryUseCase,
    private val saveDownloadHistoryUseCase: SaveDownloadHistoryUseCase,
    private val deleteDownloadHistoryUseCase: DeleteDownloadHistoryUseCase,
    private val appUpdateManager: AppUpdateManager
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        extractMediaUseCase = ExtractMediaUseCase(
            MediaExtractorRepositoryImpl(
                tikWmApiService = NetworkModule.tikWmApiService,
                cobaltApiService = NetworkModule.cobaltApiService
            )
        ),
        detectPlatformUseCase = DetectPlatformUseCase(),
        checkAppUpdateUseCase = CheckAppUpdateUseCase(
            AppUpdateRepositoryImpl(
                apiService = NetworkModule.githubReleaseApiService
            )
        ),
        downloadMediaUseCase = DownloadMediaUseCase(
            MediaDownloaderRepositoryImpl(
                okHttpClient = NetworkModule.okHttpClient,
                mediaStoreHelper = MediaStoreHelper(application.applicationContext),
                notificationHelper = DownloadNotificationHelper(application.applicationContext)
            )
        ),
        getDownloadHistoryUseCase = GetDownloadHistoryUseCase(
            DownloadHistoryRepositoryImpl.getInstance(application.applicationContext)
        ),
        saveDownloadHistoryUseCase = SaveDownloadHistoryUseCase(
            DownloadHistoryRepositoryImpl.getInstance(application.applicationContext)
        ),
        deleteDownloadHistoryUseCase = DeleteDownloadHistoryUseCase(
            DownloadHistoryRepositoryImpl.getInstance(application.applicationContext)
        ),
        appUpdateManager = AppUpdateManager(
            application.applicationContext,
            NetworkModule.okHttpClient
        )
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MainScreenViewModel(app)
            }
        }
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null
    private var lastHandledClipboard: String? = null

    init {
        checkForUpdate()
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            getDownloadHistoryUseCase().collect { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
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

    fun onClipboardUrlDetected(rawClipboardText: String) {
        val trimmed = rawClipboardText.trim()
        val platform = detectPlatformUseCase(trimmed)
        if (platform != PlatformType.UNKNOWN && trimmed != lastHandledClipboard && trimmed != _uiState.value.inputUrl) {
            lastHandledClipboard = trimmed
            _uiState.update {
                it.copy(
                    clipboardCandidateUrl = trimmed,
                    showClipboardDialog = true
                )
            }
        }
    }

    fun onAcceptClipboardUrl() {
        val candidate = _uiState.value.clipboardCandidateUrl ?: return
        _uiState.update {
            it.copy(
                inputUrl = candidate,
                detectedPlatform = detectPlatformUseCase(candidate),
                showClipboardDialog = false,
                clipboardCandidateUrl = null
            )
        }
        fetchMedia()
    }

    fun onDismissClipboardDialog() {
        _uiState.update {
            it.copy(
                showClipboardDialog = false,
                clipboardCandidateUrl = null
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
                if (task.status == DownloadStatus.COMPLETED && task.destinationUri != null) {
                    saveDownloadHistoryUseCase(
                        DownloadedMedia(
                            id = task.id,
                            title = media.title,
                            author = media.author,
                            coverUrl = media.coverUrl,
                            platform = media.platform,
                            contentUri = task.destinationUri,
                            filename = task.filename
                        )
                    )
                }
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

    fun playVideo(media: DownloadedMedia) {
        _uiState.update { it.copy(activePlayingMedia = media) }
    }

    fun dismissPlayer() {
        _uiState.update { it.copy(activePlayingMedia = null) }
    }

    fun deleteDownloadedMedia(id: String) {
        viewModelScope.launch {
            deleteDownloadHistoryUseCase(id)
        }
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

    fun startAppUpdate() {
        val downloadUrl = _uiState.value.updateInfo?.apkDownloadUrl ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingApp = true, updateProgress = 0) }
            val result = appUpdateManager.downloadAndInstallApk(
                downloadUrl = downloadUrl,
                onProgress = { progress ->
                    _uiState.update { it.copy(updateProgress = progress) }
                }
            )
            result.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUpdatingApp = false,
                        errorMessage = error.localizedMessage
                    )
                }
            }
            _uiState.update { it.copy(isUpdatingApp = false, showUpdateDialog = false) }
        }
    }
}
