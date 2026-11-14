package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.PlatformType

class DetectPlatformUseCase {
    operator fun invoke(url: String): PlatformType {
        val trimmed = url.trim().lowercase()
        return when {
            trimmed.contains("tiktok.com") || trimmed.contains("douyin.com") -> PlatformType.TIKTOK
            trimmed.contains("instagram.com") || trimmed.contains("instagr.am") -> PlatformType.INSTAGRAM
            else -> PlatformType.UNKNOWN
        }
    }
}
