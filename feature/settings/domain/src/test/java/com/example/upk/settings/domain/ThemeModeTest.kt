package com.example.upk.settings.domain

import com.example.upk.settings.domain.models.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `valueOf returns matching enum for a valid name`() {
        val input = "DARK"

        val result = ThemeMode.valueOf(input)

        assertEquals(ThemeMode.DARK, result)
    }

    @Test
    fun `ThemeMode has exactly three values`() {
        assertEquals(3, ThemeMode.entries.size)
        assertTrue(ThemeMode.entries.containsAll(listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf throws for an unknown name`() {
        ThemeMode.valueOf("PURPLE")
    }
}
