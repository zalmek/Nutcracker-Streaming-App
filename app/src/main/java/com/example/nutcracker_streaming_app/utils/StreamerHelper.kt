package com.example.nutcracker_streaming_app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Range
import android.util.Size
import androidx.compose.runtime.Stable
import com.example.nutcracker_streaming_app.settings.SettingsContract
import io.github.thibaultbee.streampack.data.AudioConfig
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.ext.rtmp.streamers.CameraRtmpLiveStreamer
import io.github.thibaultbee.streampack.ext.srt.streamers.CameraSrtLiveStreamer
import io.github.thibaultbee.streampack.streamers.live.BaseCameraLiveStreamer
import io.github.thibaultbee.streampack.utils.defaultCameraId

object StreamerHelper {
    lateinit var supportedVideoEncoder: List<String>
    lateinit var audioEncoder: String
    lateinit var videoEncoder: String
    lateinit var inputChannelRange: Range<Int>
    lateinit var bitrateRange: Range<Int>
    lateinit var sampleRates: List<Int>
    lateinit var supportedAudioEncoder: List<String>
    lateinit var byteFormats: List<Int>
    lateinit var supportedResolutions: List<Size>
    lateinit var supportedFramerates: List<Range<Int>>
    lateinit var supportedBitrates: Range<Int>

    //    lateinit var getSupportedAllProfiles: List<Int>
    lateinit var profiles: List<Int>

    fun init(context: Context) {
        getSrtStreamer(context, false)
    }

    fun getSrtStreamer(context: Context, needToConfig: Boolean = true): CameraSrtLiveStreamer {
        val srtLiveStreamer = CameraSrtLiveStreamer(context = context)
        refreshSettings(srtLiveStreamer, context)
        if (needToConfig) configureStreamer(srtLiveStreamer)
        return srtLiveStreamer
    }

    fun getRtmpStreamer(context: Context): CameraRtmpLiveStreamer {
        val rtmpLiveStreamer = CameraRtmpLiveStreamer(context = context)
        refreshSettings(rtmpLiveStreamer, context)
        configureStreamer(rtmpLiveStreamer)
        return rtmpLiveStreamer
    }

    @SuppressLint("MissingPermission")
    fun configureStreamer(streamer: BaseCameraLiveStreamer) {
        val videoConfig = VideoConfig(
            resolution = NsaPreferences.resolution.toSize(),
            fps = NsaPreferences.framerate.range.upper,
            startBitrate = NsaPreferences.bitrateRange.range.lower,
//            mimeType = NsaPreferences.videoEncoder.mediaFormat
        )

        val audioConfig = AudioConfig(
//            mimeType = NsaPreferences.audioEncoder.mediaFormat
        )

        streamer.configure(audioConfig, videoConfig)
    }

    fun refreshSettings(
        streamer: BaseCameraLiveStreamer,
        context: Context
    ) {
        // Inflates video encoders
        supportedVideoEncoder = streamer.helper.video.supportedEncoders

        // AudioEncoders
        supportedAudioEncoder = streamer.helper.audio.supportedEncoders

        audioEncoder = supportedAudioEncoder[0]

        videoEncoder = supportedVideoEncoder[0]

        // Inflates audio number of channel
        inputChannelRange = streamer.helper.audio.getSupportedInputChannelRange(audioEncoder)

        // Inflates audio bitrate
        bitrateRange = streamer.helper.audio.getSupportedBitrates(audioEncoder)

        // Inflates audio sample rate
        sampleRates = streamer.helper.audio.getSupportedSampleRates(audioEncoder)

        // Inflates audio byte format
        byteFormats = streamer.helper.audio.getSupportedByteFormats()

        // Inflates video resolutions
        supportedResolutions = streamer.helper.video.getSupportedResolutions(
            context,
            videoEncoder
        )

        // Inflates video fps
        supportedFramerates = streamer.helper.video.getSupportedFramerates(
            context,
            videoEncoder,
            context.defaultCameraId
        )

        // Inflates video bitrate
        supportedBitrates = streamer.helper.video.getSupportedBitrates(videoEncoder)

        // Inflates profile
        profiles = streamer.helper.video.getSupportedAllProfiles(
            context,
            videoEncoder,
            context.defaultCameraId
        ).map { it }
    }

    fun getSupportedStates(): SettingsContract.SupportedStates {
        return SettingsContract.SupportedStates(
            supportedVideoEncoder,
            audioEncoder,
            videoEncoder,
            inputChannelRange,
            bitrateRange,
            sampleRates,
            supportedAudioEncoder,
            byteFormats,
            supportedResolutions,
            supportedFramerates,
            supportedBitrates,
//        var getSupportedAllProfiles: ,
            profiles,
        )
    }
}

@Stable
sealed class Option {
    abstract fun toPresentationString(): String

    @Stable
    data class Bitrate(val range: Range<Int>) : Option() {
        override fun toString(): String {
            return "${range.lower} - ${range.upper}"
        }

        override fun toPresentationString(): String {
            return "${range.lower / 1000}"
        }
    }

    @Stable
    sealed class Protocol : Option() {
        data object Srt : Protocol() {
            const val PROTOCOL = "SRT"
            override fun toString(): String {
                return PROTOCOL
            }

            override fun toPresentationString(): String = toString() // TODO
        }

        data object Rtmp : Protocol() {
            const val PROTOCOL = "RTMP"
            override fun toString(): String {
                return PROTOCOL
            }

            override fun toPresentationString(): String = toString() // TODO
        }
    }

    @Stable
    data class VideoEncoder(val mediaFormat: String) : Option() {
        override fun toString(): String {
            return mediaFormat
        }

        override fun toPresentationString(): String = toString() // TODO
    }

    @Stable
    data class AudioEncoder(val mediaFormat: String) : Option() {
        override fun toString(): String {
            return mediaFormat
        }

        override fun toPresentationString(): String = toString() // TODO
    }

    @Stable
    sealed class Link(val text: String) : Option() {
        data class SrtLink(val srtLink: String) : Link(srtLink) {
            override fun toString(): String {
                return srtLink
            }

            override fun toPresentationString(): String {
                return if (srtLink.isBlank()) {
                    "srt://"
                } else toString()
            }

        }

        data class RtmpLink(val rtmpLink: String) : Link(rtmpLink) {
            override fun toString(): String {
                return rtmpLink
            }

            override fun toPresentationString(): String {
                return if (rtmpLink.isBlank()) {
                    "rtmp://"
                } else toString()
            }

        }

    }

    @Stable
    data class Resolution(val width: Int, val height: Int) : Option() {
        override fun toString(): String {
            return "${width}x${height}"
        }

        override fun toPresentationString(): String = toString() // TODO
    }

    @Stable
    data class Framerate(val range: Range<Int>) : Option() {
        //        sealed class Variable(start: Int, end: Int) : Framerate(Range(start, end))
//        sealed class Constant(framerate: Int) : Framerate(Range(framerate, framerate))
        override fun toString(): String {
            return "${range.lower}-${range.upper}"
        }

        override fun toPresentationString(): String {
            return if (range.lower == range.upper) {
                "Постоянная частота кадров: ${range.lower}"
            } else {
                "Переменная частота кадров: ${range.lower} - ${range.upper}"
            }
        }
    }
}

internal fun Size.toResolution(): Option.Resolution {
    return Option.Resolution(this.width, this.height)
}

internal fun Option.Resolution.toSize(): Size {
    return Size(this.width, this.height)
}

internal fun String.toResolution(): Option.Resolution {
    val (width, height) = this.split("x").map { it.toInt() }
    return Option.Resolution(width, height)
}

internal fun String.toFramerate(): Option.Framerate {
    val (start, end) = this.split("-").map { it.toInt() }
    return Option.Framerate(Range(start, end))
}

internal fun String.toBitrate(): Option.Bitrate {
    val (start, end) = this.replace(" ", "").split("-").map { it.toInt() }
    return Option.Bitrate(Range(start, end))
}
