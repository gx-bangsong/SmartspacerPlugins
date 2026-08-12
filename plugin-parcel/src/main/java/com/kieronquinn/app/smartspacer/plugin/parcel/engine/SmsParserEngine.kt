package com.kieronquinn.app.smartspacer.plugin.parcel.engine

import android.content.Context
import java.util.regex.Pattern

class SmsParserEngine(private val context: Context) {
    private val ruleManager = RuleManager(context)

    suspend fun parse(text: String): ParseResult? {
        val rules = ruleManager.getEffectiveRules()
        for (rule in rules) {
            // 每个关键词项默认是必须同时满足的条件；用 | 可在同一项中声明多个等价关键词。
            // 例如通用规则可同时匹配“取件码”“取货码”和“取件密码”。
            if (rule.matchKeywords.all { keyword ->
                    keyword.split("|").any { alternative -> text.contains(alternative) }
                }) {
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
