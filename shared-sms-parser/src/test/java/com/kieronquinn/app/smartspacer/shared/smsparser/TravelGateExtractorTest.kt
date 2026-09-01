package com.kieronquinn.app.smartspacer.shared.smsparser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TravelGateExtractorTest {

    @Test
    fun `extracts 12306 ticket gates`() {
        assertEquals("A12", TravelGateExtractor.extract("南宁东站16:28开，检票口A12，请尽快进站"))
        assertEquals("B15-B16", TravelGateExtractor.extract("检票口：B15-B16"))
        assertEquals("A12", TravelGateExtractor.extract("请从A12检票口进站"))
        assertEquals("东1", TravelGateExtractor.extract("检票口东1"))
    }

    @Test
    fun `extracts airline boarding gates`() {
        assertEquals("H12", TravelGateExtractor.extract("座位13F，登机口H12。"))
        assertEquals("C3", TravelGateExtractor.extract("Please proceed to Gate C3"))
    }

    @Test
    fun `returns null when no gate is present`() {
        assertNull(TravelGateExtractor.extract("【12306】购票成功，2月28日G5507次，南宁东站16:28开。"))
    }
}
