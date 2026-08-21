package com.kieronquinn.app.smartspacer.plugin.travel.logic

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelShareTextExtractorTest {

    private val sampleText = "【12306】邓棚焕购票成功，2月28日G5507次，南宁东站16:28开。"

    @Test
    fun `accepts ACTION_SEND with text-slash-plain and EXTRA_TEXT`() {
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/plain",
            extraText = sampleText,
            clipText = null
        )
        assertTrue(result is ShareTextResult.Success)
        assertEquals(sampleText, (result as ShareTextResult.Success).text)
    }

    @Test
    fun `prefers EXTRA_TEXT over ClipData`() {
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/plain",
            extraText = "from-extra",
            clipText = "from-clip"
        )
        assertEquals("from-extra", (result as ShareTextResult.Success).text)
    }

    @Test
    fun `falls back to ClipData text when EXTRA_TEXT is missing`() {
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/plain",
            extraText = null,
            clipText = "from-clip"
        )
        assertEquals("from-clip", (result as ShareTextResult.Success).text)
    }

    @Test
    fun `empty text fails safely`() {
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/plain",
            extraText = "   ",
            clipText = null
        )
        assertEquals(ShareTextResult.Empty, result)
    }

    @Test
    fun `non-text mime type is rejected`() {
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "image/png",
            extraText = sampleText,
            clipText = null
        )
        assertEquals(ShareTextResult.WrongMimeType, result)
    }

    @Test
    fun `oversized payload fails safely instead of blocking the main thread`() {
        val huge = "a".repeat(TravelShareTextExtractor.MAX_SHARE_TEXT_LENGTH + 1)
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/plain",
            extraText = huge,
            clipText = null
        )
        assertEquals(ShareTextResult.TooLong, result)
    }

    @Test
    fun `exactly at the limit is accepted`() {
        val exactly = "b".repeat(TravelShareTextExtractor.MAX_SHARE_TEXT_LENGTH)
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/plain",
            extraText = exactly,
            clipText = null
        )
        assertTrue(result is ShareTextResult.Success)
    }

    @Test
    fun `non-SEND action is rejected even with a text payload`() {
        val result = TravelShareTextExtractor.extract(
            action = "android.intent.action.VIEW",
            mimeType = "text/plain",
            extraText = sampleText,
            clipText = null
        )
        assertEquals(ShareTextResult.Empty, result)
    }

    @Test
    fun `tolerates other text subtypes`() {
        val result = TravelShareTextExtractor.extract(
            action = Intent.ACTION_SEND,
            mimeType = "text/x-vcard",
            extraText = sampleText,
            clipText = null
        )
        assertTrue(result is ShareTextResult.Success)
    }
}
