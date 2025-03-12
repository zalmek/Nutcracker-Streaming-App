package com.example.nutcracker_streaming_app.utils

sealed class Protocol {
    data object Rtmp: Protocol()
    data object Srt: Protocol()
}