package io.lb.lbmealsnew.feature.meals.presentation.details

/**
 * Extracts the video id from the common YouTube URL shapes
 * (`watch?v=`, `youtu.be/` and `/embed/`).
 */
internal fun youtubeVideoId(url: String): String? = YOUTUBE_ID_PATTERNS
    .firstNotNullOfOrNull { it.find(url)?.groupValues?.get(1) }

/**
 * The video's thumbnail, served by YouTube for every video with no API key.
 * `hqdefault` is a letterboxed 4:3 image, so renderers should crop it to
 * 16:9 — that removes the black bars exactly.
 */
internal fun youtubeThumbnailUrl(videoId: String): String =
    "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

private val YOUTUBE_ID_PATTERNS = listOf(
    Regex("""[?&]v=([A-Za-z0-9_-]+)"""),
    Regex("""youtu\.be/([A-Za-z0-9_-]+)"""),
    Regex("""/embed/([A-Za-z0-9_-]+)"""),
)
