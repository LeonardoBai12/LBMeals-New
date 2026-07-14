package io.lb.lbmealsnew.feature.meals.presentation.details

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class YoutubeLinksTest {

    @Test
    fun `When the url is a watch link, expect the id after v`() {
        assertEquals("1IszT_guI08", youtubeVideoId("https://www.youtube.com/watch?v=1IszT_guI08"))
    }

    @Test
    fun `When the url has extra parameters, expect only the id`() {
        assertEquals("1IszT_guI08", youtubeVideoId("https://www.youtube.com/watch?v=1IszT_guI08&t=42s"))
        assertEquals("1IszT_guI08", youtubeVideoId("https://www.youtube.com/watch?feature=x&v=1IszT_guI08"))
    }

    @Test
    fun `When the url is a short link, expect the id from the path`() {
        assertEquals("1IszT_guI08", youtubeVideoId("https://youtu.be/1IszT_guI08"))
    }

    @Test
    fun `When the url is an embed link, expect the id from the path`() {
        assertEquals("1IszT_guI08", youtubeVideoId("https://www.youtube.com/embed/1IszT_guI08"))
    }

    @Test
    fun `When the url has no recognizable id, expect null`() {
        assertNull(youtubeVideoId("https://www.youtube.com/"))
    }

    @Test
    fun `When building the thumbnail url, expect the hqdefault of the video`() {
        assertEquals(
            "https://img.youtube.com/vi/1IszT_guI08/hqdefault.jpg",
            youtubeThumbnailUrl("1IszT_guI08"),
        )
    }
}
