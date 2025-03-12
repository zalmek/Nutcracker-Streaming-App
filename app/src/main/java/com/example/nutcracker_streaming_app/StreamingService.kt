package com.example.nutcracker_streaming_app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.NsaPreferences.NOTIFICATION_CHANNEL_ID
import com.example.nutcracker_streaming_app.utils.NsaPreferences.NOTIFICATION_CHANNEL_NAME
import com.example.nutcracker_streaming_app.utils.Option
import com.example.nutcracker_streaming_app.utils.StreamerManager
import com.example.nutcrackerstreamingapp.R
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.ext.rtmp.streamers.CameraRtmpLiveStreamer
import io.github.thibaultbee.streampack.ext.srt.data.SrtConnectionDescriptor
import io.github.thibaultbee.streampack.ext.srt.streamers.CameraSrtLiveStreamer
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamingService : Service() {
    val streamerManager by lazy { StreamerManager(applicationContext) }
    private val streamer get() = streamerManager.getLiveStreamer()
    private var surfaceView: Surface? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val srtLink = NsaPreferences.srtLink.toString()
    private val rtmpLink = NsaPreferences.rtmpLink.toString()
    private val binder = LocalBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        startForeground(1, builder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder() {
        val service: StreamingService
            get() = this@StreamingService
    }

    private val onErrorListener = object : OnErrorListener {
        override fun onError(error: StreamPackError) {
            Log.e("ZALMEK", "onError", error)
        }
    }

    private val onConnectionListener = object : OnConnectionListener {
        override fun onLost(message: String) {
            Log.w("ZALMEK", "Connection succeeded")
        }

        override fun onFailed(message: String) {
            // Not needed as we catch startStream
        }

        override fun onSuccess() {
            Log.i("ZALMEK", "Connection succeeded")
        }
    }

    fun status(): String {
        return when {
            streamer?.isConnected == true -> "Connected"
            else -> "Disconnected"
        }
    }

    @SuppressLint("MissingPermission")
    fun provideSurface(surface: Surface?) {
        streamerManager.rebuildStreamer()
        this.surfaceView = surface
        if (surfaceView?.isValid == true) {
            when (NsaPreferences.protocol) {
                Option.Protocol.Rtmp -> (streamer as CameraRtmpLiveStreamer).startPreview(surfaceView!!)
                Option.Protocol.Srt ->(streamer as CameraSrtLiveStreamer).startPreview(surfaceView!!)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @SuppressLint("MissingPermission")
    fun startStream(protocol: Option.Protocol = NsaPreferences.protocol) {
        coroutineScope.launch {
            streamerManager.rebuildStreamer()
            streamerManager.onErrorListener = onErrorListener
            streamerManager.onConnectionListener = onConnectionListener
            when (protocol) {
                Option.Protocol.Rtmp -> {
                    surfaceView?.let {
                        try {
                            (streamer as CameraRtmpLiveStreamer).startPreview(it, (streamer as CameraRtmpLiveStreamer).camera,)
                            Log.d("LOGLINK", "startStream: $ $rtmpLink")
                            (streamer as CameraRtmpLiveStreamer).startStream(rtmpLink)

                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "При попытке начать стрим произошла ошибка", Toast.LENGTH_SHORT)
                        }
                    }
                }
                Option.Protocol.Srt -> {
                    surfaceView?.let {
                        try {
                            (streamer as CameraSrtLiveStreamer).startPreview(it, (streamer as CameraSrtLiveStreamer).camera)
                            (streamer as CameraSrtLiveStreamer).startStream(
                                SrtConnectionDescriptor.fromUrlAndParameters(
                                    url = srtLink,
                                    streamId = srtLink.split(applicationContext.getString(R.string.stremaid_param))[1]
                                )
                            )
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "При попытке начать стрим произошла ошибка", Toast.LENGTH_SHORT) }
                    }
                }
            }
        }
    }

    fun closeStream() {
        coroutineScope.launch {
            streamerManager.stopStream()
        }
    }
}