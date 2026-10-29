package com.example.upk.settings.domain.usecases

import com.example.upk.settings.domain.FakeSettingsRepository
import com.example.upk.settings.domain.models.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateThemeModeUseCaseTest {

    private lateinit var fakeRepository: FakeSettingsRepository
    private lateinit var updateThemeMode: UpdateThemeModeUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeSettingsRepository()
        updateThemeMode = UpdateThemeModeUseCase(fakeRepository)
    }

    @Test
    fun `updating to DARK stores DARK in repository`() = runBlocking {
        updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, fakeRepository.getThemeMode().first())
    }

    @Test
    fun `update calls repository exactly once`() = runBlocking {
        updateThemeMode(ThemeMode.LIGHT)

        assertEquals(1, fakeRepository.setThemeModeCallCount)
    }

    @Test
    fun `multiple updates keep the last value`() = runBlocking {
        updateThemeMode(ThemeMode.DARK)
        updateThemeMode(ThemeMode.LIGHT)
        updateThemeMode(ThemeMode.SYSTEM)

        assertEquals(ThemeMode.SYSTEM, fakeRepository.getThemeMode().first())
        assertEquals(3, fakeRepository.setThemeModeCallCount)
    }
}
