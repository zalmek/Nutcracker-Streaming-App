package com.example.nutcracker_streaming_app.stream

import android.content.Context
import android.view.Surface
import androidx.annotation.StringRes
import com.example.nutcracker_streaming_app.StreamingService
import com.example.nutcracker_streaming_app.presentetion.base.ViewEvent
import com.example.nutcracker_streaming_app.presentetion.base.ViewSideEffect
import com.example.nutcracker_streaming_app.presentetion.base.ViewState

class StreamContract {
    sealed class Effect: ViewSideEffect {
        data class ShowSnackbar(
            @StringRes val message: Int
        ): Effect()

        data object Empty: Effect()
    }
    sealed class Event: ViewEvent {
        data class OnNewBitrate(val bitrate: String): Event()
        data class AttachToService(val service: StreamingService): Event()
        data class OnStartStopClicked(val service: StreamingService): Event()
        data object PrepareVideo: Event()
            data class ConfigureCamera(
            val context: Context,
            val service: StreamingService,
        ): Event()
        data object OnConnectionStart: Event()
        data object OnConnectionSuccess: Event()
        data object OnConnectionFailed: Event()
        data object OnDisconnect: Event()
        data object ViewDetached: Event()
        data class AttachView(
            val context: Context,
            val surface: Surface,
            val width: Int,
            val height: Int,
        ): Event()
    }
    data class State(
        val streamState: StreamState,
        val cameraIsConfigured: Boolean,
        val currentBitrate: String,
    ): ViewState
}

sealed class StreamState {
    data object Connecting: StreamState()
    data object Connected: StreamState()
    data object Disconnected: StreamState()
    data object Failed: StreamState()
}