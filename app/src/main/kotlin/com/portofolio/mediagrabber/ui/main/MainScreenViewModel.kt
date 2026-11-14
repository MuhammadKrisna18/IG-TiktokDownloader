package com.portofolio.mediagrabber.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.data.remote.NetworkModule
import com.portofolio.mediagrabber.data.repository.AppUpdateRepositoryImpl
import com.portofolio.mediagrabber.data.repository.MediaExtractorRepositoryImpl
import com.portofolio.mediagrabber.domain.model.AppUpdateInfo
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.model.PlatformType
import com.portofolio.mediagrabber.domain.usecase.CheckAppUpdateUseCase
import com.portofolio.mediagrabber.domain.usecase.DetectPlatformUseCase
import com.portofolio.mediagrabber.domain.usecase.ExtractMediaUseCase
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
    val showUpdateDialog: Boolean = false
)

class MainScreenViewModel(
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
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

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
