package com.kieronquinn.app.smartspacer.plugin.parcel.engine

import android.content.Context
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.parcel.data.RuleDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.RuleItem
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RuleManager(private val context: Context) : KoinComponent {
    private val ruleDao by inject<RuleDao>()
    private val gson = Gson()

    suspend fun getEffectiveRules(): List<ParcelRule> {
        val dbRules = ruleDao.getAllRules().first().map {
            ParcelRule(
                provider = it.provider,
                priority = it.priority,
                matchKeywords = it.matchKeywords.split(",").map { kw -> kw.trim() },
                extractionRules = ExtractionRules(it.pickupCodeRegex, it.locationRegex)
            )
        }

        if (dbRules.isEmpty()) {
            // 只有默认规则确实加载成功后才重新读取；加载失败（如内置 JSON 解析异常）时
            // 直接返回空列表，避免无限递归导致 StackOverflowError
            if (loadDefaultRulesIntoDb()) {
                return getEffectiveRules()
            }
            return emptyList()
        }

        return dbRules
    }

    private suspend fun loadDefaultRulesIntoDb(): Boolean {
        return try {
            val json = context.assets.open("rules/default_rules.json").bufferedReader().use { it.readText() }
            val config = gson.fromJson(json, ParsingEngineConfig::class.java) ?: return false
            if (config.rules.isEmpty()) return false
            val ruleItems = config.rules.map {
                RuleItem(
                    provider = it.provider,
                    priority = it.priority,
                    matchKeywords = it.matchKeywords.joinToString(","),
                    pickupCodeRegex = it.extractionRules.pickupCodeRegex,
                    locationRegex = it.extractionRules.locationRegex,
                    isCustom = false
                )
            }
            ruleDao.insertRules(ruleItems)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importRulesFromJson(json: String): Boolean {
        return try {
            val config = gson.fromJson(json, ParsingEngineConfig::class.java)
            val ruleItems = config.rules.map {
                RuleItem(
                    provider = it.provider,
                    priority = it.priority,
                    matchKeywords = it.matchKeywords.joinToString(","),
                    pickupCodeRegex = it.extractionRules.pickupCodeRegex,
                    locationRegex = it.extractionRules.locationRegex,
                    isCustom = true
                )
            }
            ruleDao.insertRules(ruleItems)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
