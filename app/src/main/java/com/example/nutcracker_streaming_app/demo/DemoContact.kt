package com.example.nutcracker_streaming_app.demo

import com.example.nutcracker_streaming_app.utils.Protocol
import com.example.nutcracker_streaming_app.presentetion.base.ViewEvent
import com.example.nutcracker_streaming_app.presentetion.base.ViewSideEffect
import com.example.nutcracker_streaming_app.presentetion.base.ViewState

class DemoContact {
    sealed class Event: ViewEvent {
        data class SelectProtocol(val protocol: Protocol): Event()
        data class InputLink(val link: String): Event()
    }
    sealed class State: ViewState {
        data class Main(
            val protocol: Protocol,
            val link: String,
        ) : State()
    }
    sealed class Effect: ViewSideEffect
}