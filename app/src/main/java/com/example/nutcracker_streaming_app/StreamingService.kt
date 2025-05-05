package com.example.nutcracker_streaming_app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.NsaPreferences.NOTIFICATION_CHANNEL_ID
import com.example.nutcracker_streaming_app.utils.NsaPreferences.NOTIFICATION_CHANNEL_NAME
import com.example.nutcracker_streaming_app.utils.Option
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.sources.audio.MicrophoneSource
import com.pedro.extrasources.CameraXSource
import com.pedro.library.rtmp.RtmpStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class StreamingService : Service(), ConnectChecker {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val srtLink get() = NsaPreferences.srtLink.toString()
    private val rtmpLink get() = NsaPreferences.rtmpLink.toString()
    private val binder = LocalBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("My notification").setContentText("Hello World!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent).setAutoCancel(false)


        startForeground(1, builder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: StreamingService
            get() = this@StreamingService
    }

    @SuppressLint("MissingPermission")
    fun startStream(
        protocol: Option.Protocol = NsaPreferences.protocol
    ): Deferred<Boolean?> {
        return coroutineScope.async {
            when (protocol) {
                Option.Protocol.Rtmp -> {
                    try {
                        val stream: RtmpStream = RtmpStream(
                            applicationContext,
                            binder.service,
                            videoSource = CameraXSource(applicationContext),
                            audioSource = MicrophoneSource()
                        ).apply {
                            getGlInterface().autoHandleOrientation = true
                            getStreamClient().setBitrateExponentialFactor(0.5f)
                            getStreamClient().forceIncrementalTs(true)
                        }
                        withContext(Dispatchers.Main) {
                            stream.prepareVideo(1280, 720, 2000000)
                            stream.prepareAudio(32000, true, 128000)
                            stream.startStream(rtmpLink)
                        }
                        return@async true
                    } catch (e: Exception) {
                        Log.d("LOGLINK", "При попытке начать стрим произошла ошибка ${e.message}")
                        return@async false
                    }
                }
                Option.Protocol.Srt -> {
                    try {
                        return@async true
                    } catch (e: Exception) {
                        Log.d("LOGLINK", "При попытке начать стрим произошла ошибка ${e.message}")
                        return@async false
                    }
                }
            }
        }
    }

    override fun onConnectionStarted(url: String) {
        Log.d("ZALMEK", "onConnectionStarted: $url")
    }

    override fun onConnectionSuccess() {
        Log.d("ZALMEK", "onConnectionSuccess: ")
    }

    override fun onConnectionFailed(reason: String) {
        Log.d("ZALMEK", "onConnectionFailed: $reason ")
    }

    override fun onDisconnect() {
        Log.d("ZALMEK", "onDisconnect: ")
    }

    override fun onAuthError() {
        Log.d("ZALMEK", "onAuthError: ")
    }

    override fun onAuthSuccess() {
        Log.d("ZALMEK", "onAuthSuccess: ")
    }
}