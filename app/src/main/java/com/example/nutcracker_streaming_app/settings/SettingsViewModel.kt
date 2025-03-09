package com.example.nutcracker_streaming_app.settings

import com.example.nutcracker_streaming_app.presentetion.base.BaseViewModel
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.StreamerHelper

class SettingsViewModel :
    BaseViewModel<SettingsContract.Event, SettingsContract.State, SettingsContract.Effect>() {
    override fun setInitialState(): SettingsContract.State {
        return SettingsContract.State(
            audioEncoder = NsaPreferences.audioEncoder,
            videoEncoder = NsaPreferences.videoEncoder,
            rtmpLink = NsaPreferences.rtmpLink,
            srtLink = NsaPreferences.srtLink,
            framerate = NsaPreferences.framerate,
            resolution = NsaPreferences.resolution,
            protocol = NsaPreferences.protocol,
            bitrateRange = NsaPreferences.bitrateRange,
            supportedStates = StreamerHelper.getSupportedStates()
        )
    }

    override fun handleEvents(event: SettingsContract.Event) {
        when (event) {
            is SettingsContract.Event.SelectFramerate -> setState {
                NsaPreferences.framerate = event.framerate
                copy(framerate = event.framerate)
            }

            is SettingsContract.Event.SelectResolution -> setState {
                NsaPreferences.resolution = event.resolution
                copy(resolution = event.resolution)
            }

            is SettingsContract.Event.InputRtmpLink -> setState {
                NsaPreferences.rtmpLink = event.rtmpLink
                copy(rtmpLink = event.rtmpLink)
            }

            is SettingsContract.Event.InputSrtLink -> setState {
                NsaPreferences.srtLink = event.srtLink
                copy(srtLink = event.srtLink)
            }

            is SettingsContract.Event.SelectAudioEncoder -> setState {
                NsaPreferences.audioEncoder = event.audioEncoder
                copy(audioEncoder = event.audioEncoder)
            }

            is SettingsContract.Event.SelectVideoEncoder -> setState {
                NsaPreferences.videoEncoder = event.videoEncoder
                copy(videoEncoder = event.videoEncoder)
            }

            SettingsContract.Event.Refresh -> setState {
                setInitialState()
            }

            is SettingsContract.Event.SelectProtocol -> setState {
                NsaPreferences.protocol = event.protocol
                copy(protocol = event.protocol)
            }

            is SettingsContract.Event.InputBitrate -> setState {
                NsaPreferences.bitrateRange = event.bitrateRange
                copy(bitrateRange = event.bitrateRange)
            }
        }
    }
}