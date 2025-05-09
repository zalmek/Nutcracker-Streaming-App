package com.example.nutcracker_streaming_app.stream

import com.example.nutcracker_streaming_app.StreamingService
import com.example.nutcracker_streaming_app.presentetion.base.BaseViewModel
import com.example.nutcracker_streaming_app.stream.StreamContract.Effect.ShowSnackbar
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.Option
import com.example.nutcracker_streaming_app.utils.StreamManager
import com.example.nutcrackerstreamingapp.R

class StreamViewModel :
    BaseViewModel<StreamContract.Event, StreamContract.State, StreamContract.Effect>() {
    override fun setInitialState(): StreamContract.State {

        return StreamContract.State(
            streamState = if (StreamManager.isStreaming) StreamState.Connected else StreamState.Disconnected,
            cameraIsConfigured = StreamManager.isPreConfigured,
            currentBitrate = "0",
        )
    }

    override fun handleEvents(event: StreamContract.Event) {
        when (event) {
            is StreamContract.Event.OnStartStopClicked -> {
                toggleStream(event.service)
            }
            StreamContract.Event.OnConnectionFailed -> setState {
                setEffect { ShowSnackbar(R.string.snackbar_stream_connection_failed) }
                StreamManager.stopStream()
                copy(streamState = StreamState.Failed)
            }
            StreamContract.Event.OnConnectionStart -> setState {
                setEffect { ShowSnackbar(R.string.snackbar_stream_connecting) }
                copy(streamState = StreamState.Connecting)
            }
            StreamContract.Event.OnConnectionSuccess -> setState {
                setEffect { ShowSnackbar(R.string.stream_started) }
                copy(streamState = StreamState.Connected)
            }
            StreamContract.Event.OnDisconnect -> setState {
                if (streamState != StreamState.Failed) {
                    setEffect { ShowSnackbar(R.string.snackbar_stream_stopped) }
                }
                copy(streamState = StreamState.Disconnected)
            }
            StreamContract.Event.ViewDetached -> {
                StreamManager.detachPreview()
            }

            is StreamContract.Event.AttachView -> {
                StreamManager.attachPreview(
                    surface = event.surface,
                    context = event.context,
                    width = event.width,
                    height = event.height
                )
            }

            is StreamContract.Event.ConfigureCamera -> {
                val isConfigured = StreamManager.createStream(
                    event.context,
                    event.service
                )
                setState {
                    copy(cameraIsConfigured = isConfigured)
                }
            }

            is StreamContract.Event.AttachToService -> {
                event.service.bindViewModel(this)
            }

            StreamContract.Event.PrepareVideo -> {
                StreamManager.prepareStream(
                    if (NsaPreferences.protocol == Option.Protocol.Rtmp) {
                        StreamManager.rtmpStream
                    } else {
                        StreamManager.srtStream
                    }
                )
            }

            is StreamContract.Event.OnNewBitrate -> setState {
                copy(currentBitrate = event.bitrate)
            }
        }
    }

    private fun toggleStream(streamingService: StreamingService) {
        when (viewState.value.streamState) {
            StreamState.Connected -> streamingService.stopStream()
            StreamState.Connecting -> { }
            StreamState.Failed -> streamingService.startStream(this)
            StreamState.Disconnected -> streamingService.startStream(this)
        }
    }
}