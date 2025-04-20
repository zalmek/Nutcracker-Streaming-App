package com.example.nutcracker_streaming_app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutcracker_streaming_app.demo.PreviewScreen
import com.example.nutcracker_streaming_app.network.QrResponse
import com.example.nutcracker_streaming_app.permissions.PermissionScreen
import com.example.nutcracker_streaming_app.settings.SettingsScreen
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.Option
import com.example.nutcracker_streaming_app.utils.Routes
import com.example.nutcracker_streaming_app.utils.rememberStreamingService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val handler = CoroutineExceptionHandler { _, throwable ->
                // process the Throwable
                Log.e("ERROR FONT", "There has been an issue: ", throwable)
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
                    MyAppNavHost(intent = intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, StreamingService::class.java)
        stopService(intent)
    }
}

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    intent: Intent? = null,
    navController: NavHostController = rememberNavController(),
) {
    val streamingService = rememberStreamingService<StreamingService, StreamingService.LocalBinder> { service }
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.PermissionsScreen
    ) {
        composable<Routes.MainScreen> {
            PreviewScreen(navController = navController, streamingService)
        }
        composable<Routes.SettingsScreen> { backStackEntry ->
            SettingsScreen(navController)
        }
        composable<Routes.PermissionsScreen> {
            PermissionScreen(navController)
        }
    }
    LaunchedEffect(Unit) {
        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data
        appLinkData?.let {
//            NsaPreferences.streamLink = Option.Link(appLinkData.toString())
            navController.navigate(Routes.PermissionsScreen)
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
