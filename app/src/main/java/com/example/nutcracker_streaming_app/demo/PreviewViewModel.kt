package com.example.nutcracker_streaming_app.demo

import com.example.nutcracker_streaming_app.presentetion.base.BaseViewModel
import com.example.nutcracker_streaming_app.utils.Protocol

class PreviewViewModel: BaseViewModel<PreviewContact.Event, PreviewContact.State, PreviewContact.Effect>() {

    override fun setInitialState(): PreviewContact.State {
        return PreviewContact.State.Main(protocol = Protocol.Srt, link = "")
    }

    override fun handleEvents(event: PreviewContact.Event) {
        when (event) {
            is PreviewContact.Event.SelectProtocol -> setState {
                val state = this as PreviewContact.State.Main
                val newState = state.copy(protocol = event.protocol)
                newState
            }

            is PreviewContact.Event.InputLink -> setState {
                val state = this as PreviewContact.State.Main
                val newState = state.copy(link = event.link)
                newState
            }
        }
    }
}