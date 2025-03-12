package com.example.nutcracker_streaming_app.demo

import com.example.nutcracker_streaming_app.presentetion.base.BaseViewModel
import com.example.nutcracker_streaming_app.utils.Protocol

class DemoViewModel: BaseViewModel<DemoContact.Event, DemoContact.State, DemoContact.Effect>() {

    override fun setInitialState(): DemoContact.State {
        return DemoContact.State.Main(protocol = Protocol.Srt, link = "")
    }

    override fun handleEvents(event: DemoContact.Event) {
        when (event) {
            is DemoContact.Event.SelectProtocol -> setState {
                val state = this as DemoContact.State.Main
                val newState = state.copy(protocol = event.protocol)
                newState
            }

            is DemoContact.Event.InputLink -> setState {
                val state = this as DemoContact.State.Main
                val newState = state.copy(link = event.link)
                newState
            }
        }
    }
}