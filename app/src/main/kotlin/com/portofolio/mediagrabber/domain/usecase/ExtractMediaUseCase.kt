package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.domain.model.MediaItem
import com.portofolio.mediagrabber.domain.repository.MediaExtractorRepository

class ExtractMediaUseCase(
    private val repository: MediaExtractorRepository
) {
    suspend operator fun invoke(url: String): Resource<MediaItem> {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isBlank()) {
            return Resource.Error("URL cannot be empty")
        }
        return repository.extractMedia(sanitizedUrl)
    }
}
