package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

/**
 * Text shown in the promoted Live Update status chip.
 * Must be the pickup code only — never a label like "取件码：" / "Pickup code:".
 */
object ParcelLiveUpdateCapsule {

    fun text(pickupCode: String): String {
        return pickupCode
            .replace(PREFIX, "")
            .replace(WHITESPACE, "")
            .trim()
    }

    private val PREFIX = Regex(
        "^(取件码|取货码|取件密码|Pickup\\s*code)\\s*[:：]?",
        setOf(RegexOption.IGNORE_CASE)
    )
    private val WHITESPACE = Regex("\\s+")
}
