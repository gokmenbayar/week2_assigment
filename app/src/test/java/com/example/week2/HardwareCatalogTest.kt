package com.example.week2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareCatalogTest {
    private val devices = HardwareCatalog.initialItems

    @Test
    fun assignedDatasetHasEightDifferentIds() {
        assertEquals(8, devices.size)
        assertEquals(8, devices.map { it.id }.distinct().size)
    }

    @Test
    fun searchingIsCaseInsensitiveAndIgnoresWhitespace() {
        assertEquals("102", filterHardware(devices, "  nvidia ").single().id)
        assertEquals(2, filterHardware(devices, "GPU").size)
    }

    @Test
    fun categoryOnlyShowsSelectedType() {
        val gpu = filterCategory(devices, "Yapay Zeka / GPU")
        assertEquals(listOf("102", "108"), gpu.map { it.id })
    }

    @Test
    fun outOfStockItemIsIncludedButHasZeroStock() {
        assertEquals(0, devices.first { it.id == "107" }.stockCount)
        assertTrue(filterHardware(devices, "bulunamayan").isEmpty())
    }
}
