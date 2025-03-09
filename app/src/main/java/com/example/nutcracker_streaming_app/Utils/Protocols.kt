package com.example.nutcracker_streaming_app.Utils

sealed class Protocol {
    data object Rtmp: Protocol()
    data object Srt: Protocol()
}