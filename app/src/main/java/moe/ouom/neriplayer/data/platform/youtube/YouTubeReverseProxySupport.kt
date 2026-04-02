package moe.ouom.neriplayer.data.platform.youtube

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
 * File: moe.ouom.neriplayer.data.platform.youtube/YouTubeReverseProxySupport
 * Updated: 2026/4/2
 */

import java.util.Locale
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

fun normalizeYouTubeReverseProxyBaseUrl(candidate: String?): String? {
    val trimmed = candidate?.trim().orEmpty()
    if (trimmed.isBlank()) {
        return null
    }
    val parsed = runCatching { trimmed.toHttpUrl() }.getOrNull() ?: return null
    if (!parsed.scheme.equals("https", ignoreCase = true) &&
        !parsed.scheme.equals("http", ignoreCase = true)
    ) {
        return null
    }
    if (parsed.query != null || parsed.fragment != null) {
        return null
    }
    return parsed.newBuilder()
        .query(null)
        .fragment(null)
        .build()
        .toString()
        .removeSuffix("/")
}

fun isYouTubeReverseProxyCandidateHost(host: String?): Boolean {
    return isTrustedYouTubeHost(host)
}

fun buildYouTubeReverseProxyUrl(
    originalUrl: String,
    proxyBaseUrl: String
): HttpUrl? {
    val normalizedBaseUrl = normalizeYouTubeReverseProxyBaseUrl(proxyBaseUrl) ?: return null
    return runCatching {
        "$normalizedBaseUrl/$originalUrl".toHttpUrl()
    }.getOrNull()
}

fun rewriteYouTubeRequestUrl(
    originalUrl: HttpUrl,
    proxyBaseUrl: String,
    proxyHost: String? = null
): HttpUrl? {
    val normalizedOriginalHost = normalizeYouTubeHost(originalUrl.host)
    if (!isYouTubeReverseProxyCandidateHost(normalizedOriginalHost)) {
        return null
    }

    val normalizedProxyHost = normalizeYouTubeHost(proxyHost).ifBlank {
        normalizeYouTubeHost(
            normalizeYouTubeReverseProxyBaseUrl(proxyBaseUrl)
                ?.let { runCatching { it.toHttpUrl() }.getOrNull()?.host }
        )
    }
    if (normalizedProxyHost.isNotBlank() && normalizedOriginalHost == normalizedProxyHost) {
        return null
    }

    return buildYouTubeReverseProxyUrl(
        originalUrl = originalUrl.toString(),
        proxyBaseUrl = proxyBaseUrl
    )
}

object YouTubeReverseProxyRuntime {
    @Volatile
    private var reverseProxyEnabled: Boolean = false

    @Volatile
    private var normalizedBaseUrl: String = ""

    @Volatile
    private var normalizedProxyHost: String = ""

    fun update(enabled: Boolean, baseUrl: String?) {
        val resolvedBaseUrl = normalizeYouTubeReverseProxyBaseUrl(baseUrl).orEmpty()
        reverseProxyEnabled = enabled
        normalizedBaseUrl = resolvedBaseUrl
        normalizedProxyHost = runCatching { resolvedBaseUrl.toHttpUrl().host }
            .getOrNull()
            ?.lowercase(Locale.US)
            .orEmpty()
    }

    fun isEnabled(): Boolean = reverseProxyEnabled

    fun isActive(): Boolean = reverseProxyEnabled && normalizedBaseUrl.isNotBlank()

    fun baseUrl(): String = normalizedBaseUrl

    fun proxyHost(): String = normalizedProxyHost

    fun rewrite(url: HttpUrl): HttpUrl? {
        if (!isActive()) {
            return null
        }
        return rewriteYouTubeRequestUrl(
            originalUrl = url,
            proxyBaseUrl = normalizedBaseUrl,
            proxyHost = normalizedProxyHost
        )
    }
}
