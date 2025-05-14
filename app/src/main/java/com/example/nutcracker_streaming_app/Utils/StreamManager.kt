package com.example.nutcracker_streaming_app.utils

import android.content.Context
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.Surface
import androidx.annotation.MainThread
import com.example.nutcracker_streaming_app.StreamingService
import com.pedro.extrasources.CameraXSource
import com.pedro.library.base.StreamBase
import com.pedro.library.rtmp.RtmpStream
import com.pedro.library.srt.SrtStream
import com.pedro.library.util.BitrateAdapter

object StreamManager {
    lateinit var rtmpStream: RtmpStream
    lateinit var rtmpBitrateAdapter: BitrateAdapter
    lateinit var srtStream: SrtStream
    lateinit var srtBitrateAdapter: BitrateAdapter

    val bitrateAdapter: BitrateAdapter
        get() = when (protocol) {
            Option.Protocol.Rtmp -> rtmpBitrateAdapter
            Option.Protocol.Srt -> srtBitrateAdapter
        }
    val vBitrate: Int get() = NsaPreferences.bitrateRange.range.lower * 2
    var isPreConfigured: Boolean = false
    val isStreaming: Boolean
        get() = if (isPreConfigured) when (protocol) {
            Option.Protocol.Rtmp -> rtmpStream.isStreaming
            Option.Protocol.Srt -> srtStream.isStreaming
        } else false
    val audioEncoder: String get() = NsaPreferences.audioEncoder.mediaFormat
    val videoEncoder: String get() = NsaPreferences.videoEncoder.toString()
    val rtmpLink: String get() = NsaPreferences.rtmpLink.toString()
    val srtLink: String get() = NsaPreferences.srtLink.toString()
    val framerate: Range<Int> get() = NsaPreferences.framerate.range
    val resolution: Size get() = NsaPreferences.resolution.toSize()
    val protocol: Option.Protocol get() = NsaPreferences.protocol
    val bitrateRange: Range<Int> get() = NsaPreferences.bitrateRange.range

    fun startStream() {
        Log.d("ASDKA:LD", "startStream: $rtmpLink")
        when (protocol) {
            Option.Protocol.Rtmp -> rtmpStream.startStream(rtmpLink)
            Option.Protocol.Srt -> srtStream.startStream(srtLink)
        }
    }

    @MainThread
    fun prepareStream(streamBase: StreamBase) {
        if (!streamBase.isStreaming && !streamBase.isOnPreview) {
            streamBase.prepareVideo(
                resolution.width,
                resolution.height,
                bitrateRange.lower,
                framerate.upper
            )
            streamBase.prepareAudio(32000, true, 128000)
        }
    }

    fun stopStream() {
        when (protocol) {
            Option.Protocol.Rtmp -> if (rtmpStream.isStreaming) rtmpStream.stopStream()
            Option.Protocol.Srt -> if (srtStream.isStreaming) srtStream.stopStream()
        }
    }

    @MainThread
    fun attachPreview(surface: Surface, context: Context, width: Int, height: Int) {
        Log.d("Attached", "StreamScreen: $surface #${width} $height")

        when (protocol) {
            Option.Protocol.Rtmp -> {
                rtmpStream.changeVideoSource(CameraXSource(context))
                if (!rtmpStream.isOnPreview) rtmpStream.startPreview(surface, width, height)
            }

            Option.Protocol.Srt -> {
                srtStream.changeVideoSource(CameraXSource(context))
                if (!srtStream.isOnPreview) srtStream.startPreview(surface, width, height)
            }
        }
    }

    fun detachPreview() {
        Log.d("Detached", "StreamScreen:")
        when (protocol) {
            Option.Protocol.Rtmp -> rtmpStream.stopPreview()
            Option.Protocol.Srt -> srtStream.stopPreview()
        }
    }

    @MainThread
    fun createStream(
        context: Context,
        service: StreamingService,
    ): Boolean {
        val aBitrate = 128 * 1000
        rtmpStream =
            RtmpStream(context, service).apply {
                getGlInterface().autoHandleOrientation = true
                getStreamClient().setBitrateExponentialFactor(0.5f)
                getStreamClient().forceIncrementalTs(true)
            }
        srtStream = SrtStream(context, service).apply {
            getGlInterface().autoHandleOrientation = true
            getStreamClient().setBitrateExponentialFactor(0.5f)
        }
        srtBitrateAdapter = BitrateAdapter {
            rtmpStream.setVideoBitrateOnFly(it)
        }.apply {
            setMaxBitrate(vBitrate + aBitrate)
        }
        rtmpBitrateAdapter = BitrateAdapter {
            rtmpStream.setVideoBitrateOnFly(it)
        }.apply {
            setMaxBitrate(vBitrate + aBitrate)
        }
        isPreConfigured = true
        return true
    }
}
