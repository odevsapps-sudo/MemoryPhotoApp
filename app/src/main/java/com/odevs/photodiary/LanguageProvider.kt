package com.odevs.photodiary.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object LanguageProvider {
    var language by mutableStateOf("en") // vagy "hu" a magyarhoz
}
