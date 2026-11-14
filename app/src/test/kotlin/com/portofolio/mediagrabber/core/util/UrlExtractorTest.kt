package com.portofolio.mediagrabber.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlExtractorTest {

    @Test
    fun extractsUrl_fromSimpleUrl() {
        val input = "https://www.tiktok.com/@user/video/123"
        assertEquals("https://www.tiktok.com/@user/video/123", UrlExtractor.extractUrl(input))
    }

    @Test
    fun extractsUrl_fromShareTextWithPrefixAndSuffix() {
        val input = "Watch this video: https://vt.tiktok.com/ZSNxxxxxx/ on TikTok!"
        assertEquals("https://vt.tiktok.com/ZSNxxxxxx/", UrlExtractor.extractUrl(input))
    }

    @Test
    fun extractsUrl_fromInstagramShareText() {
        val input = "Lihat postingan ini di Instagram https://www.instagram.com/reel/Cxxxxxx/"
        assertEquals("https://www.instagram.com/reel/Cxxxxxx/", UrlExtractor.extractUrl(input))
    }
}
