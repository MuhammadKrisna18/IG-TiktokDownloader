package com.portofolio.mediagrabber.core.util

object UrlExtractor {
    private val urlRegex = Regex("(https?://[^\\s]+)")

    fun extractUrl(text: String): String {
        val trimmed = text.trim()
        val match = urlRegex.find(trimmed)
        return match?.value?.trimEnd('.', ',', '!', '?') ?: trimmed
    }
}
