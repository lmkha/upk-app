package com.example.upk.settings.data.language

import com.example.upk.settings.data.local.datasource.FakeSettingsLocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * The provider needs a CoroutineScope to observe the DataStore.
 * Tests pass a scope backed by UnconfinedTestDispatcher so the coroutine runs immediately,
 * which makes it safe to assert right after constructing the provider.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsAppLanguageProviderImplTest {

    private fun createProvider(
        localDataSource: FakeSettingsLocalDataSource,
        snapshot: LanguageSnapshot,
        scope: CoroutineScope,
    ) = SettingsAppLanguageProviderImpl(localDataSource, snapshot, scope)

    @Test
    fun `falls back to default locale when no language is stored`() = runTest {
        val localDataSource = FakeSettingsLocalDataSource()
        val snapshot = LanguageSnapshot()
        val provider = createProvider(
            localDataSource, snapshot, CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        assertEquals(Locale.getDefault().toLanguageTag(), provider.getLanguageTag())
    }

    @Test
    fun `returns stored language when one is stored`() = runTest {
        val localDataSource = FakeSettingsLocalDataSource().apply { languageTag.value = "vi-VN" }
        val snapshot = LanguageSnapshot()
        val provider = createProvider(
            localDataSource, snapshot, CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        assertEquals("vi-VN", provider.getLanguageTag())
    }

    @Test
    fun `updates when stored language changes`() = runTest {
        val localDataSource = FakeSettingsLocalDataSource()
        val snapshot = LanguageSnapshot()
        val provider = createProvider(
            localDataSource, snapshot, CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        localDataSource.setLanguageTag("ja-JP")

        assertEquals("ja-JP", provider.getLanguageTag())
    }

    @Test
    fun `falls back to default locale when stored language is cleared`() = runTest {
        val localDataSource = FakeSettingsLocalDataSource().apply { languageTag.value = "vi-VN" }
        val snapshot = LanguageSnapshot()
        val provider = createProvider(
            localDataSource, snapshot, CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        localDataSource.setLanguageTag("")

        assertEquals(Locale.getDefault().toLanguageTag(), provider.getLanguageTag())
    }
}
