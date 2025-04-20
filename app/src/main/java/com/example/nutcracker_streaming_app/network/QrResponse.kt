package com.example.nutcracker_streaming_app.network

import androidx.annotation.Keep

@Keep
data class QrResponse(
    val activity: Activity
) {
    @Keep
    data class Activity(
        val halls: List<Halls>
    ) {
        @Keep
        data class Halls(
            val stream: Stream
        ) {
            @Keep
            data class Stream(
                val streamServerUrl: String,
                val rtmpUrl: String,
                val rtmpKey: String,
                val hlsCdnUrl: String,
                val rtspUrl: String,
                val srtPort: String,
                val srtHost: String,
                val srtKey: String,
            )
        }
    }
}
