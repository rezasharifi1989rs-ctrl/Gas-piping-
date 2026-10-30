package com.example

import com.example.data.models.Pipe
import com.example.data.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPersianDigitsConversion() {
        val latin = "0123456789"
        val persian = PersianUtils.toPersianDigits(latin)
        assertEquals("۰۱۲۳۴۵۶۷۸۹", persian)

        val reversed = PersianUtils.toLatinDigits(persian)
        assertEquals(latin, reversed)
    }

    @Test
    fun testValveSearchMatching() {
        // Query in Persian digits with lowercase 'v' should match standard valve "24V1"
        assertTrue(PersianUtils.matchesValve("24V1", "۲۴v1"))
        assertTrue(PersianUtils.matchesValve("101V1", "101v"))
        assertTrue(PersianUtils.matchesValve("100v2", "۱۰۰V2"))
    }

    @Test
    fun testPipeSnappingProjection() {
        val pipe = Pipe(
            id = "TEST_PIPE",
            points = doubleArrayOf(0.0, 0.0, 100.0, 0.0)
        )

        // Query point at (50.0, 10.0) should project to (50.0, 0.0) with distance 10.0
        val snap = pipe.findClosestPoint(50.0, 10.0)
        assertEquals(50.0, snap.snappedX, 0.001)
        assertEquals(0.0, snap.snappedY, 0.001)
        assertEquals(10.0, snap.distance, 0.001)
        assertEquals(0, snap.segmentIndex)
    }
}
