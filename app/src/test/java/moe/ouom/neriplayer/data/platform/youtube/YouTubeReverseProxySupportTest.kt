package moe.ouom.neriplayer.data.platform.youtube

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeReverseProxySupportTest {

    @Test
    fun normalizeYouTubeReverseProxyBaseUrl_trimsTrailingSlash() {
        assertEquals(
            "https://proxy.api.030101.xyz/base",
            normalizeYouTubeReverseProxyBaseUrl(" https://proxy.api.030101.xyz/base/ ")
        )
    }

    @Test
    fun normalizeYouTubeReverseProxyBaseUrl_rejectsQueryParameters() {
        assertNull(
            normalizeYouTubeReverseProxyBaseUrl(
                "https://proxy.api.030101.xyz/base?token=123"
            )
        )
    }

    @Test
    fun isYouTubeReverseProxyCandidateHost_matchesYouTubeImageAndMediaHosts() {
        assertTrue(isYouTubeReverseProxyCandidateHost("music.youtube.com"))
        assertTrue(isYouTubeReverseProxyCandidateHost("rr1---sn.googlevideo.com"))
        assertTrue(isYouTubeReverseProxyCandidateHost("i.ytimg.com"))
        assertTrue(isYouTubeReverseProxyCandidateHost("lh3.googleusercontent.com"))
        assertTrue(isYouTubeReverseProxyCandidateHost("yt3.ggpht.com"))
        assertFalse(isYouTubeReverseProxyCandidateHost("accounts.google.com"))
    }

    @Test
    fun rewriteYouTubeRequestUrl_rewritesTrustedYouTubeHostsToProxyPath() {
        val rewritten = rewriteYouTubeRequestUrl(
            originalUrl = "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"
                .toHttpUrl(),
            proxyBaseUrl = "https://proxy.api.030101.xyz"
        )

        val resolved = requireNotNull(rewritten)
        assertEquals(
            "https://proxy.api.030101.xyz/https://music.youtube.com/youtubei/v1/search?prettyPrint=false",
            resolved.toString()
        )
    }

    @Test
    fun rewriteYouTubeRequestUrl_rewritesYtimgHostsToProxyPath() {
        val rewritten = rewriteYouTubeRequestUrl(
            originalUrl = "https://i.ytimg.com/vi/song-video-id/hqdefault.jpg".toHttpUrl(),
            proxyBaseUrl = "https://proxy.api.030101.xyz"
        )

        val resolved = requireNotNull(rewritten)
        assertEquals(
            "https://proxy.api.030101.xyz/https://i.ytimg.com/vi/song-video-id/hqdefault.jpg",
            resolved.toString()
        )
    }

    @Test
    fun rewriteYouTubeRequestUrl_rewritesGoogleusercontentHostsToProxyPath() {
        val rewritten = rewriteYouTubeRequestUrl(
            originalUrl = "https://lh3.googleusercontent.com/abc123=w1200-h1200".toHttpUrl(),
            proxyBaseUrl = "https://proxy.api.030101.xyz"
        )

        val resolved = requireNotNull(rewritten)
        assertEquals(
            "https://proxy.api.030101.xyz/https://lh3.googleusercontent.com/abc123=w1200-h1200",
            resolved.toString()
        )
    }

    @Test
    fun rewriteYouTubeRequestUrl_skipsAlreadyProxiedHost() {
        val rewritten = rewriteYouTubeRequestUrl(
            originalUrl = "https://proxy.api.030101.xyz/https://music.youtube.com/".toHttpUrl(),
            proxyBaseUrl = "https://proxy.api.030101.xyz",
            proxyHost = "proxy.api.030101.xyz"
        )

        assertNull(rewritten)
    }
}
