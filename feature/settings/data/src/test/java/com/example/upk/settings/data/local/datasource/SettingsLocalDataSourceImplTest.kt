package com.example.upk.settings.data.local.datasource

import com.example.upk.settings.data.AppSettingsPreferences
import com.example.upk.settings.data.AppSettingsPreferences.ThemeProto
import com.example.upk.settings.data.FakeDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsLocalDataSourceImplTest {

    private lateinit var dataStore: FakeDataStore<AppSettingsPreferences>
    private lateinit var dataSource: SettingsLocalDataSourceImpl

    @Before
    fun setUp() {
        dataStore = FakeDataStore(AppSettingsPreferences.getDefaultInstance())
        dataSource = SettingsLocalDataSourceImpl(dataStore)
    }

    @Test
    fun `default theme is THEME_UNSPECIFIED`() = runBlocking {
        assertEquals(ThemeProto.THEME_UNSPECIFIED, dataSource.observeThemeMode().first())
    }

    @Test
    fun `default language is empty string`() = runBlocking {
        assertEquals("", dataSource.observeLanguageTag().first())
    }

    @Test
    fun `setThemeMode persists to data store`() = runBlocking {
        dataSource.setThemeMode(ThemeProto.DARK)

        assertEquals(ThemeProto.DARK, dataSource.observeThemeMode().first())
        assertEquals(ThemeProto.DARK, dataStore.data.first().themeMode)
    }

    @Test
    fun `setLanguageTag persists to data store`() = runBlocking {
        dataSource.setLanguageTag("vi-VN")

        assertEquals("vi-VN", dataSource.observeLanguageTag().first())
        assertEquals("vi-VN", dataStore.data.first().language)
    }

    @Test
    fun `setThemeMode keeps previously stored language`() = runBlocking {
        dataSource.setLanguageTag("vi-VN")

        dataSource.setThemeMode(ThemeProto.LIGHT)

        val prefs = dataStore.data.first()
        assertEquals("vi-VN", prefs.language)
        assertEquals(ThemeProto.LIGHT, prefs.themeMode)
    }

    @Test
    fun `setLanguageTag keeps previously stored theme`() = runBlocking {
        dataSource.setThemeMode(ThemeProto.DARK)

        dataSource.setLanguageTag("en")

        val prefs = dataStore.data.first()
        assertEquals(ThemeProto.DARK, prefs.themeMode)
        assertEquals("en", prefs.language)
    }
}
