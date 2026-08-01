package com.kieronquinn.app.smartspacer.shared.smsparser

data class ParserRule(
    val id: String,
    val pattern: String,
    val mappings: Map<String, Int>
)
