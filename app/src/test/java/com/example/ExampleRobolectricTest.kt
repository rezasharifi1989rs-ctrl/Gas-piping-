package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.demo.DemoNetworkData
import com.example.data.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string resource from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("شیر‌یاب گاز", appName)
    }

    @Test
    fun `demo network dataset is valid and complete`() {
        val dataset = DemoNetworkData.createDataset()
        assertEquals("نمونه‌ی نمایشی (DEMO)", dataset.name)
        assertTrue(dataset.isDemo)
        assertTrue("Must contain pipes", dataset.pipes.isNotEmpty())
        assertTrue("Must contain valves", dataset.valves.size >= 10)
        assertTrue("Must contain at least one station", dataset.stations.isNotEmpty())
        assertTrue("Must contain at least one end marker", dataset.endMarkers.isNotEmpty())
        assertTrue("Must contain crossings", dataset.crossings.isNotEmpty())

        // Check valve search normalization
        assertTrue(PersianUtils.matchesValve("24V1", "۲۴v1"))
        assertTrue(PersianUtils.matchesValve("24V1", "24V1"))
        assertTrue(PersianUtils.matchesValve("101V1", "۱۰۱"))
    }
}
