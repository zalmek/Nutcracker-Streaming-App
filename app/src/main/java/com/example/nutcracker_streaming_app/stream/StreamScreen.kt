package com.example.nutcracker_streaming_app.stream

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutcracker_streaming_app.StreamingService
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.utils.AspectRatio
import com.example.nutcracker_streaming_app.utils.Routes
import com.example.nutcracker_streaming_app.utils.videoCamVector
import com.example.nutcracker_streaming_app.utils.calculateHeightOfUnusedArea
import com.example.nutcrackerstreamingapp.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StreamScreen(
    navController: NavController,
    viewModel: StreamViewModel = viewModel(),
    streamingService: StreamingService?,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val freeSpace = calculateHeightOfUnusedArea(AspectRatio.AspectRatio16to9)
    Log.d("ZALMK", "StreamScreen: $freeSpace")
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    val streamState = viewState.streamState
    val currentBitrate = viewState.currentBitrate
    val snackbarHostState = remember { SnackbarHostState() }
    var isConfigured = viewState.cameraIsConfigured

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.setEvent(StreamContract.Event.ViewDetached)
    }
    LaunchedEffect(Unit, streamingService) {
        if (streamingService!= null) {
            viewModel.setEvent(StreamContract.Event.AttachToService(streamingService))
            if (!isConfigured) {
                viewModel.setEvent(
                    StreamContract.Event.ConfigureCamera(
                        context = activity?.applicationContext ?: context,
                        service = streamingService,
                    )
                )
            }
            viewModel.setEvent(StreamContract.Event.PrepareVideo)
        } else {
            activity?.startForegroundService(Intent(activity, StreamingService::class.java))
        }

        viewModel.effect.collect {
            when (it) {
                is StreamContract.Effect.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    launch(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(context.getString(it.message))
                    }
                }

                StreamContract.Effect.Empty -> {}
            }
        }
    }

    if (isConfigured)
        AndroidExternalSurface(
            modifier = Modifier
        ) {
            onSurface { surface, width, height ->
                viewModel.setEvent(
                    StreamContract.Event.AttachView(
                        surface = surface,
                        context = activity?.applicationContext ?: context,
                        width = width,
                        height = height
                    )
                )
                surface.onChanged { width, height ->
                    viewModel.setEvent(
                        StreamContract.Event.AttachView(
                            surface = surface,
                            context = activity?.applicationContext ?: context,
                            width = width,
                            height = height
                        )
                    )
                    Log.d("Changed", "StreamScreen: $surface $width $height")
                }
                surface.onDestroyed {
                    viewModel.setEvent(StreamContract.Event.ViewDetached)
                    Log.d("Destroyed", "StreamScreen: $surface $width $height")
                }
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.height(height = freeSpace / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = videoCamVector,
                    tint = if (streamState == StreamState.Connected) Color.Red else Colors.Text.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentDescription = null,
                )
                Text(
                    color = Colors.Text.primary,
                    text = stringResource(R.string.current_bitrate_scheme, currentBitrate)
                )
            }
            if (streamState == StreamState.Disconnected)
                Box(
                    Modifier
                        .size(freeSpace / 2)
                        .align(Alignment.TopEnd)
                        .clickable(
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { navController.navigate(Routes.SettingsScreen) },
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_40),
                        contentDescription = null,
                        tint = Colors.Icons.primary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp),
                    )
                }
            Button(
                modifier = Modifier
                    .padding(bottom = freeSpace / 2 - 36.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Background.button.copy(alpha = 0f)),
                onClick = {
                    streamingService?.let {
                        viewModel.setEvent(StreamContract.Event.OnStartStopClicked(it))
                    }
                }
            ) {
                Text(
                    when (streamState) {
                        StreamState.Connected -> stringResource(R.string.stop_stream)
                        StreamState.Connecting -> stringResource(R.string.stream_connecting)
                        StreamState.Disconnected -> stringResource(R.string.start_stream)
                        StreamState.Failed -> stringResource(R.string.stop_stream)
                    }
                )
            }
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(top = freeSpace / 2)
    )
}

