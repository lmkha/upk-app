package com.example.upk.settings.data.local.datastore

import androidx.datastore.core.CorruptionException
import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.settings.data.AppSettingsPreferences.ThemeProto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AppSettingsPreferencesSerializerTest {

    private val serializer = AppSettingsPreferencesSerializer

    @Test
    fun `defaultValue is the default proto instance`() {
        assertEquals(AppSettingsPreferences.getDefaultInstance(), serializer.defaultValue)
        assertEquals(ThemeProto.THEME_UNSPECIFIED, serializer.defaultValue.themeMode)
        assertEquals("", serializer.defaultValue.language)
    }

    @Test
    fun `write then read round-trips the data`() = runBlocking {
        val original = AppSettingsPreferences.newBuilder()
            .setThemeMode(ThemeProto.DARK)
            .setLanguage("vi-VN")
            .build()
        val output = ByteArrayOutputStream()

        serializer.writeTo(original, output)
        val restored = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun `reading empty input returns the default proto`() = runBlocking {
        val restored = serializer.readFrom(ByteArrayInputStream(ByteArray(0)))

        assertEquals(AppSettingsPreferences.getDefaultInstance(), restored)
    }

    @Test(expected = CorruptionException::class)
    fun `reading corrupted input throws CorruptionException`() = runBlocking {
        // 0x08 is the tag for field 1 (varint) with no value following it: a truncated proto.
        val corrupted = byteArrayOf(0x08)

        serializer.readFrom(ByteArrayInputStream(corrupted))
        Unit
    }
}
