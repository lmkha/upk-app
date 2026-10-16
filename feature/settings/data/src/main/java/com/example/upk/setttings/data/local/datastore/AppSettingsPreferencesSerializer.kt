package com.example.upk.setttings.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.upk.settings.data.AppSettingsPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object AppSettingsPreferencesSerializer : Serializer<AppSettingsPreferences> {
    override val defaultValue: AppSettingsPreferences = AppSettingsPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettingsPreferences {
        try {
            return AppSettingsPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppSettingsPreferences, output: OutputStream) {
        return t.writeTo(output)
    }
}