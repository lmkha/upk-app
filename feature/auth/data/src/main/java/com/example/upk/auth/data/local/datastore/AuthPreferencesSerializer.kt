package com.example.upk.auth.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.upk.auth.data.AuthPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object AuthPreferencesSerializer : Serializer<AuthPreferences> {
    override val defaultValue: AuthPreferences = AuthPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AuthPreferences {
        try {
            return AuthPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AuthPreferences, output: OutputStream) {
        return t.writeTo(output)
    }
}