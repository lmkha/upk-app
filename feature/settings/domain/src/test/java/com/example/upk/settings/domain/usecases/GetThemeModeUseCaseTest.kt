package com.example.upk.settings.domain.usecases

import com.example.upk.settings.domain.FakeSettingsRepository
import com.example.upk.settings.domain.models.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetThemeModeUseCaseTest {

    private lateinit var fakeRepository: FakeSettingsRepository
    private lateinit var getThemeMode: GetThemeModeUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeSettingsRepository()
        getThemeMode = GetThemeModeUseCase(fakeRepository)
    }

    @Test
    fun `default value is SYSTEM`() = runBlocking {
        val result = getThemeMode().first()

        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun `returns latest value from repository`() = runBlocking {
        fakeRepository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, getThemeMode().first())
    }
}
