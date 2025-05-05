package com.example.nutcracker_streaming_app.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.CamcorderProfile
import android.media.MediaCodecList
import android.media.MediaRecorder
import android.util.Range
import android.util.Size
import androidx.compose.runtime.Stable
import com.example.nutcracker_streaming_app.settings.SettingsContract

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

    lateinit var profiles: List<Int>

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

    fun initSupportedSettings(context: Context) {
        // 1. Находим ID задней (главной) камеры -----------------------
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.first { id ->
            val char = cameraManager.getCameraCharacteristics(id)
            char.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }

        // 2. Получаем характеристики этой камеры -------------------
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)

        val configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: throw IllegalStateException("StreamConfigurationMap недоступен")
        supportedFramerates = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                ?.toList() ?: emptyList()

        // 3. Собираем поддерживаемые разрешения ---------------------
        //    берем все разрешения для записи видео через MediaRecorder
        supportedResolutions = configMap
            .getOutputSizes(MediaRecorder::class.java)
            .toList()

        // 4. Собираем все допустимые диапазоны FPS для видеовыхода ---
//        supportedFramerates += configMap
//            .highSpeedVideoFpsRanges
//            .toList()

        // 5. Определяем профили видеозаписи (CamcorderProfile) ------
        //    проверяем, какие предустановленные профили вообще есть
        val allProfiles = listOf(
            CamcorderProfile.QUALITY_HIGH,
            CamcorderProfile.QUALITY_LOW,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_2160P
        )
        profiles = allProfiles.filter { q ->
            CamcorderProfile.hasProfile(cameraId.toInt(), q)
        }

        // 6. Собираем битрейт-диапазон на основании профилей -------
        //    возьмем min и max видеобитрейт среди доступных профилей
        val bitrates = profiles.map { q ->
            CamcorderProfile.get(cameraId.toInt(), q).videoBitRate
        }
        val minBr = bitrates.minOrNull() ?: 0
        val maxBr = bitrates.maxOrNull() ?: 0
        supportedBitrates = Range(minBr, maxBr)

        // 7. Выбираем кодеки, поддерживающие видео ------------------
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val videoTypes = listOf("video/avc", "video/hevc", "video/mp4v-es", "video/x-vnd.on2.vp8")
        supportedVideoEncoder = codecList.codecInfos
            .filter { it.isEncoder }
            .flatMap { ci -> ci.supportedTypes.toList() }
            .distinct()
            .filter { mime -> videoTypes.contains(mime) }
        // Берём первый попавшийся для примера
        videoEncoder = supportedVideoEncoder.firstOrNull()
            ?: throw IllegalStateException("Нет видеокодека")

        // 8. Аналогично — список и выбор аудиокодека -----------------
        val audioTypes = listOf("audio/mp4a-latm", "audio/3gpp", "audio/vorbis")
        supportedAudioEncoder = codecList.codecInfos
            .filter { it.isEncoder }
            .flatMap { it.supportedTypes.toList() }
            .distinct()
            .filter { audioTypes.contains(it) }
        audioEncoder = supportedAudioEncoder.firstOrNull()
            ?: throw IllegalStateException("Нет аудиокодека")

        // 9. Диапазон входных каналов аудио --------------------------
        //    обычно 1 (моно) и 2 (стерео). Проверим, какие реально работают:
        val possibleRates = listOf(1, 2)
        val good = possibleRates.filter { channels ->
            val sr = 44100  // любой стандартный sample rate
            val buf = AudioRecord.getMinBufferSize(
                sr,
                if (channels == 1)
                    AudioFormat.CHANNEL_IN_MONO
                else
                    AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            buf > 0
        }
        inputChannelRange = if (good.isNotEmpty()) {
            Range(good.minOrNull()!!, good.maxOrNull()!!)
        } else {
            Range(1, 1)
        }

        // 10. Поддерживаемые sample rates для AudioRecord ----------
        val allSampleRates = listOf(8000, 11025, 16000, 22050, 32000, 44100, 48000)
        sampleRates = allSampleRates.filter { rate ->
            AudioRecord.getMinBufferSize(
                rate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ) > 0
        }

        // 11. Поддерживаемые форматы PCM (байтовые форматы) ---------
        byteFormats = listOf(
            AudioFormat.ENCODING_PCM_8BIT,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // 12. Диапазон видеобитрейта (общий), если нужен отдельно ---
        bitrateRange = Range(minBr, maxBr)
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
