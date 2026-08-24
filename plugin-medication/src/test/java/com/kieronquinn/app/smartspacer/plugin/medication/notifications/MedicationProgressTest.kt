package com.kieronquinn.app.smartspacer.plugin.medication.notifications

import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationProgressTest {

    private fun medication(
        type: ScheduleType,
        times: String?,
        intervalHours: Int? = null
    ) = Medication(
        id = 1,
        name = "Test",
        dosage = "1",
        startDate = 0L,
        endDate = null,
        isUnlimited = true,
        scheduleType = type,
        intervalHours = intervalHours,
        intervalDays = null,
        timesOfDay = times,
        weekdays = null,
        nextDoseTs = 0L
    )

    @Test
    fun `specific times count as scheduled doses`() {
        val item = medication(ScheduleType.SPECIFIC_TIMES, """["08:00","12:00","18:00","22:00"]""")
        assertEquals(4, MedicationProgress.scheduledToday(item))
        assertTrue(MedicationProgress.isMultiDoseToday(item))
        assertEquals("2/4", MedicationProgress.capsule(2, 4))
    }

    @Test
    fun `every four hours from 08 00 is four doses`() {
        val item = medication(ScheduleType.EVERY_X_HOURS, "08:00", intervalHours = 4)
        assertEquals(4, MedicationProgress.scheduledToday(item))
        assertTrue(MedicationProgress.isMultiDoseToday(item))
    }

    @Test
    fun `once a day is not a live update`() {
        val item = medication(ScheduleType.EVERY_X_DAYS, "08:00")
        assertEquals(1, MedicationProgress.scheduledToday(item))
        assertFalse(MedicationProgress.isMultiDoseToday(item))
    }
}
