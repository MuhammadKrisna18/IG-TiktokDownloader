package com.portofolio.mediagrabber.domain.repository

import com.portofolio.mediagrabber.core.result.Resource
import com.portofolio.mediagrabber.domain.model.MediaItem

interface MediaExtractorRepository {
    suspend fun extractMedia(url: String): Resource<MediaItem>
}
