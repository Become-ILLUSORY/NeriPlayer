package moe.ouom.neriplayer.util

/*
 * NeriPlayer - A unified Android player for streaming music and videos from multiple online platforms.
 * Copyright (C) 2025-2025 NeriPlayer developers
 * https://github.com/cwuom/NeriPlayer
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 * File: moe.ouom.neriplayer.util/YouTubeDomainReplacer
 */

object YouTubeDomainReplacer {
    private val YOUTUBE_DOMAINS = listOf(
        "youtube.com",
        "www.youtube.com",
        "music.youtube.com",
        "youtu.be",
        "youtubei.googleapis.com"
    )

    fun replaceDomain(url: String, replacementDomain: String): String {
        if (replacementDomain.isBlank()) {
            return url
        }

        val normalizedReplacement = normalizeDomain(replacementDomain)

        var hasYouTubeDomain = false
        for (domain in YOUTUBE_DOMAINS) {
            if (url.contains(domain)) {
                hasYouTubeDomain = true
                break
            }
        }

        if (!hasYouTubeDomain) {
            return url
        }

        return "$normalizedReplacement/$url"
    }

    fun replaceOrigin(origin: String, replacementDomain: String): String {
        if (replacementDomain.isBlank()) {
            return origin
        }
        return normalizeDomain(replacementDomain)
    }

    private fun normalizeDomain(domain: String): String {
        var normalized = domain.trim()
        if (normalized.startsWith("http://")) {
            normalized = normalized.removePrefix("http://")
        }
        if (normalized.startsWith("https://")) {
            normalized = normalized.removePrefix("https://")
        }
        if (normalized.startsWith("www.")) {
            normalized = normalized.removePrefix("www.")
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.removeSuffix("/")
        }
        return normalized
    }

    fun isYouTubeDomain(host: String): Boolean {
        val normalizedHost = host.removePrefix("www.")
        return YOUTUBE_DOMAINS.any { domain ->
            normalizedHost == domain || normalizedHost.endsWith(".$domain")
        }
    }
}