package com.kieronquinn.app.smartspacer.plugin.travel.logic

import android.content.Intent

/**
 * Result of extracting the share payload from an ACTION_SEND intent.
 */
sealed class ShareTextResult {
    data class Success(val text: String) : ShareTextResult()
    object Empty : ShareTextResult()
    object WrongMimeType : ShareTextResult()
    object TooLong : ShareTextResult()
}

/**
 * Pure extraction logic for the "解析出行信息" (parse travel info) sharesheet entry.
 *
 * Kept free of Android framework objects so it is unit-testable on the JVM; the activity maps
 * `Intent.getCharSequenceExtra` / `ClipData` values into the plain inputs below.
 *
 * Behaviour:
 *  - Only `ACTION_SEND` shares are accepted; anything else is rejected (not reachable via the
 *    intent filter, but defensive).
 *  - `text/plain` is the primary MIME type; `text/*` subtypes are tolerated.
 *  - `EXTRA_TEXT` is preferred, `ClipData` text is the fallback.
 *  - Blank text, non-text MIME types and oversized payloads fail safely without exceptions.
 */
object TravelShareTextExtractor {

    const val MAX_SHARE_TEXT_LENGTH = 10_000

    fun extract(
        action: String?,
        mimeType: String?,
        extraText: String?,
        clipText: String?,
        maxLength: Int = MAX_SHARE_TEXT_LENGTH
    ): ShareTextResult {
        if (action != Intent.ACTION_SEND) return ShareTextResult.Empty
        if (mimeType != null && !mimeType.startsWith("text/")) {
            return ShareTextResult.WrongMimeType
        }
        val candidate = extraText?.trim().orEmpty().ifBlank { clipText?.trim().orEmpty() }
        if (candidate.isBlank()) return ShareTextResult.Empty
        if (candidate.length > maxLength) return ShareTextResult.TooLong
        return ShareTextResult.Success(candidate)
    }
}
