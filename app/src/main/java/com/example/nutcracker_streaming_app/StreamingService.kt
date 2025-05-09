package com.example.nutcracker_streaming_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nutcracker_streaming_app.stream.StreamContract
import com.example.nutcracker_streaming_app.stream.StreamViewModel
import com.example.nutcracker_streaming_app.utils.IntentActions
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.NsaPreferences.NOTIFICATION_CHANNEL_ID
import com.example.nutcracker_streaming_app.utils.NsaPreferences.NOTIFICATION_CHANNEL_NAME
import com.example.nutcracker_streaming_app.utils.StreamManager
import com.pedro.common.ConnectChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.Locale

class StreamingService : Service(), ConnectChecker {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val binder = LocalBinder()
    private var viewModel: StreamViewModel? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val serviceStopIntent = Intent(this, MainActivity::class.java)
        serviceStopIntent.putExtra("Notification Intent", IntentActions.STOP_STREAM)
        val pendingServiceStopIntent: PendingIntent = PendingIntent.getActivity(this, 0, serviceStopIntent, PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Nutcracker Streaming").setContentText("Click to stop the stream")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent).setAutoCancel(false)
            .addAction(NotificationCompat.Action(null, "Завершить стрим", pendingServiceStopIntent))


        startForeground(1, builder.build())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: StreamingService
            get() = this@StreamingService
    }

    fun startStream(viewModel: StreamViewModel) {
        bindViewModel(viewModel)
        StreamManager.startStream()
    }

    fun stopStream() {
        StreamManager.stopStream()
        viewModel?.setEvent(StreamContract.Event.OnDisconnect)
        unbindViewModel()
    }

    fun bindViewModel(viewModel: StreamViewModel) {
        this.viewModel = viewModel
    }

    private fun unbindViewModel() {
        viewModel = null
    }

    override fun onConnectionStarted(url: String) {
        viewModel?.setEvent(StreamContract.Event.OnConnectionStart)
    }

    override fun onConnectionSuccess() {
        viewModel?.setEvent(StreamContract.Event.OnConnectionSuccess)
    }

    override fun onConnectionFailed(reason: String) {
        viewModel?.setEvent(StreamContract.Event.OnConnectionFailed)
    }

    override fun onDisconnect() {
        viewModel?.setEvent(StreamContract.Event.OnDisconnect)
    }

    override fun onNewBitrate(bitrate: Long) {
        if (NsaPreferences.adaptiveBitrateEnabled.enabled) {
            StreamManager.bitrateAdapter.adaptBitrate(bitrate, StreamManager.rtmpStream.getStreamClient().hasCongestion())
        }
        viewModel?.setEvent(StreamContract.Event.OnNewBitrate(String.format(Locale.getDefault(), "%.1f mb/s", bitrate / 1000_000f)))
    }

    override fun onAuthError() {
        viewModel?.let {

        }
    }
    override fun onAuthSuccess() {
        viewModel?.let {

        }
    }
}