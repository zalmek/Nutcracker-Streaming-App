package com.example.nutcracker_streaming_app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.streamers.StreamerLifeCycleObserver
import io.github.thibaultbee.streampack.streamers.interfaces.ILiveStreamer
import io.github.thibaultbee.streampack.streamers.interfaces.IStreamer
import io.github.thibaultbee.streampack.streamers.interfaces.settings.IBaseCameraStreamerSettings
import io.github.thibaultbee.streampack.utils.CameraSettings
import io.github.thibaultbee.streampack.utils.backCameraList
import io.github.thibaultbee.streampack.utils.frontCameraList
import io.github.thibaultbee.streampack.utils.getCameraStreamer
import io.github.thibaultbee.streampack.utils.getFileStreamer
import io.github.thibaultbee.streampack.utils.getLiveStreamer
import io.github.thibaultbee.streampack.utils.getStreamer
import io.github.thibaultbee.streampack.utils.isBackCamera
import io.github.thibaultbee.streampack.views.PreviewView
import kotlinx.coroutines.runBlocking

@SuppressLint("MissingPermission")
class StreamerManager(private val context: Context) {
    private var streamer: IStreamer? = null

    init {
        rebuildStreamer()
    }
    var onErrorListener: OnErrorListener?
        get() = streamer?.onErrorListener
        set(value) {
            streamer?.onErrorListener = value
        }

    var onConnectionListener: OnConnectionListener?
        get() = streamer?.getLiveStreamer()?.onConnectionListener
        set(value) {
            streamer?.getLiveStreamer()?.onConnectionListener = value
        }

    val cameraId: String?
        get() = streamer?.getCameraStreamer()?.camera

    val streamerLifeCycleObserver: StreamerLifeCycleObserver by lazy {
        StreamerLifeCycleObserver(streamer!!)
    }

    fun getLiveStreamer(): ILiveStreamer? {
        return streamer?.getStreamer<ILiveStreamer>()
    }

    val requiredPermissions: List<String>
        get() {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
            streamer?.getFileStreamer()?.let {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return permissions
        }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun rebuildStreamer(protocol: Option.Protocol = NsaPreferences.protocol) {
        streamer = when (protocol) {
            Option.Protocol.Rtmp -> StreamerHelper.getRtmpStreamer(context)
            Option.Protocol.Srt -> StreamerHelper.getSrtStreamer(context)
        }
    }

    fun inflateStreamerView(view: PreviewView) {
        view.streamer = streamer?.getCameraStreamer()
    }

    fun stopStream() {
        runBlocking {
            streamer?.stopStream()
        }
        streamer?.getLiveStreamer()?.disconnect()
    }

    fun release() {
        streamer?.release()
    }

    fun toggleCamera() {
        streamer?.getCameraStreamer()?.let {
            // Handle devices with only one camera
            val cameras = if (context.isBackCamera(it.camera)) {
                context.frontCameraList
            } else {
                context.backCameraList
            }
            if (cameras.isNotEmpty()) {
                it.camera = cameras[0]
            }
        }
    }

    val cameraSettings: CameraSettings?
        get() {
            val settings = streamer?.settings
            return if (settings is IBaseCameraStreamerSettings) {
                settings.camera
            } else {
                null
            }
        }

    var isMuted: Boolean
        get() = streamer?.settings?.audio?.isMuted ?: true
        set(value) {
            streamer?.settings?.audio?.isMuted = value
        }
}