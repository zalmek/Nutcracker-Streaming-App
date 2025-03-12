package com.example.nutcracker_streaming_app.network

data class QrResponse(
    val activity: Activity
) {
    data class Activity(
        val halls: List<Halls>
    ) {
        data class Halls(
            val stream: Stream
        ) {
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
