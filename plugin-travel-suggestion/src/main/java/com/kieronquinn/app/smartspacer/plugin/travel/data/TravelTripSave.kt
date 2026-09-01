package com.kieronquinn.app.smartspacer.plugin.travel.data

/**
 * Room `@Insert` with autoGenerate returns the new row id. Callers must copy that id onto the
 * in-memory object before scheduling alarms or posting notifications — an item with `id = 0`
 * would collide across trips and cancel the wrong reminder.
 */
object TravelTripSave {
    fun afterInsert(item: TravelInfoItem, insertedId: Long): TravelInfoItem {
        require(insertedId > 0L) { "Room insert must return a real row id, was $insertedId" }
        return item.copy(id = insertedId.toInt())
    }
}
