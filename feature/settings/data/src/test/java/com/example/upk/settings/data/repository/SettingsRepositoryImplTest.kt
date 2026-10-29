package com.example.upk.settings.data.repository

import com.example.upk.settings.data.AppSettingsPreferences.ThemeProto
import com.example.upk.settings.data.local.datasource.FakeSettingsLocalDataSource
import com.example.upk.settings.data.local.datasource.SettingsLocalDataSource
import com.example.upk.settings.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsRepositoryImplTest {

    private lateinit var localDataSource: FakeSettingsLocalDataSource
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        localDataSource = FakeSettingsLocalDataSource()
        repository = SettingsRepositoryImpl(localDataSource)
    }

    // ---------- getThemeMode: proto -> domain mapping ----------

    @Test
    fun `proto DARK maps to domain DARK`() = runBlocking {
        localDataSource.themeMode.value = ThemeProto.DARK

        assertEquals(ThemeMode.DARK, repository.getThemeMode().first())
    }

    @Test
    fun `proto LIGHT maps to domain LIGHT`() = runBlocking {
        localDataSource.themeMode.value = ThemeProto.LIGHT

        assertEquals(ThemeMode.LIGHT, repository.getThemeMode().first())
    }

    @Test
    fun `proto SYSTEM maps to domain SYSTEM`() = runBlocking {
        localDataSource.themeMode.value = ThemeProto.SYSTEM

        assertEquals(ThemeMode.SYSTEM, repository.getThemeMode().first())
    }

    @Test
    fun `proto THEME_UNSPECIFIED maps to domain SYSTEM`() = runBlocking {
        localDataSource.themeMode.value = ThemeProto.THEME_UNSPECIFIED

        assertEquals(ThemeMode.SYSTEM, repository.getThemeMode().first())
    }

    @Test
    fun `proto UNRECOGNIZED maps to domain SYSTEM`() = runBlocking {
        localDataSource.themeMode.value = ThemeProto.UNRECOGNIZED

        assertEquals(ThemeMode.SYSTEM, repository.getThemeMode().first())
    }

    @Test
    fun `getThemeMode drops consecutive duplicates after mapping`() = runBlocking {
        // THEME_UNSPECIFIED and SYSTEM both map to SYSTEM, so it must be emitted only once.
        val dataSource = object : SettingsLocalDataSource by localDataSource {
            override fun observeThemeMode(): Flow<ThemeProto> =
                flowOf(ThemeProto.THEME_UNSPECIFIED, ThemeProto.SYSTEM, ThemeProto.DARK, ThemeProto.DARK)
        }

        val result = SettingsRepositoryImpl(dataSource).getThemeMode().toList()

        assertEquals(listOf(ThemeMode.SYSTEM, ThemeMode.DARK), result)
    }

    // ---------- setThemeMode: domain -> proto mapping ----------

    @Test
    fun `setting DARK stores proto DARK`() = runBlocking {
        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeProto.DARK, localDataSource.lastSetThemeMode)
    }

    @Test
    fun `setting LIGHT stores proto LIGHT`() = runBlocking {
        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeProto.LIGHT, localDataSource.lastSetThemeMode)
    }

    @Test
    fun `setting SYSTEM stores proto SYSTEM`() = runBlocking {
        repository.setThemeMode(ThemeMode.SYSTEM)

        assertEquals(ThemeProto.SYSTEM, localDataSource.lastSetThemeMode)
    }

    @Test
    fun `get returns the value that was set`() = runBlocking {
        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repository.getThemeMode().first())
    }

    // ---------- getLanguageTag ----------

    @Test
    fun `getLanguageTag returns value from local data source`() = runBlocking {
        localDataSource.languageTag.value = "vi-VN"

        assertEquals("vi-VN", repository.getLanguageTag().first())
    }

    @Test
    fun `getLanguageTag drops consecutive duplicates`() = runBlocking {
        val dataSource = object : SettingsLocalDataSource by localDataSource {
            override fun observeLanguageTag(): Flow<String> = flowOf("en", "en", "vi", "vi", "en")
        }

        val result = SettingsRepositoryImpl(dataSource).getLanguageTag().toList()

        assertEquals(listOf("en", "vi", "en"), result)
    }

    // ---------- setLanguageTag: tag normalization ----------

    @Test
    fun `setLanguageTag trims surrounding whitespace`() = runBlocking {
        repository.setLanguageTag("  en-US  ")

        assertEquals("en-US", localDataSource.lastSetLanguageTag)
    }

    @Test
    fun `setLanguageTag stores empty string for empty input`() = runBlocking {
        repository.setLanguageTag("")

        assertEquals("", localDataSource.lastSetLanguageTag)
    }

    @Test
    fun `setLanguageTag stores empty string for blank input`() = runBlocking {
        repository.setLanguageTag("   ")

        assertEquals("", localDataSource.lastSetLanguageTag)
    }

    @Test
    fun `setLanguageTag normalizes casing to BCP 47`() = runBlocking {
        repository.setLanguageTag("en-us")
        assertEquals("en-US", localDataSource.lastSetLanguageTag)

        repository.setLanguageTag("VI-vn")
        assertEquals("vi-VN", localDataSource.lastSetLanguageTag)
    }

    @Test
    fun `setLanguageTag keeps a language-only tag unchanged`() = runBlocking {
        repository.setLanguageTag("vi")

        assertEquals("vi", localDataSource.lastSetLanguageTag)
    }
}
