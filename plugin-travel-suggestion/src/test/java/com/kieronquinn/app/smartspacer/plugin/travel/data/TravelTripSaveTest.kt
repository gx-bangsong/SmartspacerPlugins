package com.kieronquinn.app.smartspacer.plugin.travel.data

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmCompat
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSchedulerImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelTripSaveTest {

    private fun trip(id: Int = 0) = TravelInfoItem(
        id = id,
        trainNumber = "G123",
        departureStation = "Beijing",
        arrivalStation = "Shanghai",
        departureTime = System.currentTimeMillis() + 3_600_000L,
        seat = "05A",
        passengerName = null,
        source = "manual"
    )

    @Test
    fun `room insert id is copied onto the scheduled item and is never zero`() {
        val original = trip(id = 0)
        val saved = TravelTripSave.afterInsert(original, insertedId = 42L)
        assertEquals(42, saved.id)
        assertNotEquals(0, saved.id)
        assertEquals(original.trainNumber, saved.trainNumber)
    }

    @Test
    fun `manual share and sms save paths schedule the non-zero id`() = runBlocking {
        val scheduledIds = mutableListOf<Int>()
        val scheduler = object : TravelScheduler {
            override fun hasPermission() = true
            override fun scheduleReminder(item: TravelInfoItem) {
                scheduledIds.add(item.id)
            }
            override fun cancelReminder(itemId: Int) = Unit
            override suspend fun rescheduleAll() = Unit
        }

        suspend fun persist(source: String, insertedId: Long) {
            val item = trip().copy(source = source)
            val saved = TravelTripSave.afterInsert(item, insertedId)
            scheduler.scheduleReminder(saved)
        }

        persist("manual", 11L)
        persist("share", 22L)
        persist("sms", 33L)

        assertEquals(listOf(11, 22, 33), scheduledIds)
        assertTrue(scheduledIds.none { it == 0 })
    }

    @Test
    fun `planAlarms uses the real item id and inexact fallback when exact alarms are denied`() {
        val item = trip(id = 7)
        val now = item.departureTime - 2 * TravelSchedulerImpl.DEPARTURE_WINDOW_MS
        val denied = TravelSchedulerImpl.planAlarms(item, now, hasExactPermission = false)
        assertTrue(denied.isNotEmpty())
        assertTrue(denied.all { it.itemId == 7 })
        assertTrue(denied.all { it.path == ExactAlarmCompat.Path.INEXACT_FALLBACK })

        val granted = TravelSchedulerImpl.planAlarms(item, now, hasExactPermission = true)
        assertTrue(granted.all { it.path == ExactAlarmCompat.Path.EXACT })
        assertTrue(granted.all { it.itemId != 0 })
    }

    @Test
    fun `used trips are not scheduled even when exact alarms are granted`() {
        val item = trip(id = 9).copy(isUsed = true)
        val now = item.departureTime - 2 * TravelSchedulerImpl.DEPARTURE_WINDOW_MS
        assertTrue(TravelSchedulerImpl.planAlarms(item, now, hasExactPermission = true).isEmpty())
    }
}
