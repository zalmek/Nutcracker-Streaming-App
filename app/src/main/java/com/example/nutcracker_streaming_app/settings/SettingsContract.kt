package com.example.nutcracker_streaming_app.settings

import android.util.Range
import android.util.Size
import com.example.nutcracker_streaming_app.presentetion.base.ViewEvent
import com.example.nutcracker_streaming_app.presentetion.base.ViewSideEffect
import com.example.nutcracker_streaming_app.presentetion.base.ViewState
import com.example.nutcracker_streaming_app.utils.Option

class SettingsContract {
    sealed class Event : ViewEvent {
        data object Refresh: Event()
        data class SelectResolution(val resolution: Option.Resolution) : Event()
        data class SelectFramerate(val framerate: Option.Framerate) : Event()
        data class SelectProtocol(val protocol: Option.Protocol): Event()
        data class InputBitrate(val bitrateRange: Option.Bitrate): Event()
        data class InputSrtLink(val srtLink: Option.Link.SrtLink) : Event()
        data class InputRtmpLink(val rtmpLink: Option.Link.RtmpLink) : Event()
        data class SelectAudioEncoder(val audioEncoder: Option.AudioEncoder): Event()
        data class SelectVideoEncoder(val videoEncoder: Option.VideoEncoder): Event()
        data class ToggleAdaptiveBitrate(val adaptiveBitrateEnabled: Option.AdaptiveBitrateEnabled): Event()
    }

    data class State(
        val bitrateRange: Option.Bitrate,
        val protocol: Option.Protocol,
        val videoEncoder: Option.VideoEncoder,
        val audioEncoder: Option.AudioEncoder,
        val rtmpLink: Option.Link,
        val srtLink: Option.Link,
        val framerate: Option.Framerate,
        val resolution: Option.Resolution,
        val adaptiveBitrateEnabled: Option.AdaptiveBitrateEnabled,
        val supportedStates: SupportedStates
    ) : ViewState

    data class SupportedStates(
        val supportedVideoEncoder: List<String>,
        val audioEncoder: String,
        val videoEncoder: String,
        val inputChannelRange: Range<Int>,
        val bitrateRange: Range<Int>,
        val sampleRates: List<Int>,
        val supportedAudioEncoder: List<String>,
        val byteFormats: List<Int>,
        val supportedResolutions: List<Size>,
        val supportedFramerates: List<Range<Int>>,
        val supportedBitrates: Range<Int>,
//        var getSupportedAllProfiles: List<Int>,
        val profiles: List<Int>,
    ) {

    }


    sealed class Effect : ViewSideEffect
}
