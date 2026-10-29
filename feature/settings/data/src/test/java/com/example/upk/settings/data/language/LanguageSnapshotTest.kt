package com.example.upk.settings.data.language

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class LanguageSnapshotTest {

    @Test
    fun `initial value is the system default locale`() {
        val snapshot = LanguageSnapshot()

        assertEquals(Locale.getDefault().toLanguageTag(), snapshot.getLanguageTag())
    }

    @Test
    fun `updateLanguageTag changes the returned value`() {
        val snapshot = LanguageSnapshot()

        snapshot.updateLanguageTag("vi-VN")

        assertEquals("vi-VN", snapshot.getLanguageTag())
    }

    @Test
    fun `multiple updates keep the last value`() {
        val snapshot = LanguageSnapshot()

        snapshot.updateLanguageTag("vi-VN")
        snapshot.updateLanguageTag("ja-JP")

        assertEquals("ja-JP", snapshot.getLanguageTag())
    }
}
