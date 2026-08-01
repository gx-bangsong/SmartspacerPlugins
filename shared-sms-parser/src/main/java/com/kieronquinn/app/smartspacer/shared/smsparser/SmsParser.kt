package com.kieronquinn.app.smartspacer.shared.smsparser

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.regex.Pattern

class SmsParser {
    private var rules: List<ParserRule> = emptyList()

    constructor(context: Context) {
        try {
            context.assets.open("travel_sms_rules.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<ParserRule>>() {}.type
                rules = Gson().fromJson(reader, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    constructor(rulesJson: String) {
        try {
            val type = object : TypeToken<List<ParserRule>>() {}.type
            rules = Gson().fromJson(rulesJson, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parseTravelInfo(rawText: String): TravelParseResult {
        if (rawText.isBlank()) {
            return TravelParseResult(ParseResultStatus.NO_MATCH, errorMessage = "输入文本为空")
        }

        for (rule in rules) {
            try {
                val pattern = Pattern.compile(rule.pattern)
                val matcher = pattern.matcher(rawText)
                if (matcher.find()) {
                    val mappings = rule.mappings

                    val trainNumberGroup = mappings["trainNumber"]
                    val trainNumber = if (trainNumberGroup != null && trainNumberGroup <= matcher.groupCount()) {
                        matcher.group(trainNumberGroup)
                    } else null

                    if (trainNumber.isNullOrEmpty()) {
                        return TravelParseResult(ParseResultStatus.MISSING_TRAIN_NUMBER, errorMessage = "未识别到车次或航班号")
                    }

                    val departureDateGroup = mappings["departureDate"]
                    val departureDate = if (departureDateGroup != null && departureDateGroup <= matcher.groupCount()) {
                        matcher.group(departureDateGroup)
                    } else null

                    val fullDateGroup = mappings["fullDate"]
                    val fullDate = if (fullDateGroup != null && fullDateGroup <= matcher.groupCount()) {
                        matcher.group(fullDateGroup)
                    } else null

                    val departureTimeGroup = mappings["departureTime"]
                    val departureTimeStr = if (departureTimeGroup != null && departureTimeGroup <= matcher.groupCount()) {
                        matcher.group(departureTimeGroup)
                    } else null

                    if (departureDate.isNullOrEmpty() && fullDate.isNullOrEmpty()) {
                        return TravelParseResult(ParseResultStatus.INVALID_DATE_FORMAT, errorMessage = "未识别到有效的日期格式")
                    }

                    val departureStationGroup = mappings["departureStation"]
                    val departureStation = if (departureStationGroup != null && departureStationGroup <= matcher.groupCount()) {
                        matcher.group(departureStationGroup) ?: ""
                    } else ""

                    val arrivalStationGroup = mappings["arrivalStation"]
                    val arrivalStation = if (arrivalStationGroup != null && arrivalStationGroup <= matcher.groupCount()) {
                        matcher.group(arrivalStationGroup)
                    } else null

                    val seatGroup = mappings["seat"]
                    val seat = if (seatGroup != null && seatGroup <= matcher.groupCount()) {
                        matcher.group(seatGroup)
                    } else null

                    val passengerNameGroup = mappings["passengerName"]
                    val passengerName = if (passengerNameGroup != null && passengerNameGroup <= matcher.groupCount()) {
                        matcher.group(passengerNameGroup)
                    } else null

                    val departureTimeMs = try {
                        parseDateTime(departureDate, fullDate, departureTimeStr)
                    } catch (e: Exception) {
                        return TravelParseResult(ParseResultStatus.INVALID_DATE_FORMAT, errorMessage = "时间解析失败: ${e.message}")
                    }

                    val travelInfo = TravelInfo(
                        trainNumber = trainNumber,
                        departureStation = departureStation.trim(),
                        arrivalStation = arrivalStation?.trim(),
                        departureTime = departureTimeMs,
                        seat = seat?.trim(),
                        passengerName = passengerName?.trim(),
                        rawText = rawText
                    )
                    return TravelParseResult(ParseResultStatus.SUCCESS, travelInfo)
                }
            } catch (e: Exception) {
                return TravelParseResult(ParseResultStatus.NO_MATCH, errorMessage = "解析正则出错: ${e.message}")
            }
        }

        return TravelParseResult(ParseResultStatus.NO_MATCH, errorMessage = "未匹配到任何解析规则")
    }

    private fun parseDateTime(dateStr: String?, fullDateStr: String?, timeStr: String?): Long {
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        val hourMinute = if (!timeStr.isNullOrEmpty() && timeStr.contains(":")) {
            val parts = timeStr.split(":")
            Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
        } else {
            Pair(0, 0)
        }

        if (!fullDateStr.isNullOrEmpty()) {
            val parts = fullDateStr.split("-")
            if (parts.size >= 3) {
                val y = parts[0].toIntOrNull() ?: currentYear
                val m = (parts[1].toIntOrNull() ?: 1) - 1
                val d = parts[2].toIntOrNull() ?: 1
                calendar.set(y, m, d, hourMinute.first, hourMinute.second, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                return calendar.timeInMillis
            }
        }

        if (!dateStr.isNullOrEmpty()) {
            val regexZh = "(\\d+)月(\\d+)日".toRegex()
            val matchZh = regexZh.find(dateStr)
            if (matchZh != null) {
                val m = matchZh.groupValues[1].toInt() - 1
                val d = matchZh.groupValues[2].toInt()

                calendar.set(java.util.Calendar.YEAR, currentYear)
                calendar.set(java.util.Calendar.MONTH, m)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, d)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hourMinute.first)
                calendar.set(java.util.Calendar.MINUTE, hourMinute.second)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)

                val now = System.currentTimeMillis()
                if (calendar.timeInMillis < now - 30L * 24 * 60 * 60 * 1000) {
                    calendar.set(java.util.Calendar.YEAR, currentYear + 1)
                }
                return calendar.timeInMillis
            }
        }

        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourMinute.first)
        calendar.set(java.util.Calendar.MINUTE, hourMinute.second)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
