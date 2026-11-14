package com.portofolio.mediagrabber.domain.usecase

import com.portofolio.mediagrabber.domain.model.PlatformType
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectPlatformUseCaseTest {

    private val useCase = DetectPlatformUseCase()

    @Test
    fun returnsTikTok_whenUrlIsTikTokStandard() {
        val url = "https://www.tiktok.com/@user/video/123456789"
        assertEquals(PlatformType.TIKTOK, useCase(url))
    }

    @Test
    fun returnsTikTok_whenUrlIsTikTokShort() {
        val url = "https://vt.tiktok.com/ZSNxxxxxx/"
        assertEquals(PlatformType.TIKTOK, useCase(url))
    }

    @Test
    fun returnsInstagram_whenUrlIsInstagramReel() {
        val url = "https://www.instagram.com/reel/Cxxxxxx/"
        assertEquals(PlatformType.INSTAGRAM, useCase(url))
    }

    @Test
    fun returnsInstagram_whenUrlIsInstagramPost() {
        val url = "https://instagr.am/p/Cxxxxxx/"
        assertEquals(PlatformType.INSTAGRAM, useCase(url))
    }

    @Test
    fun returnsUnknown_whenUrlIsUnsupported() {
        val url = "https://example.com/video"
        assertEquals(PlatformType.UNKNOWN, useCase(url))
    }
}
