package com.example.nutcracker_streaming_app.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaFormat
import androidx.core.content.edit
import com.example.nutcracker_streaming_app.NsaApplication
import com.example.nutcrackerstreamingapp.R

object NsaPreferences {
    private lateinit var appContext: Context
    const val NOTIFICATION_CHANNEL_NAME = "NutcrackerChannel"
    const val NOTIFICATION_CHANNEL_ID = "ForegroundStreamingService"

    fun initWithApplicationContext(applicationContext: Context) {
        if (!::appContext.isInitialized) {
            appContext = applicationContext
        }
    }

    private fun pref(): SharedPreferences = NsaApplication.prefs
    private fun encryptedPrefs(): SharedPreferences = NsaApplication.encryptedPrefs


    var rtmpLink : Option.Link.RtmpLink
        get() = Option.Link.RtmpLink(encryptedPrefs().getString(
            getPrefName(Prefs.RtmpLink), getPrefDefault(Prefs.RtmpLink)
        ).orEmpty())
        set(value) = encryptedPrefs().edit(commit = true) { putString(getPrefName(Prefs.RtmpLink), value.toString()) }

    var srtLink : Option.Link.SrtLink
        get() = Option.Link.SrtLink(encryptedPrefs().getString(
            getPrefName(Prefs.SrtLink), getPrefDefault(Prefs.SrtLink)
        ).orEmpty())
        set(value) = encryptedPrefs().edit(commit = true) { putString(getPrefName(Prefs.SrtLink), value.toString()) }

    var bitrateRange: Option.Bitrate
        get() = pref().getString(
            getPrefName(Prefs.Bitrate), getPrefDefault(Prefs.Bitrate)
        ).orEmpty().toBitrate()
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.Bitrate), value.toString()) }


    var resolution: Option.Resolution
        get() = pref().getString(
            getPrefName(Prefs.Resolution), getPrefDefault(Prefs.Resolution)
        ).orEmpty().toResolution()
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.Resolution), value.toString()) }

    var framerate: Option.Framerate
        get() = pref().getString(
            getPrefName(Prefs.Framerate), getPrefDefault(Prefs.Framerate)
        ).orEmpty().toFramerate()
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.Framerate), value.toString()) }

    var audioEncoder: Option.AudioEncoder
        get() = Option.AudioEncoder(pref().getString(
            getPrefName(Prefs.AudioEncoder), getPrefDefault(Prefs.AudioEncoder)
        ).orEmpty())
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.AudioEncoder), value.toString()) }

    var videoEncoder: Option.VideoEncoder
        get() = Option.VideoEncoder(pref().getString(
            getPrefName(Prefs.VideoEncoder), getPrefDefault(Prefs.VideoEncoder)
        ).orEmpty())
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.VideoEncoder), value.toString()) }

    var protocol: Option.Protocol
        get() = when {
            pref().getString(getPrefName(Prefs.Protocol), getPrefDefault(Prefs.Protocol)) == Option.Protocol.Srt.PROTOCOL -> Option.Protocol.Srt
            else -> Option.Protocol.Rtmp
        }
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.Protocol), value.toString()) }

    var adaptiveBitrateEnabled: Option.AdaptiveBitrateEnabled
        get() = when {
            pref().getString(getPrefName(Prefs.AdaptBitrate), getPrefDefault(Prefs.AdaptBitrate)) == "false" -> Option.AdaptiveBitrateEnabled(false)
            else -> Option.AdaptiveBitrateEnabled(true)
        }
        set(value) = pref().edit(commit = true) { putString(getPrefName(Prefs.AdaptBitrate), value.toString()) }

    private fun getPrefName(prefs: Prefs): String {
        return when (prefs) {
            Prefs.Resolution -> appContext.getString(R.string.pref_name_resolution)
            Prefs.AudioEncoder -> appContext.getString(R.string.pref_name_audio_encoder)
            Prefs.VideoEncoder -> appContext.getString(R.string.pref_name_video_encoder)
            Prefs.RtmpLink -> appContext.getString(R.string.pref_name_rtmp_stream_link_key)
            Prefs.SrtLink -> appContext.getString(R.string.pref_name_srt_stream_link_key)
            Prefs.Protocol -> appContext.getString(R.string.pref_name_protocol)
            Prefs.Framerate -> appContext.getString(R.string.pref_name_framerate)
            Prefs.Bitrate -> appContext.getString(R.string.pref_name_bitrate)
            Prefs.AdaptBitrate -> appContext.getString(R.string.pref_name_adaptive_bitrate_enabled)
        }
    }

    private fun getPrefDefault(prefs: Prefs): String {
        return when (prefs) {
            Prefs.Resolution -> appContext.getString(R.string.pref_default_resolution)
            Prefs.AudioEncoder -> MediaFormat.MIMETYPE_AUDIO_AAC
            Prefs.VideoEncoder -> MediaFormat.MIMETYPE_VIDEO_AVC
            Prefs.RtmpLink -> ""
            Prefs.SrtLink -> ""
            Prefs.Protocol -> "RTMP"
            Prefs.Framerate -> "30-30"
            Prefs.Bitrate -> "2000000-10000000"
            Prefs.AdaptBitrate -> "false"
        }
    }
}



sealed class Prefs {
    data object Bitrate: Prefs()
    data object Framerate: Prefs()
    data object Resolution : Prefs()
    data object AudioEncoder: Prefs()
    data object VideoEncoder: Prefs()
    data object RtmpLink: Prefs()
    data object SrtLink: Prefs()
    data object Protocol: Prefs()
    data object AdaptBitrate: Prefs()
}