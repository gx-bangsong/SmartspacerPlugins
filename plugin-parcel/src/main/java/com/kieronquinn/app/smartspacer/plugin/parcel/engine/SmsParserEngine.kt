package com.kieronquinn.app.smartspacer.plugin.parcel.engine

import android.content.Context
import java.util.regex.Pattern

class SmsParserEngine(private val context: Context) {
    private val ruleManager = RuleManager(context)

    suspend fun parse(text: String): ParseResult? {
        val rules = ruleManager.getEffectiveRules()
        for (rule in rules) {
            if (rule.matchKeywords.all { text.contains(it) }) {
                val pickupCode = extract(text, rule.extractionRules.pickupCodeRegex)
                if (pickupCode != null) {
                    val location = rule.extractionRules.locationRegex?.let { extract(text, it) }
                    return ParseResult(rule.provider, pickupCode, location)
                }
            }
        }
        return null
    }

    private fun extract(text: String, regex: String): String? {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            val result = if (matcher.groupCount() >= 1) {
                matcher.group(1)
            } else {
                matcher.group()
            }
            result?.trim()
        } else {
            null
        }
    }

    data class ParseResult(
        val provider: String,
        val pickupCode: String,
        val location: String?
    )
}
