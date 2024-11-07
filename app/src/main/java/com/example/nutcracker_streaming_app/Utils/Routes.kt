package com.example.nutcracker_streaming_app.Utils

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    @Serializable
    data object MainScreen : Routes()
    @Serializable
    data object SettingsScreen : Routes()
    @Serializable
    data object StreamScreen : Routes()
    @Serializable
    data object QrScreen : Routes()
}