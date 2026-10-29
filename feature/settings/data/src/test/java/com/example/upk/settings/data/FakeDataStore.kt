package com.example.upk.settings.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake of [DataStore]: keeps data in memory instead of writing a file on Android.
 * DataStore only has two members, the `data` flow and `updateData`, so the fake stays small.
 */
class FakeDataStore<T>(initial: T) : DataStore<T> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<T> = state

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
