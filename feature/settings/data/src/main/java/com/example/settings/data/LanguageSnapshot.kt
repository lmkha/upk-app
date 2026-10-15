package com.example.settings.data

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageSnapshot @Inject constructor() {

    @Volatile
    private var languageTag: String =
        Locale.getDefault().toLanguageTag()

    fun getLanguageTag(): String = languageTag

    fun updateLanguageTag(tag: String) {
        languageTag = tag
    }
}
