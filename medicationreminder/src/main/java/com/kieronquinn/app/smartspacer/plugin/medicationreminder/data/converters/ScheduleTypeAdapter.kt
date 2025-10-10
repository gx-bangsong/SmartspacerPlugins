package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.converters

import com.google.gson.*
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Schedule
import java.lang.reflect.Type

class ScheduleTypeAdapter : JsonSerializer<Schedule>, JsonDeserializer<Schedule> {

    companion object {
        private const val TYPE = "type"
        private const val DATA = "data"
    }

    override fun serialize(src: Schedule?, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty(TYPE, src?.javaClass?.simpleName)
        jsonObject.add(DATA, context.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): Schedule {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get(TYPE).asString
        val data = jsonObject.get(DATA)
        val clazz = when (type) {
            "EveryXHours" -> Schedule.EveryXHours::class.java
            "SpecificTimes" -> Schedule.SpecificTimes::class.java
            "EveryXDays" -> Schedule.EveryXDays::class.java
            "SpecificDaysOfWeek" -> Schedule.SpecificDaysOfWeek::class.java
            else -> throw JsonParseException("Unknown schedule type: $type")
        }
        return context.deserialize(data, clazz)
    }
}