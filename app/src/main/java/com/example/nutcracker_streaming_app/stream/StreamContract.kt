package com.example.nutcracker_streaming_app.stream

import com.example.nutcracker_streaming_app.presentetion.base.ViewEvent
import com.example.nutcracker_streaming_app.presentetion.base.ViewSideEffect
import com.example.nutcracker_streaming_app.presentetion.base.ViewState

class StreamContract {
    sealed class Effect: ViewSideEffect
    sealed class Event: ViewEvent {
        data class OnStartStopClicked(val onStartClick: Boolean): Event()
    }
    data class State(
        val isStreaming: Boolean
    ): ViewState
}