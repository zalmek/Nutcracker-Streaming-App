package com.example.nutcracker_streaming_app.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.Surface
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.nutcracker_streaming_app.StreamingService
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.Routes
import com.example.nutcracker_streaming_app.utils.rememberStreamingService
import com.example.nutcrackerstreamingapp.R


@SuppressLint("MissingPermission")
@Composable
fun DemoScreen(
    navController: NavController,
) {
    val activity = LocalContext.current as Activity
    val lifecycle  = LocalLifecycleOwner.current.lifecycle
    var surfaceView by remember {
        mutableStateOf<Surface?>(null)
    }
    var streamActive by remember {
        mutableStateOf(false)
    }
//    val lensFacing = CameraSelector.LENS_FACING_BACK
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val context = LocalContext.current
//    val preview = Preview.Builder().build()
//    val previewView = remember {
//        PreviewView(context)
//    }
//    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
//    LaunchedEffect(lensFacing) {
//        val cameraProvider = context.getCameraProvider()
//        cameraProvider.unbindAll()
//        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview,)
//        preview.setSurfaceProvider(previewView.surfaceProvider)
//    }
    activity.startForegroundService(Intent(activity, StreamingService::class.java))
    val streamingService = rememberStreamingService<StreamingService, StreamingService.LocalBinder> { service }
    LaunchedEffect(streamingService, surfaceView) {
        streamingService?.provideSurface(surfaceView)
    }
    AndroidExternalSurface(
        modifier = Modifier.fillMaxSize()
    ) {
        onSurface { surface, width, height ->
            surfaceView = surface
            streamingService?.streamerManager?.let { lifecycle.addObserver(it.streamerLifeCycleObserver) }
            streamingService?.provideSurface(surface)
            surface.onDestroyed { streamingService?.provideSurface(null) }
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
            tint = Color.Gray,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { navController.navigate(Routes.SettingsScreen) },
                ),
        )
        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                streamActive = !streamActive
                if (streamActive) {
                    println("ZALMEK ${NsaPreferences.protocol}")
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    streamingService?.startStream()
                    if (streamingService != null) {
                        println("ZALMEK ${streamingService.status()}")
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

//@Composable
//fun CameraPreviewScreen(previewView: PreviewView) {
//    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
//}
//
//private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
//    suspendCoroutine { continuation ->
//        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
//            cameraProvider.addListener({
//                continuation.resume(cameraProvider.get())
//            }, ContextCompat.getMainExecutor(this))
//        }
//    }