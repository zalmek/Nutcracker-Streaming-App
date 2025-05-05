package com.example.nutcracker_streaming_app.stream

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.camera.viewfinder.core.TransformationInfo
import androidx.camera.viewfinder.core.ViewfinderSurfaceRequest
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutcracker_streaming_app.StreamingService
import com.example.nutcracker_streaming_app.permissions.PermissionScreen
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.utils.OverridedCameraXViewFinder
import com.example.nutcracker_streaming_app.utils.Routes
import com.example.nutcrackerstreamingapp.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StreamScreen(
    navController: NavController,
    streamingService: StreamingService?,
    viewModel: StreamViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val permissionsList = persistentListOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
    )
    permissionsList.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add(android.Manifest.permission.FOREGROUND_SERVICE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add(android.Manifest.permission.FOREGROUND_SERVICE_CAMERA)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.POST_NOTIFICATIONS
        }
    }
    val permissionsState = rememberMultiplePermissionsState(permissionsList)
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionsState.allPermissionsGranted) {
        val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle(lifecycleOwner)

        val context = LocalContext.current
        val currentImplementationMode by rememberUpdatedState(ImplementationMode.EXTERNAL)

        LaunchedEffect(lifecycleOwner) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }
        surfaceRequest?.let { request ->
            OverridedCameraXViewFinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
                updateSurface = {},
            )
        }

        val activity = LocalActivity.current

        val coroutineScope = rememberCoroutineScope()
        var streamActive by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(streamingService) {
            if (streamingService == null) {
                context.startForegroundService(Intent(activity, StreamingService::class.java))
                streamActive = false
            }
        }
        LifecycleStartEffect(Unit) {
            onStopOrDispose {
                streamActive = false
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
                        coroutineScope.launch {
                            try {
                                val streamStarted = streamingService?.startStream()?.await()
                                if (streamStarted != true) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.stream_start_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    streamActive = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.stream_started), Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.restart_app), Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            ) {
                Text(
                    if (!streamActive) stringResource(R.string.start_stream)
                    else stringResource(R.string.stop_stream)
                )
            }
        }


    } else {
        PermissionScreen(permissionsList, permissionsState)
    }
}


@Immutable
private data class ViewfinderArgs(
    val viewfinderSurfaceRequest: ViewfinderSurfaceRequest,
    val implementationMode: ImplementationMode,
    val transformationInfo: TransformationInfo
)