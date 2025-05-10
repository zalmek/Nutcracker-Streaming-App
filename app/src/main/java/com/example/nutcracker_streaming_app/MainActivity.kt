package com.example.nutcracker_streaming_app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets.Type
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutcracker_streaming_app.network.QrResponse
import com.example.nutcracker_streaming_app.permissions.PermissionScreen
import com.example.nutcracker_streaming_app.settings.SettingsScreen
import com.example.nutcracker_streaming_app.stream.StreamScreen
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.Option
import com.example.nutcracker_streaming_app.utils.Routes
import com.example.nutcracker_streaming_app.utils.rememberStreamingService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.gson.Gson
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var streamingService: StreamingService? = null

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowInsetsController.hide(Type.systemBars())
        }
        setContent {
            val handler = CoroutineExceptionHandler { _, throwable ->
                // process the Throwable
                Log.e("ERROR FONT", "There has been an issue: ", throwable)
            }
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
            streamingService = rememberStreamingService<StreamingService, StreamingService.LocalBinder> { service }
            LaunchedEffect(streamingService, permissionsState.allPermissionsGranted) {
                permissionsState.launchMultiplePermissionRequest()
                if (streamingService == null) {
                    if (permissionsState.allPermissionsGranted) {
                        startForegroundService(Intent(this@MainActivity, StreamingService::class.java))
                    }
                }
            }
            CompositionLocalProvider(
                LocalFontFamilyResolver provides createFontFamilyResolver(
                    LocalContext.current,
                    handler
                )
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Colors.Background.main)
                ) {
                    if (permissionsState.allPermissionsGranted) {
                        MyAppNavHost(intent = intent, service = streamingService)
                    } else {
                        PermissionScreen(permissionsList, permissionsState)
                    }
                }
            }
        }
    }
}

@Composable
fun MyAppNavHost(
    service: StreamingService?,
    modifier: Modifier = Modifier,
    intent: Intent? = null,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.StreamScreen
    ) {
        composable<Routes.StreamScreen> {
            StreamScreen(navController = navController, streamingService = service)
        }
        composable<Routes.SettingsScreen> { backStackEntry ->
            SettingsScreen(navController)
        }
    }
    LaunchedEffect(Unit) {
        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data
        appLinkData?.let {
//            NsaPreferences.streamLink = Option.Link(appLinkData.toString())
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(appLinkData.toString())
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val responseBody = response.body()!!
                        val gson = Gson()
                        val qrResponse =
                            gson.fromJson(responseBody.string(), QrResponse::class.java)
                        NsaPreferences.rtmpLink = Option.Link.RtmpLink(
                            "${qrResponse.activity.halls[0].stream.rtmpUrl}/${qrResponse.activity.halls[0].stream.rtmpKey}"
                        )
                        NsaPreferences.srtLink = Option.Link.SrtLink(
                            "srt://" +
                                    qrResponse.activity.halls[0].stream.srtHost +
                                    ":${qrResponse.activity.halls[0].stream.srtPort}" +
                                    "?streamid=${qrResponse.activity.halls[0].stream.srtKey}"
                        )
                        println(NsaPreferences.rtmpLink)
                        println(NsaPreferences.srtLink)
                    }
                }
            })
        }
    }
}
