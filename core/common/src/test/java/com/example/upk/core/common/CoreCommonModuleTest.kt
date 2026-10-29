package com.example.upk.core.common

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor

/**
 * CoreCommonModule is a Hilt module, but its @Provides functions build objects whose
 * configuration matters to the whole app, so the configuration itself is tested here.
 */
class CoreCommonModuleTest {

    @Serializable
    data class Sample(
        val name: String,
        val nickname: String? = null,
        val count: Int = 0,
    )

    private val json = CoreCommonModule.provideJson()

    // ---------- Json configuration ----------

    @Test
    fun `json ignores unknown keys`() {
        val decoded = json.decodeFromString<Sample>("""{"name":"a","unexpected":1}""")

        assertEquals(Sample(name = "a"), decoded)
    }

    @Test
    fun `json treats missing nullable field as null and missing default as default`() {
        val decoded = json.decodeFromString<Sample>("""{"name":"a"}""")

        assertEquals(Sample(name = "a", nickname = null, count = 0), decoded)
    }

    @Test
    fun `json omits null fields and keeps defaults when encoding`() {
        val encoded = json.encodeToString(Sample.serializer(), Sample(name = "a"))

        assertEquals("""{"name":"a","count":0}""", encoded)
    }

    @Test
    fun `json accepts explicit null for nullable field`() {
        val decoded = json.decodeFromString<Sample>("""{"name":"a","nickname":null}""")

        assertNull(decoded.nickname)
    }

    // ---------- Application scope ----------

    @Test
    fun `application scope runs on the IO dispatcher`() {
        val scope = CoreCommonModule.provideApplicationScope()

        assertEquals(Dispatchers.IO, scope.coroutineContext[ContinuationInterceptor])
        scope.coroutineContext.job.cancel()
    }

    @Test
    fun `a failing child does not cancel the application scope`() = runBlocking {
        val scope = CoreCommonModule.provideApplicationScope()

        val failing = scope.launch { throw IllegalStateException("boom") }
        failing.join()

        assertTrue(scope.isActive)
        val survivor = scope.launch { }
        survivor.join()
        assertFalse(survivor.isCancelled)
        scope.coroutineContext.job.cancel()
    }

    @Test
    fun `control - without SupervisorJob a failing child cancels siblings`() = runBlocking {
        // Documents why provideApplicationScope uses SupervisorJob.
        val plainScope = CoroutineScope(Job() + Dispatchers.IO)
        val gate = CompletableDeferred<Unit>()
        val sibling = plainScope.launch { gate.await() }

        val failing = plainScope.launch { throw IllegalStateException("boom") }
        failing.join()
        sibling.join()

        assertTrue(sibling.isCancelled)
        assertFalse(plainScope.isActive)
    }

    @Test
    fun `provideApplicationScope returns a fresh scope per call`() {
        // Hilt makes it a singleton; the provider itself must not share state between calls.
        val first = CoreCommonModule.provideApplicationScope()
        val second = CoreCommonModule.provideApplicationScope()

        assertNotSame(first.coroutineContext.job, second.coroutineContext.job)
        first.coroutineContext.job.cancel()
        second.coroutineContext.job.cancel()
    }
}
