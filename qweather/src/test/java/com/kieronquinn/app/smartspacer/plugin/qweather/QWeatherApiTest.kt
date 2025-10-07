package com.kieronquinn.app.smartspacer.plugin.qweather

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily
import com.kieronquinn.app.smartspacer.plugin.qweather.data.QWeatherResponse
import com.kieronquinn.app.smartspacer.plugin.qweather.data.Refer
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QWeatherApiTest {

    private val qweatherApi = mockk<QWeatherApi>()

    @Test
    fun `getIndices returns expected response`() = runTest {
        // Arrange
        val mockResponse = QWeatherResponse(
            code = "200",
            updateTime = "2024-01-01T00:00:00+00:00",
            fxLink = "http://example.com",
            daily = listOf(
                Daily(
                    date = "2024-01-01",
                    type = "1",
                    name = "Test Index",
                    level = "1",
                    category = "Test Category",
                    text = "Test Text"
                )
            ),
            refer = Refer(
                sources = listOf("Test Source"),
                license = listOf("Test License")
            )
        )
        coEvery { qweatherApi.getIndices(any(), any(), any()) } returns mockResponse

        // Act
        val response = qweatherApi.getIndices("location", "key", "type")

        // Assert
        assertEquals(mockResponse, response)
    }
}