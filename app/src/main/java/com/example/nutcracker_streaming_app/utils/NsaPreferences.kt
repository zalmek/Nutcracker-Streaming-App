package com.example.nutcracker_streaming_app.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaFormat
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

    private fun pref(): SharedPreferences {
        val name = appContext.getString(R.string.default_preference_name)
        val mode = Context.MODE_PRIVATE
        return appContext.getSharedPreferences(name, mode)
    }

    var rtmpLink : Option.Link.RtmpLink
        get() = Option.Link.RtmpLink(pref().getString(
            getPrefName(Prefs.RtmpLink), getPrefDefault(Prefs.RtmpLink)
        ).orEmpty())
        set(value) = pref().edit().putString(getPrefName(Prefs.RtmpLink), value.toString()).apply()

    var srtLink : Option.Link.SrtLink
        get() = Option.Link.SrtLink(pref().getString(
            getPrefName(Prefs.SrtLink), getPrefDefault(Prefs.SrtLink)
        ).orEmpty())
        set(value) = pref().edit().putString(getPrefName(Prefs.SrtLink), value.toString()).apply()

    var resolution: Option.Resolution
        get() = pref().getString(
            getPrefName(Prefs.Resolution), getPrefDefault(Prefs.Resolution)
        ).orEmpty().toResolution()
        set(value) = pref().edit().putString(getPrefName(Prefs.Resolution), value.toString()).apply()

    var audioEncoder: Option.AudioEncoder
        get() = Option.AudioEncoder(pref().getString(
            getPrefName(Prefs.AudioEncoder), getPrefDefault(Prefs.AudioEncoder)
        ).orEmpty())
        set(value) = pref().edit().putString(getPrefName(Prefs.AudioEncoder), value.toString()).apply()

    var videoEncoder: Option.VideoEncoder
        get() = Option.VideoEncoder(pref().getString(
            getPrefName(Prefs.VideoEncoder), getPrefDefault(Prefs.VideoEncoder)
        ).orEmpty())
        set(value) = pref().edit().putString(getPrefName(Prefs.VideoEncoder), value.toString()).apply()

    var protocol: Option.Protocol
        get() = when {
            pref().getString(getPrefName(Prefs.Protocol), getPrefDefault(Prefs.Protocol)) == Option.Protocol.Srt.PROTOCOL -> Option.Protocol.Srt
            else -> Option.Protocol.Rtmp
        }
        set(value) = pref().edit().putString(getPrefName(Prefs.Protocol), value.toString())
            .apply()

    private fun getPrefName(prefs: Prefs): String {
        return when (prefs) {
            Prefs.Resolution -> appContext.getString(R.string.pref_name_resolution)
            Prefs.AudioEncoder -> appContext.getString(R.string.pref_name_audio_encoder)
            Prefs.VideoEncoder -> appContext.getString(R.string.pref_name_video_encoder)
            Prefs.RtmpLink -> appContext.getString(R.string.pref_name_rtmp_stream_link_key)
            Prefs.SrtLink -> appContext.getString(R.string.pref_name_srt_stream_link_key)
            Prefs.Protocol -> appContext.getString(R.string.pref_name_protocol)
        }
    }

    private fun getPrefDefault(prefs: Prefs): String {
        return when (prefs) {
            Prefs.Resolution -> appContext.getString(R.string.pref_default_resolution)
            Prefs.AudioEncoder -> MediaFormat.MIMETYPE_AUDIO_AAC
            Prefs.VideoEncoder -> MediaFormat.MIMETYPE_VIDEO_AVC
            Prefs.RtmpLink -> ""
            Prefs.SrtLink -> ""
            Prefs.Protocol -> "rtmp"
        }
    }
}



sealed class Prefs {
    data object Resolution : Prefs()
    data object AudioEncoder: Prefs()
    data object VideoEncoder: Prefs()
    data object RtmpLink: Prefs()
    data object SrtLink: Prefs()
    data object Protocol: Prefs()
}