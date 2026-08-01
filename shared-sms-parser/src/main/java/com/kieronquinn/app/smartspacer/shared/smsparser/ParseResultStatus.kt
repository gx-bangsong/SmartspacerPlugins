package com.kieronquinn.app.smartspacer.shared.smsparser

enum class ParseResultStatus {
    SUCCESS,
    NO_MATCH,
    MISSING_TRAIN_NUMBER,
    INVALID_DATE_FORMAT,
    STATION_NOT_RECOGNIZED
}
