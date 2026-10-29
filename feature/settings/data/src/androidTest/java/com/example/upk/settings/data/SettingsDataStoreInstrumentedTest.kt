package com.example.upk.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.upk.settings.data.AppSettingsPreferences.ThemeProto
import com.example.upk.settings.data.local.datasource.SettingsLocalDataSourceImpl
import com.example.upk.settings.data.local.datastore.AppSettingsPreferencesSerializer
import com.example.upk.settings.data.repository.SettingsRepositoryImpl
import com.example.upk.settings.domain.models.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Instrumented test that uses a REAL proto DataStore writing to a file on the device.
 *
 * Unit tests cover the same classes with [FakeDataStore]; this test checks what a fake cannot:
 * that the serializer, DataStore and file system actually work together and that data
 * survives when the DataStore is re-opened.
 */
@RunWith(AndroidJUnit4::class)
class SettingsDataStoreInstrumentedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var file: File
    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<AppSettingsPreferences>
    private lateinit var localDataSource: SettingsLocalDataSourceImpl
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        // A unique file per test so tests never share state.
        file = File(context.filesDir, "settings_test_${UUID.randomUUID()}.pb")
        scope = newScope()
        dataStore = openDataStore(scope)
        localDataSource = SettingsLocalDataSourceImpl(dataStore)
        repository = SettingsRepositoryImpl(localDataSource)
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    private fun newScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun openDataStore(scope: CoroutineScope): DataStore<AppSettingsPreferences> =
        DataStoreFactory.create(
            serializer = AppSettingsPreferencesSerializer,
            scope = scope,
            produceFile = { file },
        )

    // ---------- Local data source with a real DataStore ----------

    @Test
    fun freshDataStoreReturnsProtoDefaults() = runBlocking {
        assertEquals(ThemeProto.THEME_UNSPECIFIED, localDataSource.observeThemeMode().first())
        assertEquals("", localDataSource.observeLanguageTag().first())
    }

    @Test
    fun setThemeModeIsReadBack() = runBlocking {
        localDataSource.setThemeMode(ThemeProto.DARK)

        assertEquals(ThemeProto.DARK, localDataSource.observeThemeMode().first())
    }

    @Test
    fun setLanguageTagIsReadBack() = runBlocking {
        localDataSource.setLanguageTag("vi-VN")

        assertEquals("vi-VN", localDataSource.observeLanguageTag().first())
    }

    @Test
    fun dataSurvivesReopeningTheDataStore() = runBlocking {
        localDataSource.setThemeMode(ThemeProto.LIGHT)
        localDataSource.setLanguageTag("ja-JP")
        // Close the first DataStore instance and wait until it has fully shut down:
        // DataStore refuses to open the same file twice while an instance is still active.
        scope.cancel()
        scope.coroutineContext.job.join()

        scope = newScope()
        val reopened = SettingsLocalDataSourceImpl(openDataStore(scope))

        assertEquals(ThemeProto.LIGHT, reopened.observeThemeMode().first())
        assertEquals("ja-JP", reopened.observeLanguageTag().first())
    }

    // ---------- Repository end to end ----------

    @Test
    fun repositoryStoresAndReadsDomainThemeMode() = runBlocking {
        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.getThemeMode().first())
        assertEquals(ThemeProto.DARK, dataStore.data.first().themeMode)
    }

    @Test
    fun repositoryMapsDefaultProtoToSystemTheme() = runBlocking {
        assertEquals(ThemeMode.SYSTEM, repository.getThemeMode().first())
    }

    @Test
    fun repositoryNormalizesLanguageTagBeforeStoring() = runBlocking {
        repository.setLanguageTag("  en-us ")

        assertEquals("en-US", repository.getLanguageTag().first())
        assertEquals("en-US", dataStore.data.first().language)
    }
}
