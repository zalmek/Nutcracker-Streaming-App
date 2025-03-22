package com.example.nutcracker_streaming_app.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.nutcracker_streaming_app.StreamingService
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.utils.Routes
import com.example.nutcrackerstreamingapp.R
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
@Composable
fun PreviewScreen(
    navController: NavController,
    streamingService: StreamingService?
) {
    val activity = LocalContext.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val stateFlow = lifecycle.currentStateFlow
    val currentLifecycleState by stateFlow.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var surfaceView by remember {
        mutableStateOf<Surface?>(null)
    }
    var streamActive by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(streamingService) {
        if (streamingService == null) {
            activity.startForegroundService(Intent(activity, StreamingService::class.java))
            streamActive = false
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            streamingService.provideSurface(surfaceView)
        }
    }
    LaunchedEffect(streamingService, surfaceView) {
        streamingService?.provideSurface(surfaceView)
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            streamActive = false
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            streamingService?.closeStream()
        }
    }

    AndroidExternalSurface(
        modifier = Modifier.fillMaxSize()
    ) {
        onSurface { surface, width, height ->
            surfaceView = surface
            streamingService?.streamerManager?.let { lifecycle.addObserver(it.streamerLifeCycleObserver) }
            streamingService?.provideSurface(surface)
            surface.onDestroyed {
                streamingService?.provideSurface(null)
            }
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
    }
    Box(
        modifier = Modifier
            .padding(40.dp)
            .fillMaxSize()
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_settings_40),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(
                    enabled = !streamActive,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { navController.navigate(Routes.SettingsScreen) },
                ),
        )
        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(containerColor = Colors.Background.button),
            onClick = {
                streamActive = !streamActive
                if (streamActive) {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    coroutineScope.launch {
                        val streamStarted = streamingService?.startStream()?.await()
                        if (streamStarted != true) {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.stream_start_error), Toast.LENGTH_SHORT
                            ).show()
                            streamingService?.closeStream()
                            streamActive = false
                            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        } else {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.stream_started), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    streamingService?.closeStream()
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        ) {
            Text(
                if (!streamActive) stringResource(R.string.start_stream)
                else stringResource(R.string.stop_stream)
            )
        }
    }
}